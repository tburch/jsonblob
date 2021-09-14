package jsonblob.core.store

import jsonblob.core.id.IdHandler
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.Temporal

abstract class JsonBlobBase(
    private val basePath: String,
    private val idResolvers: List<IdHandler<*>>
) {
    companion object {
        internal val directoryFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(ZoneId.from(ZoneOffset.UTC))
    }

    protected fun resolveTimestamp(id: String) = idResolvers.findLast { it.handles(id) }?.resolveTimestamp(id = id) ?: throw IllegalStateException("No Resolver for '$id'")

    protected fun getPrefix(timestamp: Temporal) = "$basePath/${directoryFormat.format(timestamp)}"
}