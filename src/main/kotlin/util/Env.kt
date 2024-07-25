package util

import java.io.File
import java.io.FileInputStream
import java.util.*


class Env {
    companion object {
        private val values = mutableMapOf<Profile?, Map<Any, Any>>()

        fun load(profile: Profile?) {
            val file = getProfileToFile(profile)
            val properties = Properties()
            try {
                FileInputStream(file).use { fis ->
                    properties.load(fis)
                }
            } catch (_: Exception) {}
            values[profile] = properties.toMap()
        }

        fun getProfileToFile(profile: Profile?): File {
            var path = "${FileManager.getAbsolutePath()}/.env"
            if (profile != null) {
                path = "${FileManager.getAbsolutePath()}/.env.${profile.toString().lowercase()}"
            }
            return File(path)
        }

        fun getValue(key: String): Any? {
            val profile = Global.getProfile()
            if (values.containsKey(profile) && values[profile]!!.containsKey(key)) {
                return values[profile]!![key]
            }
            return values[null]?.get(key)
        }
    }
}