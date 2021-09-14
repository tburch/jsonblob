package jsonblob.core.id

import io.micronaut.context.annotation.Primary
import io.micronaut.core.annotation.Order
import jsonblob.core.Snowflake
import java.time.Instant
import javax.inject.Singleton

@Singleton
@Order(3)
@Primary
class SnowflakeJsonBlobHandler(
    private val snowflake: Snowflake
) : IdHandler<Long>() {
    override fun resolveTimestamp(t: Long): Instant {
        val snowflake = snowflake.parse(t)
        return Instant.ofEpochMilli(snowflake.first())
    }

    override fun generate() = snowflake.nextId().toString()

    override fun idFrom(t: Long): String = t.toString()

    override fun to(id: String) = id.toLong()
}