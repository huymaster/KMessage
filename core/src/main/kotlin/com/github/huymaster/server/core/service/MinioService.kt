package com.github.huymaster.server.core.service

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient

class MinioService(
    client: MinioClient
) : Service, MinioClient(client) {
    var bucketName: String = "default"
        private set
    private var isInitialized = false

    fun initialize(bucketName: String = "default") {
        if (isInitialized) return
        val result = runCatching {
            setupBucket(bucketName)
        }
        result.onSuccess {
            this.bucketName = bucketName
            isInitialized = true
        }
        result.onFailure { throw it }
    }

    private fun setupBucket(bucketName: String) {
        require(bucketName.isNotBlank()) { "Bucket name is blank" }
        val bea = BucketExistsArgs.builder()
            .bucket(bucketName)
            .build()

        val exists = bucketExists(bea)
        if (exists) return

        val mba = MakeBucketArgs.builder()
            .bucket(bucketName)
            .build()

        makeBucket(mba)
    }

    override fun toString(): String {
        return "MinioService[bucket = '$bucketName']"
    }
}