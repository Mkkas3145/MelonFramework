package server

import server.exception.IsCompressInvalidReferenceException
import util.compress.CompressType

class Manager {
    private var processType = ProcessType.Static
    private var root = ""
    private var fallback: String? = null
    private var cache = true

    fun isCache(): Boolean {
        return cache
    }
    fun setCache(cache: Boolean): Manager {
        this.cache = cache
        return this
    }

    fun getFallback(): String? {
        return fallback
    }
    fun setFallback(fallback: String): Manager {
        this.fallback = fallback
        return this
    }

    fun getRoot(): String {
        return root
    }
    fun setRoot(root: String): Manager {
        this.root = root
        return this
    }

    fun getProcessType(): ProcessType {
        return processType
    }
    fun setProcessType(processType: ProcessType): Manager {
        this.processType = processType
        return this
    }

    private val compressList: MutableMap<CompressType, Boolean> = mutableMapOf(
        CompressType.Gzip to true,
        CompressType.Deflate to true,
        CompressType.Brotli to true,
        CompressType.Zstd to true
    )

    fun setCompression(compressType: CompressType, isUse: Boolean): Manager {
        compressList[compressType] = isUse
        return this
    }

    fun isCompress(compressType: CompressType): Boolean? {
        if (compressList.containsKey(compressType)) {
            return compressList[compressType]
        }
        throw IsCompressInvalidReferenceException()
    }
}