package server

import com.sun.net.httpserver.HttpExchange

class Request(exchange: HttpExchange) {
    private val host = exchange.requestHeaders.getFirst("Host")
    private val port = exchange.localAddress.port
    private val uri = (exchange.getAttribute("uri") ?: exchange.requestURI).toString()
    private val headers = exchange.requestHeaders
    private val protocol = exchange.protocol
    private val method = exchange.requestMethod
    private val clientIp = if (getHeader("X-Forwarded-For") != null) getHeader("X-Forwarded-For").toString() else exchange.remoteAddress.hostString

    fun getClientIp(): String {
        return clientIp
    }

    fun getMethod(): String {
        return method
    }

    fun getProtocol(): String {
        return protocol
    }

    fun getHeader(key: String): MutableList<String>? {
        return headers[key]
    }

    fun getHost(): String {
        return host
    }

    fun getPort(): Int {
        return port
    }

    fun getURI(): String {
        return uri
    }
}