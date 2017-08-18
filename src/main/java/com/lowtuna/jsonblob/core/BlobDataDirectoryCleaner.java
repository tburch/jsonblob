package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import io.dropwizard.util.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.lang3.time.StopWatch;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tburch on 8/18/17.
 */
@AllArgsConstructor
@Slf4j
public class BlobDataDirectoryCleaner extends DirectoryWalker<String> implements Runnable {
  private final Path dataDirectoryPath;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;

  @Override
  protected void handleFile(File file, int depth, Collection<String> results) throws IOException {
    log.debug("Processing {}", file.getAbsolutePath());
    String blobId = file.getName().split("\\.", 2)[0];
    File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(file.getParentFile());

    if (file.equals(metadataFile)) {
      return;
    }
    
    try {
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
        log.debug("Blob {} is older than {}, so it's going to be deleted", blobId, blobAccessTtl);
        file.delete();
        results.add(blobId);
      }

    } catch (IOException e) {
      log.warn("Couldn't load metadata file from {}", file.getParentFile().getAbsolutePath(), e);
    }
  }

  @Override
  protected boolean handleDirectory(File directory, int depth, Collection<String> results) throws IOException {
    if (directory.listFiles() != null && directory.listFiles().length == 0) {
      log.info("{} has no files, so it's being deleted", directory.getAbsolutePath());
      directory.delete();
      return false;
    }

    if (isDataDir(directory.getAbsolutePath())) {
      String[] dateParts = directory.getAbsolutePath().replace(dataDirectoryPath.toFile().getAbsolutePath(), "").split("/", 4);
      LocalDate localDate = LocalDate.of(Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[3]));
      return localDate.isBefore(LocalDate.now().minusDays(blobAccessTtl.toDays()));
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
      List<String> removedBlobs = Lists.newArrayList();
      walk(dataDirectoryPath.toFile(), removedBlobs);
      log.info("Completed cleaning up {} un-accessed blobs in {}ms", removedBlobs.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
