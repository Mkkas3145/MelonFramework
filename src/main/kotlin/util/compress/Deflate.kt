package util.compress

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

class Deflate(private val content: ByteArray) : Compress() {
    override fun encoding(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        DeflaterOutputStream(byteArrayOutputStream, deflater).use { deflaterOutputStream ->
            deflaterOutputStream.write(content)
        }
        return byteArrayOutputStream.toByteArray()
    }
}