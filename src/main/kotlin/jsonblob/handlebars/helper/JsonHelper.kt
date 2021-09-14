package jsonblob.handlebars.helper

import com.github.jknack.handlebars.Options
import jsonblob.util.JsonCleaner
import javax.inject.Singleton

@Singleton
class JsonHelper: NamedHelper<String> {
    override fun getName() = "json"

    override fun apply(context: String, options: Options) = JsonCleaner.removeWhiteSpace(context)
}