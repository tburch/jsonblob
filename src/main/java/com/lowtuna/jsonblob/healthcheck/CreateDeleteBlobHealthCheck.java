package com.lowtuna.jsonblob.healthcheck;

import lombok.AllArgsConstructor;

import org.bson.types.ObjectId;

import com.codahale.metrics.health.HealthCheck;
import com.lowtuna.jsonblob.core.BlobManager;
import com.mongodb.DBObject;

@AllArgsConstructor
public class CreateDeleteBlobHealthCheck extends HealthCheck {
    private static final String SAMPLE_JSON = "{\"up\": true}";
    private final BlobManager blobManager;

    @Override
    protected Result check() throws Exception {
        DBObject newBlob = blobManager.create(SAMPLE_JSON);
        ObjectId id = (ObjectId) newBlob.get("_id");
        blobManager.delete(id);
        return Result.healthy();
    }
}
