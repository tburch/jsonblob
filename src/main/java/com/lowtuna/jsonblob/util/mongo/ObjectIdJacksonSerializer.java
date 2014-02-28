package com.lowtuna.jsonblob.util.mongo;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdJacksonSerializer extends StdSerializer<ObjectId> {

    public ObjectIdJacksonSerializer() {
        super(ObjectId.class);
    }

    @Override
    public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(value.toString());
    }
}
