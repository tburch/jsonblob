package jsonblob

import grails.test.ControllerUnitTestCase
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockMultipartHttpServletRequest

import javax.servlet.http.HttpServletResponse

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

@TestMixin(FileController)
class FileControllerTests extends ControllerUnitTestCase {

    void testUploadWithFile() {
        def json = new File("test/data/test.json")
        controller.metaClass.request = new MockMultipartHttpServletRequest()
        controller.request.addFile(new MockMultipartFile('json', 'test.json', 'application/json', json.getBytes()))
        controller.upload()

        assertEquals(HttpServletResponse.SC_OK, controller.response.status)
        assertEquals(new String(json.getBytes()), controller.response.getContentAsString())
    }

    void testFetch() {
        controller.params.url = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=Calvin+and+Hobbes"
        controller.fetch()

        assertEquals(HttpServletResponse.SC_OK, controller.response.status)
        assertTrue(controller.response.getContentAsString().length() > 0)
    }
}
