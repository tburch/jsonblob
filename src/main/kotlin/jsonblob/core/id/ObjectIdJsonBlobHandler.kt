package jsonblob.core.id

import io.micronaut.core.annotation.Order
import org.bson.types.ObjectId
import java.time.Instant
import javax.inject.Singleton


@Singleton
@Order(1)
class ObjectIdJsonBlobHandler : IdHandler<ObjectId>() {
    override fun resolveTimestamp(t: ObjectId): Instant = Instant.ofEpochSecond(t.timestamp.toLong())

    override fun idFrom(t: ObjectId): String = t.toHexString()

    override fun to(id: String) = ObjectId(id)

    override fun generate(): String = ObjectId().toHexString()
}