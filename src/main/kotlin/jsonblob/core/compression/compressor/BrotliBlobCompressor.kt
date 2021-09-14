package jsonblob.core.compression.compressor

import com.nixxcode.jvmbrotli.dec.BrotliInputStream
import com.nixxcode.jvmbrotli.enc.BrotliOutputStream
import com.nixxcode.jvmbrotli.enc.Encoder
import jsonblob.config.BrotliBlobCompressorConfig
import mu.KotlinLogging
import java.io.InputStream
import java.io.OutputStream


private val log = KotlinLogging.logger {}

class BrotliBlobCompressor(config: BrotliBlobCompressorConfig) : NoCompressionJsonBlobCompressor() {
    companion object {
        const val fileExtension = "br"
    }

    init {
        log.info { "Using quality of ${config.quality}" }
    }

    private val params = Encoder.Parameters().setQuality(config.quality)

    override fun handles(fileExtension: String) = fileExtension.endsWith(Companion.fileExtension)

    override fun getFileExtension() = listOf(super.getFileExtension(), fileExtension).joinToString(separator = ".")

    override fun getInputStream(inputStream: InputStream) = BrotliInputStream(inputStream)

    override fun getOutputStream(outputStream: OutputStream) = BrotliOutputStream(outputStream, params)
}