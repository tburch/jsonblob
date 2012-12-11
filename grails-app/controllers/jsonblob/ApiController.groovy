package jsonblob

class ApiController {

    def jsonBlobResourceService

    def index() {
        [demoObjectId: jsonBlobResourceService.apiDemoObjectId]
    }


}
