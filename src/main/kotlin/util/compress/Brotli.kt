package util.compress

import com.aayushatharva.brotli4j.encoder.Encoder

class Brotli(private val content: ByteArray) : Compress() {
    override fun encoding(): ByteArray {
        val parameters = Encoder.Parameters().setQuality(11)
        return Encoder.compress(content, parameters)
    }
}