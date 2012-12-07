import jsonblob.ApiController
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes

class BootStrap {

    def jsonBlobResourceService

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
        def demoObjectId = newBlob["_id"].toString()
        log.info("Created demo blob with demoObjectId=$demoObjectId")

        jsonBlobResourceService.apiDemoObjectId = demoObjectId
    }

    def destroy = {
        log.info("Removing demo blob with demoObjectId=$jsonBlobResourceService.apiDemoObjectId")
        jsonBlobResourceService.delete(jsonBlobResourceService.apiDemoObjectId)
    }
}
