package jsonblob

class BlobController {

    def jsonBlobResourceService

    def load() {
        def jsonBlobId = params.get("id")
        try {
            def blob = jsonBlobResourceService.read(jsonBlobId);
            if (blob) {
                def id = blob["_id"]
                render(view: '../editor', model: [blobId: id])
            } else {
                response.status = 404
            }
        } catch (Exception e) {
            log.error("Couldn't load blob with id=$jsonBlobId")
            response.status = 404
        }
    }

}
