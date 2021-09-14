package jsonblob.config

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.views.handlebars.HandlebarsFactory
import jsonblob.handlebars.helper.NamedHelper
import mu.KotlinLogging
import javax.inject.Singleton


private val log = KotlinLogging.logger {}

@Factory
class HandlebarsWithHelpersFactory {
    @Singleton
    @Replaces(factory = HandlebarsFactory::class)
    fun handlebarsWithHandlers(helpers: Collection<NamedHelper<*>>) : Handlebars {
        val handlebars = Handlebars().with(ConcurrentMapTemplateCache())
        log.info { "Adding Helpers to Handlebars" }
        helpers.forEach {
            handlebars.registerHelper(it.getName(), it)
        }
        log.info { "Competed adding Helpers to Handlebars" }
        return handlebars
    }
}