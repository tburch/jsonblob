package jsonblob

import org.bson.types.ObjectId
import com.mongodb.util.JSON

class JsonBlob {

    ObjectId id
    JSON blob

    static constraints = {
    }

    static mapWith = "mongo"
}
