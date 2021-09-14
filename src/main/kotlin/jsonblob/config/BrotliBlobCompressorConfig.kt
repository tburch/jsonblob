package jsonblob.config

import com.nixxcode.jvmbrotli.common.BrotliLoader
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.exceptions.DisabledBeanException
import io.micronaut.core.annotation.Order
import io.micronaut.core.order.Ordered
import jsonblob.core.compression.compressor.BrotliBlobCompressor
import mu.KotlinLogging
import javax.inject.Singleton
import javax.validation.constraints.Max
import javax.validation.constraints.Min


private val log = KotlinLogging.logger {}

@ConfigurationProperties("brotli")
class BrotliBlobCompressorConfig {
    @get:Min(-1)
    @get:Max(11)
    var quality = -1
}

@Factory
class BrotliBlobCompressorFactory {
    @Primary
    @Singleton
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun brotliBlobCompressor(brotliBlobCompressorConfig: BrotliBlobCompressorConfig): BrotliBlobCompressor {
        if (BrotliLoader.isBrotliAvailable()) {
            return BrotliBlobCompressor(brotliBlobCompressorConfig)
        } else {
            log.warn { "Brotli is not available" }
            throw DisabledBeanException("Brotli is not available")
        }
    }
}
