package jsonblob

class BlobController {

    def jsonBlobResourceService
    def jsonService

    def load() {
        def jsonBlobId = params.get("id")
        try {
           def blob = jsonBlobResourceService.read(jsonBlobId);
           def id = blob["_id"]
           blob.remove("_id")
           def json = jsonService.objectMapper.writeValueAsString(blob)
           render(view: '../index', model: [blob: json, blobId: id])
        } catch (Exception exception) {
            log.error("Couldn't load object with id $jsonBlobId", exception)
            redirect(uri: "/")
        }
    }

}
