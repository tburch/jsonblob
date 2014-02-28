package com.lowtuna.jsonblob.util.mongo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdJacksonDeserializer extends StdDeserializer<ObjectId> {

    public ObjectIdJacksonDeserializer() {
        super(ObjectId.class);
    }

    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new ObjectId(jp.getValueAsString());
    }
}
