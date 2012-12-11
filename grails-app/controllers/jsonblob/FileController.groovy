package jsonblob

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class FileController {

    static allowedMethods = [upload: 'POST']

    def upload() {
        def jsonFile = request.getFile('file')
        Reader reader = null
        String name = UUID.randomUUID().toString()
        if (!jsonFile.empty) {
            name = jsonFile.getOriginalFilename()
            reader = new StringReader(new String(jsonFile.getBytes()))
        }

        if (reader) {
            response.setContentType("application/json")
            response.setHeader("Content-disposition", "attachment; filename=\"$name\"")
            response.outputStream << reader
            response.outputStream.flush()
        } else {
            redirect(uri: "/")
        }
    }

    def fetch() {
        log.info(request.getRequestURL())
        log.info(request.getHeader("referer"))
        def referrerUri = new java.net.URI(request.getHeader("referer"))
        def requestUri = new java.net.URI(request.getRequestURL().toString())
        if (!referrerUri.getHost().equals(requestUri.getHost())) {
            redirect(uri: "/")
        }

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
        } else {
            redirect(uri: "/")
        }
    }
}
