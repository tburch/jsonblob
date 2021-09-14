package jsonblob.core.id

import java.time.Instant

abstract class IdHandler<T> {
    abstract fun idFrom(t: T) : String
    abstract fun to(id: String) : T
    abstract fun resolveTimestamp(t: T) : Instant
    abstract fun generate(): String
    open fun handles(id: String) = kotlin.runCatching {
        to(id)
        true
    }.getOrDefault(false)
    open fun resolveTimestamp(id: String) = resolveTimestamp(to(id))
}