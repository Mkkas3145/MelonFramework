package server

class Cache {
    companion object {
        private val cache = mutableMapOf<String, Response>()
        fun register(key: String, response: Response): Companion {
            cache[key] = response
            return this
        }
        fun getResponse(key: String): Response? {
            return cache[key]
        }
        fun isExist(key: String): Boolean {
            return getResponse(key) != null
        }
    }
}