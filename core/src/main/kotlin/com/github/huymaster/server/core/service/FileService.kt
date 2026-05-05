package com.github.huymaster.server.core.service

import com.github.huymaster.server.core.utils.DirectMemoryPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FileService : BaseService() {
    private val io = CoroutineScope(Dispatchers.IO)
    private val pool = DirectMemoryPool.createProvider(16, 64, "FileService")

    init {
        minio.initialize("file")
    }
}