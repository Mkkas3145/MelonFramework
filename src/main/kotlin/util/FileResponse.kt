package util

import java.io.File

class FileResponse(private val file: File, private val content: ByteArray, private val mimeType: String) {
    fun getFile(): File {
        return file
    }

    fun getContent(): ByteArray {
        return content
    }

    fun getMimeType(): String {
        return mimeType
    }
}