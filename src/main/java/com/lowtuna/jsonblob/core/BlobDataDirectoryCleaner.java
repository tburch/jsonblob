package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.dropwizard.util.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.DirectoryWalker;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tburch on 8/18/17.
 */
@AllArgsConstructor
@Slf4j
public class BlobDataDirectoryCleaner extends DirectoryWalker<Void> implements Runnable {
  private final Path dataDirectoryPath;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;
  private final AtomicInteger deletedBlobCount = new AtomicInteger(0);

  private final LoadingCache<String, BlobMetadataContainer> blobMetadataContainerCache = CacheBuilder.newBuilder()
          .expireAfterWrite(1, TimeUnit.HOURS)
          .weakValues()
          .build(new CacheLoader<String, BlobMetadataContainer>() {
            @Override
            public BlobMetadataContainer load(String key) throws Exception {
              File metadataFile = new File(key);
              return metadataFile.exists() ? om.readValue(fileSystemJsonBlobManager.readFile(metadataFile), BlobMetadataContainer.class) : new BlobMetadataContainer();
            }
          });

  @Override
  protected void handleFile(File file, int depth, Collection<Void> results) throws IOException {
    log.debug("Processing {}", file.getAbsolutePath());
    String blobId = file.getName().split("\\.", 2)[0];
    File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(file.getParentFile());

    if (file.equals(metadataFile)) {
      return;
    }

    BlobMetadataContainer metadataContainer = blobMetadataContainerCache.getUnchecked(metadataFile.getAbsolutePath());

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
      log.info("Blob {} is older than {} (last accessed {}), so it's going to be deleted", blobId, blobAccessTtl, lastAccessed.get());
      if (file.delete()) {
        deletedBlobCount.incrementAndGet();
      }
    }
  }

  @Override
  protected boolean handleDirectory(File directory, int depth, Collection<Void> results) throws IOException {
    if (directory.listFiles() != null && directory.listFiles().length == 0) {
      log.info("{} has no files, so it's being deleted", directory.getAbsolutePath());
      directory.delete();
      return false;
    }

    if (directory.listFiles().length == 1) {
      if (directory.listFiles()[0].getName().startsWith(FileSystemJsonBlobManager.BLOB_METADATA_FILE_NAME)) {
        log.info("{} has only a metadata file, so it's being deleted", directory.getAbsolutePath());
        directory.delete();
        return false;
      }
    }

    boolean process = true;
    if (isDataDir(directory.getAbsolutePath())) {
      String[] dateParts = directory.getAbsolutePath().replace(dataDirectoryPath.toFile().getAbsolutePath(), "").split("/", 4);
      LocalDate localDate = LocalDate.of(Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]), Integer.parseInt(dateParts[3]));
      process = localDate.isBefore(LocalDate.now().minusDays(blobAccessTtl.toDays()));
      if (process) {
        log.info("Processing {} with {} blobs for un-accessed blobs", directory.getAbsolutePath(), directory.listFiles().length - 1);
      }
    }

    return process;
  }

  private boolean isDataDir(String path) {
    return path.replace(dataDirectoryPath.toFile().getAbsolutePath(), "").split("/").length == 4;
  }

  @Override
  public void run() {
    deletedBlobCount.set(0);
    Stopwatch stopwatch = new Stopwatch().start();
    try {
      walk(dataDirectoryPath.toFile(), null);
      log.info("Completed cleaning up {} un-accessed blobs in {}ms", deletedBlobCount.get(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } catch (Exception e) {
      e.printStackTrace();
    }
    deletedBlobCount.set(0);
  }
}
