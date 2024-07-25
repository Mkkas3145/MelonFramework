package server

import org.json.JSONObject

class Response {
    private var statusCode = 500
    private val headers = mutableMapOf<String, Any>()
    private var body: ByteArray = byteArrayOf()

    fun getStatusCode(): Int {
        return statusCode
    }
    fun setStatusCode(statusCode: Int): Response {
        this.statusCode = statusCode
        return this
    }

    fun isHeader(key: String): Boolean {
        return headers[key] != null
    }
    fun getHeaders(): Map<String, Any> {
        return headers
    }
    fun getHeader(key: String): Any? {
        return headers[key]
    }
    fun getHeaderOrDefault(key: String, default: Any): Any {
        if (isHeader(key)) {
            return getHeader(key)!!
        }
        return default
    }
    fun setHeader(key: String, value: Any): Response {
        headers[key] = value
        return this
    }

    fun getBody(): ByteArray {
        return body
    }
    fun setBody(body: String): Response {
        this.body = body.toByteArray()
        this.setHeader("Content-Type", "text/plain; charset=utf-8")
        return this
    }
    fun setBody(body: JSONObject): Response {
        this.body = body.toString().toByteArray()
        this.setHeader("Content-Type", "application/json; charset=utf-8")
        return this
    }
    fun setBody(body: ByteArray): Response {
        this.body = body
        return this
    }
}