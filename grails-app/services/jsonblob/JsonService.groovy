package jsonblob

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import org.bson.types.ObjectId
import org.springframework.beans.factory.InitializingBean

class JsonService implements InitializingBean {

    def objectMapper

    void afterPropertiesSet() throws Exception {
        def om = new ObjectMapper()

        def jacksonMongoModule = new SimpleModule("MongoModule", new Version(1, 0, 0, null))
        jacksonMongoModule.addSerializer(ObjectId, new JsonSerializer<ObjectId>() {
            @Override
            void serialize(ObjectId t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(t.toString())
            }
        })
        om.registerModule(jacksonMongoModule)

        this.objectMapper = om
    }
}
