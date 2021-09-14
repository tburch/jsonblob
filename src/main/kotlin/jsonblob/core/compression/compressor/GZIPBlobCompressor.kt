package jsonblob.core.compression.compressor

import io.micronaut.context.annotation.Secondary
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Singleton

@Singleton
@Secondary
class GZIPBlobCompressor : NoCompressionJsonBlobCompressor() {
    companion object {
        const val fileExtension = "gz"
    }

    override fun handles(fileExtension: String) = fileExtension.endsWith(Companion.fileExtension)

    override fun getFileExtension() = listOf(super.getFileExtension(), fileExtension).joinToString(separator = ".")

    override fun getInputStream(inputStream: InputStream) = GZIPInputStream(inputStream)

    override fun getOutputStream(outputStream: OutputStream) = GZIPOutputStream(outputStream)
}