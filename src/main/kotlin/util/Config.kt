package util

import util.exception.ConfigFileNotFoundException
import util.exception.ConfigSyntaxException
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class Config {
    companion object {
        fun getPath(): String {
            return "${FileManager.getAbsolutePath()}\\.config"
        }

        fun toText(): String {
            val path = getPath()
            if (File(getPath()).exists()) {
                return String(Files.readAllBytes(Paths.get(path)), Charsets.UTF_8)
            }
            throw ConfigFileNotFoundException()
        }

        fun toMap(): Map<String, Any> {
            try {
                val configLines = toText().lines()
                val configMap = mutableMapOf<String, Any>()
                val propertyStack = mutableListOf<MutableMap<String, Any>>()

                var currentHost: String? = null
                var currentProperties: MutableMap<String, Any>? = null

                configLines.forEach { line ->
                    val trimmedLine = line.trim()
                    when {
                        trimmedLine.isEmpty() -> { /* Skip empty lines */ }
                        trimmedLine.endsWith("{") -> {
                            val key = trimmedLine.removeSuffix("{").trim()
                            if (key.contains(":")) {
                                currentHost = key
                                currentProperties = mutableMapOf()
                                propertyStack.add(currentProperties!!)
                            } else if (currentProperties != null) {
                                val subMap = mutableMapOf<String, Any>()
                                currentProperties!![key] = subMap
                                propertyStack.add(subMap)
                                currentProperties = subMap
                            }
                        }
                        trimmedLine.endsWith("}") -> {
                            if (propertyStack.isNotEmpty()) {
                                val finishedProperties = propertyStack.removeAt(propertyStack.lastIndex)
                                if (propertyStack.isEmpty()) {
                                    configMap[currentHost!!] = finishedProperties
                                    currentHost = null
                                    currentProperties = null
                                } else {
                                    currentProperties = propertyStack.last()
                                }
                            }
                        }
                        else -> {
                            val (key, value) = trimmedLine.split(":").map { it.trim() }
                            if (currentProperties != null) {
                                currentProperties!![key] = when (value) {
                                    "true" -> true
                                    "false" -> false
                                    else -> value
                                }
                            }
                        }
                    }
                }

                return configMap
            } catch (exception: Exception) {
                throw ConfigSyntaxException()
            }
        }
    }
}