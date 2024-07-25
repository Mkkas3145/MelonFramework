package util.compress

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class Gzip(private val content: ByteArray) : Compress() {
    override fun encoding(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutputStream ->
            gzipOutputStream.write(content)
        }
        return byteArrayOutputStream.toByteArray()
    }
}