package util

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class FileManager {
    companion object {
        fun getAbsolutePath(): String {
            return File(this::class.java.protectionDomain.codeSource.location.toURI().path).parent
        }

        fun getFileResponse(path: String): FileResponse {
            val file = File("${getAbsolutePath()}\\${path}")
            var mimeType = Files.probeContentType(Paths.get(file.absolutePath))
            if (mimeType == null) {
                mimeType = ""
            }
            if (file.isFile && file.exists()) {
                val content = Files.readAllBytes(Paths.get(file.absolutePath))
                return FileResponse(file, content, mimeType)
            }
            return FileResponse(file, ByteArray(0), mimeType)
        }

        fun getFiles(path: String): ArrayList<File> {
            val files = ArrayList<File>()
            val folder = File("${getAbsolutePath()}\\${path}")
            if (folder.isDirectory) {
                folder.walk().forEach {
                    if (it.isFile) {
                        files.add(File(it.absolutePath))
                    }
                }
            }
            return files
        }

        fun simplifyPath(uri: String): String {
            val parts = uri.split("/").filter { it.isNotBlank() }
            return parts.joinToString("/")
        }
    }
}