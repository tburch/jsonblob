package jsonblob

import org.grails.jaxrs.itest.IntegrationTestCase
import org.junit.Test

import static org.junit.Assert.*

class JsonBlobApiTest extends IntegrationTestCase {

    @Test
    void testPostAndGet() {
        def headers = ['Content-Type':'application/json', 'Accept':'application/json']
        def content = '{"class":"jsonblob.JsonBlob","blob":{"data": "hi"}}'

        // create new person
        sendRequest('/api/jsonBlob', 'POST', headers, content.bytes)

        assertEquals(201, response.status)
        assertTrue(response.contentAsString.length() > 0)
        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
        assertTrue(response.getHeader('Location') ==~ /.*\/api\/jsonBlob\/.*/)

        // get list of persons
        sendRequest('/api/jsonBlob', 'GET', headers)

        assertEquals(200, response.status)
        assertTrue(response.getHeader('Content-Type').startsWith('application/json'))
        assertTrue(response.contentAsString.length() > 0)
    }

}