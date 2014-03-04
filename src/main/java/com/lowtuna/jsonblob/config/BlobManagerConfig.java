package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.dropwizard.extras.config.executors.ScheduledExecutorServiceConfig;
import io.dropwizard.util.Duration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class BlobManagerConfig {

    @JsonProperty
    private boolean deleteEnabled = false;

    @JsonProperty
    @NotEmpty
    private String blobCollectionName = "blob";

    @JsonProperty
    @NotNull
    private ScheduledExecutorServiceConfig scheduledExecutorService = new ScheduledExecutorServiceConfig("blobManagerScheduledExecutor-%d");

    @JsonProperty
    @NotNull
    private Duration blobCleanupFrequency = Duration.hours(1);

    @JsonProperty
    @NotNull
    private Duration blobAccessTtl = Duration.days(90);
}
