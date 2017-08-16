package com.lowtuna.jsonblob.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Created by tburch on 8/16/17.
 */
@Data
@Slf4j
public class BulkBlobDeleteJob implements Runnable {
  private final Set<String> blobsToDelete;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final boolean deleteEnabled;

  @Override
  public void run() {
    try {
      log.info("Deleting {} blobs", blobsToDelete.size());
      blobsToDelete.parallelStream().forEach(blobId -> {
        if (deleteEnabled) {
          log.debug("Deleting blob with id {}", blobId);
          try {
            fileSystemJsonBlobManager.deleteBlob(blobId);
          } catch (BlobNotFoundException e) {
            log.debug("Couldn't delete blobId {} because it's already been deleted", blobId);
          }
        }
      });
    } catch (Exception e) {
      log.warn("Caught exception while trying to delete {} blobs", blobsToDelete.size(), e);
    }
  }
}
