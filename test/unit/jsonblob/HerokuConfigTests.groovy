package jsonblob

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class HerokuConfigTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testMongoHQConfig() {
        def expectedUsername = "aUser"
        def expectedPassword = "aPassword"
        def expectedHost = "silly.mongohq.host"
        def expectedPort = 10049
        def expectedAppId = "appId"

        def url = "mongodb://${expectedUsername}:${expectedPassword}@${expectedHost}:${expectedPort}/${expectedAppId}"

        def uri = new URI(url)
        def host = uri.host
        def port = uri.port
        def username = uri.userInfo.split(":")[0]
        def password = uri.userInfo.split(":")[1]
        def databaseName = uri.path.substring(1)

        assertEquals(expectedHost, host)
        assertEquals(expectedPort, port)
        assertEquals(expectedUsername, username)
        assertEquals(expectedPassword, password)
        assertEquals(expectedAppId, databaseName)
    }
}
