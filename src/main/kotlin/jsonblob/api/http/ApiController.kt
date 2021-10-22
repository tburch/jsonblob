package jsonblob.api.http

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.http.server.util.HttpHostResolver
import io.micronaut.views.View
import io.micronaut.web.router.RouteBuilder
import jsonblob.api.http.view.ApiView
import jsonblob.config.GoogleAnalyticsConfig
import jsonblob.config.JsonBlobConfig
import jsonblob.core.id.IdHandler
import jsonblob.core.store.JsonBlobStore
import jsonblob.model.JsonBlob
import jsonblob.util.JsonCleaner
import mu.KotlinLogging
import java.net.URI


private val log = KotlinLogging.logger {}

@Controller("/${ApiController.apiPath}")
class ApiController(
    private val jsonBlobConfig: JsonBlobConfig,
    private val idResolvers: List<IdHandler<*>>,
    private val idGenerator: IdHandler<*>,
    private val jsonBlobStore: JsonBlobStore,
    private val httpHostResolver: HttpHostResolver,
    private val uriNamingStrategy: RouteBuilder.UriNamingStrategy,
    private val googleAnalyticsConfig: GoogleAnalyticsConfig
) {
    companion object {
        const val apiPath = "api"
        const val jsonBlobPath = "jsonBlob"
        const val jsonBlobHeader = "X-jsonblob"
    }

    @Get
    @View("api")
    fun apiView() = ApiView(
        googleAnalyticsConfig.webPropertyId,
        "api",
        googleAnalyticsConfig.customTrackingCodes
    )

    @Post("/$jsonBlobPath")
    @Produces(MediaType.APPLICATION_JSON)
    fun createBlob(@Body json: String, httpRequest: HttpRequest<Any>): HttpResponse<String> {
        if (JsonCleaner.validJson(json)) {
            val jsonBlob = JsonBlob(
                id = idGenerator.generate(),
                json = json
            )
            val blob = jsonBlobStore.write(jsonBlob)
            val host = httpHostResolver.resolve(httpRequest) + uriNamingStrategy.resolveUri("/api/jsonBlob/${blob.id}")
            return HttpResponse.created(blob.json, URI.create(host))
        } else {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON")
        }
    }

    @Get("/$jsonBlobPath/{blobId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getBlob(@PathVariable blobId: String) = readJsonBlob(blobId)?.json
        ?: throw HttpStatusException(HttpStatus.NOT_FOUND, "Blob with id $blobId does not exist")

    @Get("/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCustomPath(@PathVariable path: String, @Header(jsonBlobHeader) jsonbBlobHeader: String?): String {
        if (jsonbBlobHeader != null) {
            val headerBlob = readJsonBlob(jsonbBlobHeader)
            if (headerBlob != null) {
                return headerBlob.json
            }
        }
        return readFirstBlobFromPath(path)?.json ?: throw HttpStatusException(
            HttpStatus.NOT_FOUND,
            "Blob does not exist"
        )
    }

    @Put("/$jsonBlobPath/{blobId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updateBlob(@PathVariable blobId: String, @Body json: String) =
        update(blobId, json)?.json ?: throw HttpStatusException(
            HttpStatus.NOT_FOUND,
            "Blob with id $blobId does not exist"
        )

    @Put("/{path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updateCustomPathOrHeader(
        @PathVariable path: String,
        @Header(jsonBlobHeader) jsonbBlobHeader: String?,
        @Body json: String
    ): String {
        if (jsonbBlobHeader != null) {
            val headerBlob = update(jsonbBlobHeader, json)
            if (headerBlob != null) {
                return headerBlob.json
            }
        }
        return updateFirstBlobFromPath(path, json)?.json ?: throw HttpStatusException(
            HttpStatus.NOT_FOUND,
            "Blob does not exist"
        )
    }

    @Delete("/$jsonBlobPath/{blobId}")
    fun deleteBlob(@PathVariable blobId: String): HttpStatus {
        if (!jsonBlobConfig.deleteEnabled) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Delete is not enabled")
        }
        return if (delete(blobId)) {
            HttpStatus.OK
        } else {
            throw HttpStatusException(HttpStatus.NOT_FOUND, "Blob with id $blobId does not exist")
        }
    }

    @Delete("/{path:.*}")
    fun deleteCustomPathOrHeader(
        @PathVariable path: String,
        @Header(jsonBlobHeader) jsonbBlobHeader: String?
    ): HttpStatus {
        if (!jsonBlobConfig.deleteEnabled) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Delete is not enabled")
        }
        if (jsonbBlobHeader != null) {
            if (delete(jsonbBlobHeader)) {
                return HttpStatus.OK
            }
        }
        return if (deleteFirstBlobFromPath(path)) {
            HttpStatus.OK
        } else {
            throw HttpStatusException(HttpStatus.NOT_FOUND, "Blob does not exist")
        }
    }

    private fun deleteFirstBlobFromPath(path: String): Boolean {
        val ids = blobIdsFromPath(path)
        if (ids.isNotEmpty()) {
            return delete(ids.first())
        }
        return false
    }

    private fun delete(blobId: String) = jsonBlobStore.remove(blobId)

    private fun updateFirstBlobFromPath(path: String, json: String): JsonBlob? {
        val ids = blobIdsFromPath(path)
        return if (ids.isNotEmpty()) {
            update(ids.first(), json)
        } else {
            null
        }
    }

    private fun update(blobId: String, json: String): JsonBlob? {
        val resolver = idResolvers.firstOrNull { it.handles(blobId) }
        return if (resolver != null) {
            val created = resolver.resolveTimestamp(blobId)
            val jsonBlob = JsonBlob(
                id = blobId,
                json = json,
                created = created
            )
            jsonBlobStore.write(jsonBlob)
        } else {
            null
        }
    }

    private fun readFirstBlobFromPath(path: String): JsonBlob? {
        val ids = blobIdsFromPath(path)
        return if (ids.isNotEmpty()) {
            readJsonBlob(ids.first())
        } else {
            null
        }
    }

    private fun readJsonBlob(blobId: String) = jsonBlobStore.read(blobId)

    private fun blobIdsFromPath(path: String) = path
        .split("/")
        .mapNotNull { pathPart ->
            if (idResolvers.any { it.handles(pathPart) }) {
                pathPart
            } else {
                null
            }
        }
}