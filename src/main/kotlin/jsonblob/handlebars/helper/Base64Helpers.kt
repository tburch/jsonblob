package jsonblob.handlebars.helper

import com.github.jknack.handlebars.Options
import org.apache.commons.codec.binary.Base64
import java.nio.charset.StandardCharsets
import javax.inject.Singleton

@Singleton
class Base64EncodeHelper : NamedHelper<String> {
    override fun getName() = "base64Encode"

    override fun apply(context: String, options: Options): String {
        val urlSafe: Boolean = options.hash("urlSafe", java.lang.Boolean.FALSE)
        return if (urlSafe) {
            Base64.encodeBase64URLSafeString(context.toByteArray(StandardCharsets.UTF_8))
        } else {
            Base64.encodeBase64String(context.toByteArray(StandardCharsets.UTF_8))
        }
    }
}

@Singleton
class Base64DecodeHelper: NamedHelper<String> {
    override fun getName() = "base64Decode"

    override fun apply(context: String, options: Options): Any {
        val urlSafe: Boolean = options.hash("urlSafe", java.lang.Boolean.FALSE)
        return if (urlSafe) {
            Base64.encodeBase64URLSafe(context.toByteArray(StandardCharsets.UTF_8))
        } else {
            Base64.decodeBase64(context.toByteArray(StandardCharsets.UTF_8))
        }
    }

}