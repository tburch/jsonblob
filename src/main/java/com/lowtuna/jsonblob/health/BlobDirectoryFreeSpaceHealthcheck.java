package com.lowtuna.jsonblob.health;

import com.codahale.metrics.health.HealthCheck;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileSystemUtils;

import java.io.File;

/**
 * Created by tburch on 2/9/17.
 */
@RequiredArgsConstructor
public class BlobDirectoryFreeSpaceHealthcheck extends HealthCheck {
  private final File blobDataDirectory;
  private final long minFreeSpaceKb;

  @Override
  protected Result check() throws Exception {
    long freeSpaceKb = FileSystemUtils.freeSpaceKb(blobDataDirectory.getAbsolutePath());
    String message = freeSpaceKb + "Kb free for blob storage`";
    return freeSpaceKb > minFreeSpaceKb ? Result.healthy(message) : Result.unhealthy(message);
  }
}
