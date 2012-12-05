package jsonblob

import org.grails.jaxrs.provider.DomainObjectNotFoundException

class BlobController {

    def jsonBlobResourceService
    def jsonService

    def load() {
        def jsonBlobId = params.get("id")
        def json = ""
        try {
           def blob = jsonBlobResourceService.read(jsonBlobId);
           blob.remove("_id")
           json = jsonService.objectMapper.writeValueAsString(blob)
        } catch (Exception exception) {
            log.error("Couldn't load object with id $jsonBlobId", exception)
            redirect(uri: "/")
        }
        [blob: json]
    }

}
