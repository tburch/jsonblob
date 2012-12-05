package jsonblob

import com.gmongo.GMongo
import com.mongodb.BasicDBObject
import com.mongodb.util.JSON
import org.bson.types.ObjectId
import org.grails.jaxrs.provider.DomainObjectNotFoundException

class JsonBlobResourceService {

    GMongo mongo

    private def blobCollection() {
        mongo.getDB("jsonblob").getCollection("blob")
    }

    private def getDBObject(String objectId) {
        new BasicDBObject("_id", new ObjectId(objectId))
    }

    def create(String json) {
        def parsed = JSON.parse(json)
        blobCollection().insert(parsed)
        parsed
    }

    def read(String id) {
        def obj = blobCollection().findOne(getDBObject(id))
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        obj
    }
    
    def update(String id, String json) {
        def obj = blobCollection().findOne(getDBObject(id))
        if (!obj) {
            throw new DomainObjectNotFoundException(ObjectId.class, id)
        }
        def parsed = com.mongodb.util.JSON.parse(json)
        blobCollection().update(getDBObject(id), parsed)
        parsed
    }
    
    void delete(String id) {
        def obj = blobCollection().findOne(getDBObject(id))
        if (obj) {
            blobCollection().remove(getDBObject(id))
        }
    }

}

