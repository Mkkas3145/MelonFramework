package server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import util.FileManager
import util.JarLoader
import util.compress.*

class Handler(private val managers: Map<String, Manager>) : HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        val processStartTime = System.currentTimeMillis();

        val request = Request(exchange!!)
        val host = request.getHost()
        val port = request.getPort()
        val uri = request.getURI()
        exchange.setAttribute("uri", null)

        // 매니저 참조
        val manager = managers.getOrDefault(host, managers["*"]) ?: return

        // 콘텐츠 압축
        var compressType: CompressType? = null
        var headerCompressType: String? = null
        if (exchange.requestHeaders.containsKey("Accept-Encoding")) {
            val acceptEncoding = exchange.requestHeaders["Accept-Encoding"]?.toString()?.let { it ->
                it.substring(1, it.length - 1).split(",").map { it.trim() }
            }!!
            if (manager.isCompress(CompressType.Zstd)!! && "zstd" in acceptEncoding) {
                compressType = CompressType.Zstd
                headerCompressType = "zstd"
            } else if (manager.isCompress(CompressType.Brotli)!! && "br" in acceptEncoding) {
                compressType = CompressType.Brotli
                headerCompressType = "br"
            } else if (manager.isCompress(CompressType.Deflate)!! && "deflate" in acceptEncoding) {
                compressType = CompressType.Deflate
                headerCompressType = "deflate"
            } else if (manager.isCompress(CompressType.Gzip)!! && "gzip" in acceptEncoding) {
                compressType = CompressType.Gzip
                headerCompressType = "gzip"
            }
        }

        // 응답 인스턴스 참조
        var response: Response
        val cacheKey = "${host}:${port}|" + uri + "|" + (compressType?.toString()?.lowercase() ?: "none")
        if (Cache.isExist(cacheKey)) {
            response = Cache.getResponse(cacheKey)!!
        } else {
            response = Response()
            val processType = manager.getProcessType()

            try {
                // 정적
                if (processType == ProcessType.Static) {
                    val fileResponse = FileManager.getFileResponse("${manager.getRoot()}\\${uri}")
                    response.setBody(fileResponse.getContent())
                    response.setHeader("Content-Type", "${fileResponse.getMimeType()}; charset=utf-8")
                    // 상태 코드를 200으(로) 변경한다
                    if (fileResponse.getFile().isFile && fileResponse.getFile().exists()) {
                        response.setStatusCode(200)
                    } else {
                        if (manager.getFallback() == null || uri == manager.getFallback()) {
                            response.setStatusCode(404);
                        } else {
                            exchange.setAttribute("uri", manager.getFallback());
                            handle(exchange);
                            return;
                        }
                    }
                }

                // 동적
                if (processType == ProcessType.Dynamic) {
                    response.setStatusCode(404)
                    val files = FileManager.getFiles(manager.getRoot())
                    for (file in files) {
                        val instances = JarLoader.getInstances(file)
                        if (instances != null) {
                            for (instance in instances) {
                                val methods = instance::class.java.methods
                                methods.forEach { method ->
                                    val annotation = method.getAnnotation(HandlerEvent::class.java)
                                    if (annotation != null) {
                                        if (FileManager.simplifyPath(annotation.uri) == FileManager.simplifyPath(uri)) {
                                            try {
                                                response.setStatusCode(200)
                                                method.invoke(instance, Request(exchange), response)
                                            } catch (_: Exception) {
                                                response.setStatusCode(500)
                                            }
                                        }
                                    }
                                    val anotation2 = method.getAnnotation(PostHandlerEvent::class.java)
                                    if (anotation2 != null) {
                                        if (anotation2.status == response.getStatusCode()) {
                                            try {
                                                response.setStatusCode(200)
                                                method.invoke(instance, request, response)
                                            } catch (_: Exception) {
                                                response.setStatusCode(500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            // 콘텐츠 압축
            if (compressType != null) {
                if (compressType == CompressType.Gzip) {
                    response.setBody(Gzip(response.getBody()).encoding())
                } else if (compressType == CompressType.Deflate) {
                    response.setBody(Deflate(response.getBody()).encoding())
                } else if (compressType == CompressType.Brotli) {
                    response.setBody(Brotli(response.getBody()).encoding())
                } else {
                    response.setBody(Zstd(response.getBody()).encoding())
                }
                response.setHeader("Content-Encoding", headerCompressType!!)
            }

            // 캐시 등록
            if (processType == ProcessType.Static && manager.isCache()) {
                Cache.register(cacheKey, response)   
            }
        }

        // 처리 시간
        val processEndTime = System.currentTimeMillis()
        response.setHeader("X-Response-Time", "${processEndTime - processStartTime}ms")
        
        // 서버 이름
        response.setHeader("Server", "Melon Framework")

        // 최종 응답
        for (header in response.getHeaders()) {
            exchange.responseHeaders.set(header.key, header.value.toString())
        }
        exchange.sendResponseHeaders(response.getStatusCode(), response.getBody().size.toLong())
        exchange.responseBody.write(response.getBody())
        exchange.responseBody.close()
    }
}
