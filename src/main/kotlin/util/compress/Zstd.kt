package util.compress

import com.github.luben.zstd.Zstd

class Zstd(private val content: ByteArray) : Compress() {
    override fun encoding(): ByteArray {
        return Zstd.compress(content, 22)
    }
}