package jsonblob.core.store.s3

import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jsonblob.config.JsonBlobConfig
import jsonblob.config.S3JsonBlobStoreConfig
import jsonblob.core.compression.BlobCompressorPicker
import jsonblob.core.compression.compressor.BlobCompressor
import jsonblob.core.id.IdHandler
import jsonblob.core.store.JsonBlobStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.ExpirationStatus
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.LifecycleExpiration
import software.amazon.awssdk.services.s3.model.LifecycleRule
import software.amazon.awssdk.services.s3.model.LifecycleRuleAndOperator
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.Tag
import software.amazon.awssdk.services.s3.model.Tagging
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Singleton
@Requires(beans = [S3JsonBlobStoreConfig::class])
open class S3JsonBlobStore(
    idResolvers: List<IdHandler<*>>,
    compressorPicker: BlobCompressorPicker,
    private val s3: S3Client,
    private val config: JsonBlobConfig,
    private val s3JsonBlobStoreConfig: S3JsonBlobStoreConfig,
    private val blobCompressors: List<BlobCompressor>,
    private val eventPublisher: ApplicationEventPublisher,
) : JsonBlobStore(s3JsonBlobStoreConfig.basePath, idResolvers, compressorPicker) {
    private val lastAccessedFormat = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("UTC"))
    private val lastAccessedTag = "last-accessed"
    private val expirationTag = "expire-after"
    private val typeTag = "type"
    private val jsonBlobTypeValue = "json-blob"

    init {
        if (s3JsonBlobStoreConfig.setupLifecycle) {
            val bucketPolicyId = "$lastAccessedTag-lifecycle-policy-${config.deleteAfter}"
            log.info { "Setting up Lifecycle Policy for expiring objects with prefix ${s3JsonBlobStoreConfig.basePath} after ${config.deleteAfter} with id $bucketPolicyId" }
            s3.putBucketLifecycleConfiguration(
                PutBucketLifecycleConfigurationRequest.builder()
                    .bucket("json-blobs-dev")
                    .lifecycleConfiguration(
                        BucketLifecycleConfiguration.builder()
                            .rules(
                                LifecycleRule.builder()
                                    .expiration(
                                        LifecycleExpiration.builder()
                                            .days(config.deleteAfter.toDays().toInt())
                                            .build()
                                    )
                                    .id(bucketPolicyId)
                                    .filter(
                                        LifecycleRuleFilter.builder()
                                            .and(
                                                LifecycleRuleAndOperator.builder()
                                                    .prefix(s3JsonBlobStoreConfig.basePath)
                                                    .tags(
                                                        jsonBlobTag(),
                                                        expirationTag()
                                                    )
                                                    .build()
                                            ).build()
                                    )
                                    .status(ExpirationStatus.ENABLED)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            log.info { "Completed setting up Lifecycle Policy for expiring objects with id $bucketPolicyId" }
        }
    }

    override fun retrieve(id: String): InputStream? {
        return kotlin.runCatching {
            val latestObjectKey = lastModifiedS3Blob(id)
            val compression = latestObjectKey.substringAfterLast(".")
            val compressor = blobCompressors.find { it.handles(compression) }
                ?: throw IllegalStateException("No Blob compressor for .$compression")

            log.debug { "Reading blob from $latestObjectKey using ${compressor::class.simpleName}" }

            return compressor.getInputStream(
                s3.getObject(
                    GetObjectRequest.builder()
                        .bucket(s3JsonBlobStoreConfig.bucket)
                        .key(latestObjectKey)
                        .build(),
                    ResponseTransformer.toBytes()
                ).also {
                    log.debug { "Read blob from $latestObjectKey" }
                    if (s3JsonBlobStoreConfig.copyToResetLastModified) {
                        eventPublisher.publishEventAsync(S3JsonBlobAccessedEvent(latestObjectKey))
                    }
                }.asInputStream()
            )
        }.onFailure {
            log.warn { "Couldn't retrieve blobId=$id from S3" }
        }.getOrNull()
    }

    override fun store(id: String, json: String, compressor: BlobCompressor): Boolean {
        return kotlin.runCatching {
            val key = getKey(id, compressor)

            log.debug { "Storing blob to $key using ${compressor::class.simpleName}" }

            val byteArrayOutputStream = ByteArrayOutputStream().apply {
                DataOutputStream(compressor.getOutputStream(this)).use {
                    it.write(json.toByteArray())
                }
            }

            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(s3JsonBlobStoreConfig.bucket)
                    .key(key)
                    .tagging(objectTags())
                    .build(),
                RequestBody.fromBytes(byteArrayOutputStream.toByteArray())
            )
            return true.apply {
                log.debug { "Stored blob to $key" }
            }
        }.onFailure {
            log.warn(it) { "Couldn't store blobId=$id from S3" }
        }.getOrElse { false }
    }

    override fun remove(id: String): Boolean {
        return kotlin.runCatching {
            s3Blobs(id)
                .map { it.first }
                .forEach { blob ->
                    s3.deleteObject {
                        it.bucket(
                            s3JsonBlobStoreConfig.bucket
                        ).key(blob)
                }
            }
            return true
        }.getOrElse { false }
    }

    override fun path(id: String) = kotlin.runCatching {
        lastModifiedS3Blob(id)
    }.getOrElse { null }

    @EventListener
    @Async
    open fun onAccessedEvent(event: S3JsonBlobAccessedEvent) {
        val now = lastAccessedFormat.format(Instant.now())
        val tags = s3.getObjectTagging {
            it
                .bucket(s3JsonBlobStoreConfig.bucket)
                .key(event.s3ObjectKey)
        }.tagSet()
        if (tags.isEmpty() || tags.filter { it.key() == lastAccessedTag }.filterNot { it.value() == now }.any()) {
            kotlin.runCatching {
                log.info { "Copying ${s3JsonBlobStoreConfig.bucket}/${event.s3ObjectKey} to reset the timestamp on the object" }
                s3.copyObject(
                    CopyObjectRequest.builder()
                        .copySource("${s3JsonBlobStoreConfig.bucket}/${event.s3ObjectKey}")
                        .destinationBucket(s3JsonBlobStoreConfig.bucket)
                        .destinationKey(event.s3ObjectKey)
                        .tagging(objectTags())
                        .build()
                )
                log.info { "Copied ${event.s3ObjectKey}" }
            }.onFailure {
                log.warn { "Couldn't copy ${s3JsonBlobStoreConfig.bucket}/${event.s3ObjectKey}" }
            }
        }
    }

    private fun lastModifiedS3Blob(id: String) = s3Blobs(id).maxByOrNull { it.second.lastModified() }!!.first

    private fun objectTags() =
        Tagging.builder()
            .tagSet(listOf(
                lastAccessedTag(),
                expirationTag(),
                jsonBlobTag()
            )).build()

    private fun lastAccessedTag() = tagOf(lastAccessedTag, lastAccessedFormat.format(Instant.now()))

    private fun jsonBlobTag() = tagOf(typeTag, jsonBlobTypeValue)

    private fun expirationTag() = tagOf(expirationTag, config.deleteAfter.toString())

    private fun tagOf(key: String, value: String) = Tag.builder().key(key).value(value).build()

    private fun getKey(id: String, compressor: BlobCompressor) =
        "${getPrefix(resolveTimestamp(id))}/$id.${compressor.getFileExtension()}"

    private fun s3Blobs(id: String) : List<Pair<String, HeadObjectResponse>> {
        return runBlocking {
            blobCompressors.associateBy {
                getKey(id, it)
            }.map {
                async {
                    kotlin.runCatching {
                        it.key to s3.headObject(
                            HeadObjectRequest.builder()
                                .bucket(s3JsonBlobStoreConfig.bucket)
                                .key(getKey(id, it.value))
                                .build()
                        )
                    }.getOrNull()
                }
            }.awaitAll().filterNotNull()
        }
    }

}

data class S3JsonBlobAccessedEvent(val s3ObjectKey: String)