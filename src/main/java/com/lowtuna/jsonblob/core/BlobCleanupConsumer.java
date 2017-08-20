package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import io.dropwizard.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tburch on 8/20/17.
 */
@Slf4j
@RequiredArgsConstructor
public class BlobCleanupConsumer implements Runnable {
  private static final Duration QUEUE_TIMEOUT = Duration.seconds(15);

  private final BlockingQueue<File> filesToProcess;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;

  @Override
  public void run() {
    log.info("Polling queue for files to process for {}", QUEUE_TIMEOUT);
    try {
      File file = filesToProcess.poll(QUEUE_TIMEOUT.getQuantity(), QUEUE_TIMEOUT.getUnit());
      log.debug("Processing {}", file.getAbsolutePath());
      String blobId = file.getName().split("\\.", 2)[0];
      File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(file.getParentFile());

      if (file.equals(metadataFile)) {
        return;
      }

      BlobMetadataContainer metadataContainer = metadataFile.exists() ? om.readValue(fileSystemJsonBlobManager.readFile(metadataFile), BlobMetadataContainer.class) : new BlobMetadataContainer();

      Optional<DateTime> lastAccessed = fileSystemJsonBlobManager.resolveTimestamp(blobId);
      if (metadataContainer.getLastAccessedByBlobId().containsKey(blobId)) {
        lastAccessed = Optional.of(metadataContainer.getLastAccessedByBlobId().get(blobId));
      }

      if (!lastAccessed.isPresent()) {
        log.warn("Couldn't get last accessed timestamp for blob {}", blobId);
        return;
      }

      log.debug("Blob {} was last accessed {}", blobId, lastAccessed.get());

      if (lastAccessed.get().plusMillis((int) blobAccessTtl.toMilliseconds()).isBefore(DateTime.now())) {
        if (file.delete()) {
          log.info("Blob {} is older than {} (last accessed {}), so it's going to be deleted", blobId, blobAccessTtl, lastAccessed.get());
        }
      }
    } catch (InterruptedException e) {
      log.warn("Interrupted while trying to poll queue", e);
    } catch (JsonParseException e) {
      log.warn("Couldn't parse JSON from BlobMetadataContainer", e);
    } catch (JsonMappingException e) {
      log.warn("Couldn't map JSON from BlobMetadataContainer", e);
    } catch (IOException e) {
      log.warn("Couldn't read json for BlobMetadataContainer file", e);
    }
  }
}
