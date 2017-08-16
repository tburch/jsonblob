package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class BlobCleanupJob implements Runnable {
  private final Path blobDirectory;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;
  private final boolean deleteEnabled;

  @Override
  public void run() {
    Stopwatch stopwatch = new Stopwatch().start();
    try {
      Files.walk(blobDirectory)
              .parallel()
              .filter(p -> !p.toFile().isDirectory())
              .map(Path::getParent)
              .distinct()
              .forEach(dataDir -> {
                log.info("Checking for blobs not accessed in the last {} in {}", blobAccessTtl, dataDir.toAbsolutePath());
                Set<String> blobs = Sets
                        .newHashSet(Lists.transform(Arrays.asList(dataDir.toFile().listFiles()), f -> f.getName().split("\\.", 2)[0]))
                        .parallelStream()
                        .filter(f -> fileSystemJsonBlobManager.resolveTimestamp(f).isPresent()).collect(Collectors.toSet());
                log.info("Identified {} blobs in {}", blobs.size(), dataDir);
                Map<String, DateTime> lastAccessed = Maps.newHashMap(Maps.asMap(blobs, new Function<String, DateTime>() {
                  @Nullable
                  @Override
                  public DateTime apply(@Nullable String input) {
                    return fileSystemJsonBlobManager.resolveTimestamp(input).get();
                  }
                }));
                log.info("Completed building map of {} last accessed timestamps in {}", lastAccessed.size(), dataDir);

                File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(dataDir.toFile());
                try {
                  BlobMetadataContainer metadataContainer = metadataFile.exists() ? om.readValue(fileSystemJsonBlobManager.readFile(metadataFile), BlobMetadataContainer.class) : new BlobMetadataContainer();
                  log.info("Adding {} last accessed timestamp from metadata {}", metadataContainer.getLastAccessedByBlobId().size(), metadataFile.getAbsolutePath());
                  lastAccessed.putAll(metadataContainer.getLastAccessedByBlobId());
                  log.debug("Determining which blobs to remove from {}", dataDir);
                  Map<String, DateTime> toRemove = Maps.filterEntries(lastAccessed, input -> input.getValue().plusMillis((int) blobAccessTtl.toMilliseconds()).isBefore(DateTime.now()));
                  log.info("Identified {} blobs to remove in {}", toRemove.size(), dataDir);
                  toRemove.keySet().parallelStream().forEach(blobId -> {
                    if (deleteEnabled) {
                      log.debug("Deleting blob with id {}", blobId);
                      try {
                        fileSystemJsonBlobManager.deleteBlob(blobId);
                      } catch (BlobNotFoundException e) {
                        log.debug("Couldn't delete blobId {} because it's already been deleted", blobId);
                      }
                    }
                  });
                } catch (IOException e) {
                  log.warn("Couldn't load metadata file from {}", dataDir.toAbsolutePath(), e);
                }
              });
      log.info("Completed cleanup in {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    } catch (Exception e) {
      log.warn("Couldn't remove old blobs", e);
    }
  }
}
