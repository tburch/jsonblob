package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.dropwizard.extras.config.executors.ScheduledExecutorServiceConfig;
import io.dropwizard.util.Duration;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.File;

@Data
@NoArgsConstructor
public class BlobManagerConfig {

  @JsonProperty
  @NotEmpty
  private String blobCollectionName = "blob";

  @JsonProperty
  @NotNull
  private ScheduledExecutorServiceConfig scheduledExecutorService = new ScheduledExecutorServiceConfig("blobManagerScheduledExecutor-%d");

  @JsonProperty
  @NotNull
  private ScheduledExecutorServiceConfig cleanupScheduledExecutorService = new ScheduledExecutorServiceConfig("blobManagerCleanupScheduledExecutor-%d");

  @JsonProperty
  @NotNull
  private Duration blobAccessTtl = Duration.days(90);

  private boolean deleteEnabled = false;

  @NotNull
  @JsonProperty("fileSystemBlogDataDirectory")
  private File fileSystemBlogDataDirectory;

}
