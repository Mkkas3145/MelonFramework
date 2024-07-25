package server

import com.sun.net.httpserver.HttpServer
import util.AnsiText
import util.FileManager
import util.JarLoader
import util.compress.CompressType
import java.net.BindException
import java.net.InetSocketAddress
import java.net.PortUnreachableException
import java.util.concurrent.Executors

@Suppress("UNCHECKED_CAST")
open class Server(private val port: Int, private val hosts: ArrayList<String>, private val hostConfig: Map<String, Any>) {
    private var server: HttpServer? = null
    private val managers = mutableMapOf<String, Manager>()

    private fun registerManager(host: String, manager: Manager): Server {
        managers[host] = manager
        return this
    }

    fun start(): Server {
        println("${AnsiText.BOLD}${AnsiText.YELLOW}[Melon Framework] ${port} 포트로 서버를 구동하는 중입니다...${AnsiText.RESET}")
        try {
            val addressList = ArrayList<String>()
            for (host in hosts) {
                var joinHost = host
                if (host == "*") {
                    joinHost = "localhost"
                }
                addressList.add("http://${joinHost}:${port}")

                // 매니저 생성
                val manager = Manager()
                val hostConfig = hostConfig[host] as Map<String, Any>

                // 정적 및 동적
                if (hostConfig["type"]?.equals("static")!!) {
                    manager.setProcessType(ProcessType.Static)
                } else if (hostConfig["type"]?.equals("dynamic")!!) {
                    manager.setProcessType(ProcessType.Dynamic)
                }

                // 루트 폴더 경로
                manager.setRoot(hostConfig.getOrDefault("root", "").toString())

                // 동적일 경우 .jar 파일 로드
                if (manager.getProcessType() == ProcessType.Dynamic) {
                    val files = FileManager.getFiles(manager.getRoot())
                    JarLoader.register(files)
                }

                // 404 Not Found 대체 파일 경로
                if (hostConfig["fallback"] != null) {
                    manager.setFallback(hostConfig["fallback"].toString())
                }

                // 캐시 여부
                manager.setCache(hostConfig.getOrDefault("cache", true) as Boolean)

                // 압축
                val compress = hostConfig["compress"]
                if (compress is Boolean) {
                    if (compress) {
                        manager
                            .setCompression(CompressType.Gzip, true)
                            .setCompression(CompressType.Deflate, true)
                            .setCompression(CompressType.Brotli, true)
                            .setCompression(CompressType.Zstd, true)
                    } else {
                        manager
                            .setCompression(CompressType.Gzip, false)
                            .setCompression(CompressType.Deflate, false)
                            .setCompression(CompressType.Brotli, false)
                            .setCompression(CompressType.Zstd, false)
                    }
                } else {
                    (compress as? Map<String, Boolean>)?.let { map ->
                        for (key in map.keys) {
                            val type = key.lowercase()
                            when (type) {
                                "gzip" -> manager.setCompression(CompressType.Gzip, map[key]!!)
                                "deflate" -> manager.setCompression(CompressType.Deflate, map[key]!!)
                                "brotli" -> manager.setCompression(CompressType.Brotli, map[key]!!)
                                "zstd" -> manager.setCompression(CompressType.Zstd, map[key]!!)
                            }
                        }
                    }
                }

                // 매니저 등록
                registerManager(host, manager)
            }
            val server = HttpServer.create(InetSocketAddress(port), 0)
            server.executor = Executors.newVirtualThreadPerTaskExecutor()
            server.createContext("/", Handler(managers))
            server.start()
            this.server = server
            println("${AnsiText.BOLD}${AnsiText.GREEN}[Melon Framework] 서버가 성공적으로 구동되었습니다, ${addressList.distinct().joinToString(" | ")} 으(로) 접속할 수 있습니다.${AnsiText.RESET}")
        } catch (exception: BindException) {
            println("${AnsiText.BOLD}${AnsiText.RED}[Melon Framework: 오류] ${port} 포트를 이미 사용하고 있습니다.${AnsiText.RESET}")
            return this
        } catch (exception: PortUnreachableException) {
            println("${AnsiText.BOLD}${AnsiText.RED}[Melon Framework: 오류] ${port} 포트는 사용할 수 없습니다.${AnsiText.RESET}")
            return this
        }
        return this
    }

    fun stop() {
        server?.stop(0)
    }
}