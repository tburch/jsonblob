package jsonblob.core.compression.compressor

import io.micronaut.core.annotation.Order
import io.micronaut.core.order.Ordered
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Order(Ordered.LOWEST_PRECEDENCE)
@Singleton
open class NoCompressionJsonBlobCompressor : BlobCompressor {
    companion object {
        const val fileExtension = "json"
    }

    override fun getFileExtension() = fileExtension

    override fun handles(fileExtension: String) = fileExtension == getFileExtension()

    override fun getInputStream(inputStream: InputStream) = inputStream

    override fun getOutputStream(outputStream: OutputStream) = outputStream
}