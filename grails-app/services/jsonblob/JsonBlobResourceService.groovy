package jsonblob

import com.gmongo.GMongo
import com.mongodb.util.JSON
import org.grails.jaxrs.provider.DomainObjectNotFoundException

class JsonBlobResourceService {

    GMongo mongo

    private def blobCollection() {
        mongo.getDB("jsonblob").getCollection("blob")
    }

    def create(String json) {
        def parsed = JSON.parse(json)
        blobCollection().insert(parsed)
        parsed
    }

    def read(String id) {
        def obj = blobCollection().findOne(_id: id)
        if (!obj) {
            throw new DomainObjectNotFoundException(String.class, id)
        }
        obj
    }
    
    def readAll() {
        blobCollection().find()
    }
    
    def update(String id, String json) {
        def obj = blobCollection().findOne(_id: id)
        if (!obj) {
            throw new DomainObjectNotFoundException(String.class, id)
        }
        def parsed = com.mongodb.util.JSON.parse(json)
        db.doctor.update([_id:id],parsed)
        obj
    }
    
    void delete(String id) {
        def obj = blobCollection().findOne(_id: id)
        if (obj) {
            blobCollection().remove(_id: id)
        }
    }

}

