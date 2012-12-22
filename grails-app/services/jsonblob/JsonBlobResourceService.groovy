package jsonblob

import com.gmongo.GMongo
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import groovy.json.JsonBuilder
import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException
import org.springframework.beans.factory.InitializingBean

class JsonBlobResourceService implements InitializingBean {

    def apiDemoObjectId //HACK

    GMongo mongo

    def blobCollection

    private def getDBObject(String objectId) {
        if (ObjectId.isValid(objectId)) {
            return new BasicDBObject("_id", new ObjectId(objectId))
        } else {
            return null
        }
    }

    private def createJson(String json) {
        def jsonBuilder = new JsonBuilder()
        jsonBuilder.blob JSON.parse(json)
        JSON.parse(jsonBuilder.toString())
    }

    def create(String json) {
        def parsed = createJson(json)
        blobCollection.insert(parsed)
        parsed
    }

    def read(String id) {
        def objectId = getDBObject(id)
        if (!objectId) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def obj = blobCollection.findOne(objectId)
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        obj
    }
    
    def update(String id, String json) {
        def objectId = getDBObject(id)
        if (!objectId) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def obj = blobCollection.findOne(objectId)
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def parsed = createJson(json)
        blobCollection.update(objectId, parsed)
        parsed
    }
    
    void delete(String id) {
        def objectId = getDBObject(id)
        if (!objectId) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def obj = blobCollection.findOne(objectId)
        if (obj) {
            blobCollection.remove(objectId)
        }
    }

    void afterPropertiesSet() throws Exception {
        def db
        if (System.env.MONGOHQ_URL) {
            def uri = new URI(System.env.MONGOHQ_URL)
            def username = uri.userInfo.split(":")[0]
            def password = uri.userInfo.split(":")[1]
            def databaseName = uri.path.substring(1)

            def mongo = new GMongo(uri.host, uri.port)

            db = mongo.getDB(databaseName)
            db.authenticate(username, password.toCharArray())
        } else {
            db = mongo.getDB(mongo.getDatabaseNames().first())
        }
        this.blobCollection = db.getCollection("blob")
    }
}

