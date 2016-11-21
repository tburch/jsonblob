package com.lowtuna.jsonblob.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.lowtuna.jsonblob.core.FileSystemJsonBlobManager;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CreateDeleteBlobHealthCheck extends HealthCheck {
    private static final String SAMPLE_JSON = "{\"up\": true}";
    private final FileSystemJsonBlobManager fileSystemBlobManager;

    @Override
    protected Result check() throws Exception {
        String newBlobId = fileSystemBlobManager.createBlob(SAMPLE_JSON);
        fileSystemBlobManager.deleteBlob(newBlobId);
        return Result.healthy();
    }
}
