package jsonblob

import com.gmongo.GMongo
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException
import org.springframework.beans.factory.InitializingBean

class JsonBlobResourceService implements InitializingBean {

    GMongo mongo

    def blobCollection

    private def getDBObject(String objectId) {
        new BasicDBObject("_id", new ObjectId(objectId))
    }

    def create(String json) {
        def parsed = JSON.parse(json)
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
        def parsed = com.mongodb.util.JSON.parse(json)
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
            def host = uri.host
            def port = uri.port
            def username = uri.userInfo.split(":")[0]
            def password = uri.userInfo.split(":")[1]
            def databaseName = uri.path.substring(1)

            log.info("Setting up mongo using config $System.env.MONGOHQ_URL. Parsed into host=$host, port=$port, username=$username, databaseName=$databaseName")

            def mongo = new GMongo(host, port)

            db = mongo.getDB(databaseName)
            db.authenticate(username, password.toCharArray())
        } else {
            db = mongo.getDB(mongo.getDatabaseNames().first())
        }
        this.blobCollection = db.getCollection("blob")
    }
}

