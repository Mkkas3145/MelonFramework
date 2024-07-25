import server.Server
import util.*
import java.util.*
import kotlin.system.exitProcess

private val uuid = UUID.randomUUID()
fun getUUID(): UUID {
    return uuid
}

@Suppress("UNCHECKED_CAST")
fun main() {
    val servers = ArrayList<String>()
    val serverConfig = mutableMapOf<Int, Map<String, Map<String, Any>>>()
    val config = Config.toMap()

    for (key in config.keys) {
        if (key == ":global") {
            val global = config[key] as Map<String, Any>
            Global.setValue(getUUID().toString(), global)
        } else {
            val addressList = key.split(",")
            for (address in addressList) {
                val realAddress = address.trim()
                servers.add(realAddress)
                // 매니저 등록을 위한 Config 정보
                val hostAndPort = realAddress.split(":")
                val host = hostAndPort[0]
                val port = hostAndPort[1].toInt()
                val hostConfig = serverConfig.getOrDefault(port, mutableMapOf()).toMutableMap()
                hostConfig[host] = config[key] as Map<String, Any>
                serverConfig[port] = hostConfig
            }
        }
    }

    // .env 파일 로드
    Env.load(null)
    Env.load(Profile.Local)
    Env.load(Profile.Dev)
    Env.load(Profile.Stage)
    Env.load(Profile.Prod)

    val hostsByPort = mutableMapOf<Int, ArrayList<String>>()
    for (address in servers) {
        val hostAndPort = address.split(":")
        val host = hostAndPort[0]
        val port = hostAndPort[1].toInt()
        val hosts = hostsByPort.getOrDefault(port, ArrayList())
        hosts.add(host)
        hostsByPort[port] = hosts
    }

    val loadedServer = ArrayList<Server>()
    for (entry in hostsByPort) {
        val server = Server(entry.key, entry.value, serverConfig[entry.key]!!)
        server.start()
        loadedServer.add(server)
    }

    // stop, reload 명령어
    while (true) {
        val input = readlnOrNull()
        if (input.equals("stop", true)) {
            exitProcess(0)
        } else if (input.equals("reload", true)) {
            println("${AnsiText.BOLD}${AnsiText.YELLOW}[Melon Framework] 다시 시작 중...${AnsiText.RESET}")
            for (server in loadedServer) {
                server.stop()
            }
            main()
        } else {
            println("알 수 없는 명령어입니다.")
        }
    }
}