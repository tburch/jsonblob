package com.lowtuna.jsonblob.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.DB;

public class MongoHealthCheck extends HealthCheck {

    private final DB db;

    public MongoHealthCheck(DB db) {
        this.db = db;
    }

    @Override
    protected Result check() throws Exception {
        db.getCollectionNames();
        return Result.healthy();
    }
}
