package jsonblob

import com.gmongo.GMongo
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException
import org.springframework.beans.factory.InitializingBean

class JsonBlobResourceService implements InitializingBean {

    def apiDemoObjectId //HACK

    GMongo mongo

    def blobCollection

    private def getDBObject(String objectId) {
        new BasicDBObject("_id", new ObjectId(objectId))
    }

    private def createJson(String json) {
        def builder = new groovy.json.JsonBuilder()
        builder.blob JSON.parse(json)
        JSON.parse(builder.toString())
    }

    def create(String json) {
        def parsed = createJson(json)
        blobCollection.insert(parsed)
        parsed
    }

    def read(String id) {
        def obj = blobCollection.findOne(getDBObject(id))
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        obj
    }
    
    def update(String id, String json) {
        def obj = blobCollection.findOne(getDBObject(id))
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def parsed = createJson(json)
        blobCollection.update(getDBObject(id), parsed)
        parsed
    }
    
    void delete(String id) {
        def obj = blobCollection.findOne(getDBObject(id))
        if (obj) {
            blobCollection.remove(getDBObject(id))
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

