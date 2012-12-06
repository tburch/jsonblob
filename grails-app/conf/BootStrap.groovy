class BootStrap {

    def jsonBlobResourceService

    def demoObjectId

    def init = { servletContext ->
        def jsonBuilder = new groovy.json.JsonBuilder()
        jsonBuilder.tools {
            jsbin {
                url 'http://jsbin.com/'
                inception 2009
            }
            jsoneditoronline {
                url 'http://jsoneditoronline.org/'
                inception 2011
            }
            jsonblob {
                url 'http://jsonblob.com/'
                inception 2012
            }
        }
        def newBlob = jsonBlobResourceService.create(jsonBuilder.toString())
        this.demoObjectId = newBlob["_id"].toString()
        log.info("Created demo blob with demoObjectId=$demoObjectId")
    }

    def destroy = {
        log.info("Removing demo blob with demoObjectId=$demoObjectId")
        jsonBlobResourceService.delete(demoObjectId)
    }
}
