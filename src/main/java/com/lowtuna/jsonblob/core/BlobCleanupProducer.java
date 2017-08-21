package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.DirectoryWalker;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tburch on 8/18/17.
 */
@Slf4j
public class BlobCleanupProducer extends DirectoryWalker<Void> implements Runnable {
  private final Path dataDirectoryPath;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;

  public BlobCleanupProducer(Path dataDirectoryPath, Duration blobAccessTtl, FileSystemJsonBlobManager fileSystemJsonBlobManager, ObjectMapper om) {
    super(null, 3);
    this.dataDirectoryPath = dataDirectoryPath;
    this.blobAccessTtl = blobAccessTtl;
    this.fileSystemJsonBlobManager = fileSystemJsonBlobManager;
    this.om = om;
  }


  @Override
  protected boolean handleDirectory(File directory, int depth, Collection<Void> results) throws IOException {
    if (isDataDir(directory.getAbsolutePath())) {
      String[] dateParts = directory.getAbsolutePath().replace(dataDirectoryPath.toFile().getAbsolutePath(), "").split("/", 4);
      LocalDate localDate = LocalDate.of(Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[3]));
      boolean process = localDate.isBefore(LocalDate.now().minusDays(blobAccessTtl.toDays()));
      if (process) {
        log.info("Processing {} blobs for un-accessed blobs", directory.getAbsolutePath());
        AtomicInteger fileCount = new AtomicInteger(0);
        Files.newDirectoryStream(directory.toPath())
                .forEach(path -> {
                  fileCount.incrementAndGet();
                  File file = path.toFile();
                  if (file.getName().startsWith(FileSystemJsonBlobManager.BLOB_METADATA_FILE_NAME)) {
                    return;
                  }

                  try {
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
                        log.info("Blob {} hasn't been accessed in {} (last accessed {}), so it's going to be deleted", blobId, blobAccessTtl, lastAccessed.get());
                      }
                    }
                  } catch (JsonParseException e) {
                    log.warn("Couldn't parse JSON from BlobMetadataContainer", e);
                  } catch (JsonMappingException e) {
                    log.warn("Couldn't map JSON from BlobMetadataContainer", e);
                  } catch (IOException e) {
                    log.warn("Couldn't read json for BlobMetadataContainer file", e);
                  }
                });

        log.info("Processed {} blobs in {}", fileCount.get(), directory.getAbsolutePath());

        if (fileCount.get() == 0) {
          log.info("{} has no files, so it's being deleted", directory.getAbsolutePath());
        } else if (fileCount.get() == 1) {
          File[] files = directory.listFiles();
          if (files != null && files.length > 0 && files[0].getName().startsWith(FileSystemJsonBlobManager.BLOB_METADATA_FILE_NAME)) {
            if (files[0].delete() && directory.delete()) {
              log.info("{} has only a metadata file, so it's being deleted", directory.getAbsolutePath());
            }
          }
        }
        return false;
      }
    } else {
      File[] files = directory.listFiles();
      if (files != null && files.length == 0) {
        if (directory.delete()) log.info("{} has no files, so it's being deleted", directory.getAbsolutePath());
        return false;
      }
    }
    return true;
  }

  private boolean isDataDir(String path) {
    return path.replace(dataDirectoryPath.toFile().getAbsolutePath(), "").split("/").length == 4;
  }

  @Override
  public void run() {
    Stopwatch stopwatch = new Stopwatch().start();
    try {
      walk(dataDirectoryPath.toFile(), null);
      log.info("Completed cleaning up un-accessed blobs in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
