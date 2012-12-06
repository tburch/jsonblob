package jsonblob

import org.apache.commons.io.IOUtils
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class FileController {

    static allowedMethods = [upload: 'POST']

    def upload() {
        def jsonFile = request.getFile('json')
        Reader reader = null
        String name = UUID.randomUUID().toString()
        if (!jsonFile.empty) {
            name = jsonFile.getOriginalFilename()
            reader = new StringReader(new String(jsonFile.getBytes()))
        }

        if (reader) {
            response.setContentType("application/json")
            response.setHeader("Content-disposition", "attachment; filename=\"${name}.json\"")
            response.outputStream << reader
            response.outputStream.flush()
        }
    }

    def fetch() {
        def jsonUrl = new URI(params.get("url"))
        if (jsonUrl) {
            def jsonResponse
            String name = jsonUrl.toString().bytes.encodeBase64().toString()
            def http = new HTTPBuilder(jsonUrl)
            http.request(GET, JSON) {
                response.success = { resp, json ->
                    jsonResponse = json.toString()
                }
            }
            response.setContentType("application/json")
            response.setHeader("Content-disposition", "attachment; filename=\"${name}.json\"")
            response.outputStream << jsonResponse
            response.outputStream.flush()
        }
    }
}
