package jsonblob.core.id

import com.fasterxml.uuid.Generators
import io.micronaut.core.annotation.Order
import java.time.Instant
import java.util.*
import javax.inject.Singleton

@Singleton
@Order(2)
class Type1UUIDJsonBlobHandler : IdHandler<UUID>() {
    override fun resolveTimestamp(t: UUID): Instant {
        val epochMillis = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            .apply {
                this.clear()
                this.set(1582, 9, 15, 0, 0, 0) // 9 = October
            }.time.time

        val time: Long = t.timestamp() / 10000L + epochMillis

        return Instant.ofEpochMilli(time)
    }

    override fun idFrom(t: UUID): String  = t.toString()

    override fun to(id: String): UUID = UUID.fromString(id)

    override fun generate() = Generators.timeBasedGenerator().generate().toString()
}