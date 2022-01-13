package jsonblob.util

import io.micronaut.core.annotation.Order
import io.micronaut.core.async.publisher.Publishers
import io.micronaut.core.order.Ordered
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import org.reactivestreams.Publisher


@Filter(Filter.MATCH_ALL_PATTERN)
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorsAdvice: HttpServerFilter {
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain): Publisher<MutableHttpResponse<*>>? {
        val reqHead = request.headers.get("Access-Control-Request-Headers")
        return Publishers.map(
            chain.proceed(request)
        ) { response: MutableHttpResponse<*> ->
            response.headers(
                mutableMapOf(
                    "Access-Control-Allow-Methods" to "GET,POST,PUT,DELETE,HEAD,OPTIONS",
                    "Access-Control-Allow-Origin" to "*",
                    "Access-Control-Expose-Headers" to "X-Requested-With,X-jsonblob,Location,Date,Content-Type,Accept,Origin"
                ).apply {
                    if (!reqHead.isNullOrEmpty()) {
                        put("Access-Control-Allow-Headers", reqHead)
                    }
                }.toMap()
            )
        }
    }
}