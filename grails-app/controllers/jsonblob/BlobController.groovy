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
           json = jsonService.objectMapper.writeValueAsString(blob)
        } catch (DomainObjectNotFoundException domainObjectNotFoundException) {
            redirect(uri: "/")
        }
        [blob: json]
    }

}
