package com.lowtuna.jsonblob.util.mongo;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

public class JacksonMongoDbModule extends SimpleModule {

    public JacksonMongoDbModule() {
        super("MongoModule");

        addSerializer(new ObjectIdJacksonSerializer());
        addDeserializer(ObjectId.class, new ObjectIdJacksonDeserializer());
    }
}
