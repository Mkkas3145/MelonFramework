package util

import io.github.classgraph.ClassGraph
import server.DynamicProcessor
import java.io.File

class JarLoader {
    companion object {
        private val loadedFiles = ArrayList<File>()
        private val loadedDynamicProcessor = mutableMapOf<File, DynamicProcessor>()
        private val loadedInstances = mutableMapOf<File, ArrayList<Any>>()

        fun register(files: ArrayList<File>): Companion {
            for (file in files) {
                if (file.extension == "jar" && !isLoaded(file)) {
                    // .jar 파일 동적으로 로드
                    ClassGraph().overrideClasspath(file).enableAllInfo().scan().use { scanResult ->
                        val loadedClasses = ArrayList<Any>()
                        for (classInfo in scanResult.allClasses) {
                            val clazz = scanResult.allClasses.get(classInfo.name)
                            val instance = clazz.loadClass().newInstance()
                            loadedClasses.add(instance)
                            // DynamicProcessor 오버라이드 함수 enable 실행
                            if (instance is DynamicProcessor) {
                                val dynamicProcessor = clazz.loadClass().newInstance() as DynamicProcessor
                                dynamicProcessor.enable()
                                loadedDynamicProcessor[file] = dynamicProcessor
                            }
                        }
                        this.loadedInstances[file] = loadedClasses
                    }
                }
            }
            return this
        }

        fun isLoaded(file: File): Boolean {
            for (loadedFile in loadedFiles) {
                if (loadedFile.absolutePath == file.absolutePath) {
                    return true
                }
            }
            return false
        }

        fun getInstances(file: File): ArrayList<Any>? {
            for (loadedInstance in loadedInstances) {
                if (loadedInstance.key.absolutePath == file.absolutePath) {
                    return loadedInstances[file]
                }
            }
            return null
        }

        fun getDynamicProcessor(file: File): DynamicProcessor? {
            for (loadedFile in loadedFiles) {
                if (loadedFile.absolutePath == file.absolutePath) {
                    return loadedDynamicProcessor[file]
                }
            }
            return null
        }
    }
}