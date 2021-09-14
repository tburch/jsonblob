package jsonblob.handlebars.helper

import com.github.jknack.handlebars.Options
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
class InstantFormatter : NamedHelper<Instant> {
    override fun apply(context: Instant, options: Options): String {
        val format = options.params.first() as String
        return DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC")).format(context)
    }

    override fun getName() = "dateFormat"
}