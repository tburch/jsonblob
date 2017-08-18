package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.util.Duration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * Created by tburch on 8/16/17.
 */
@Data
@Slf4j
public class DataDirectoryCleanupJob implements Runnable {
  private final String dataDirPath;
  private final ExecutorService executorService;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final Duration blobAccessTtl;
  private final ObjectMapper om;
  private final boolean deleteEnabled;

  @Override
  public void run() {
    File dir = new File(dataDirPath);

    log.info("Checking for blobs not accessed in the last {} in {}", blobAccessTtl, dir.getAbsolutePath());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    try {
      List<File> files = Arrays.asList(dir.listFiles()).parallelStream().filter(File::exists).collect(Collectors.toList());
      Set<String> blobs = Sets
              .newHashSet(Lists.transform(files, f -> f.getName().split("\\.", 2)[0]))
              .parallelStream()
              .filter(f -> fileSystemJsonBlobManager.resolveTimestamp(f).isPresent()).collect(Collectors.toSet());
      log.info("Identified {} blobs in {}", blobs.size(), dir);
      Map<String, DateTime> lastAccessed = Maps.newHashMap(Maps.asMap(blobs, new Function<String, DateTime>() {
        @Nullable
        @Override
        public DateTime apply(@Nullable String input) {
          return fileSystemJsonBlobManager.resolveTimestamp(input).get();
        }
      }));
      log.debug("Completed building map of {} last accessed timestamps in {}", lastAccessed.size(), dir);

      File metadataFile = fileSystemJsonBlobManager.getMetaDataFile(dir);
      try {
        BlobMetadataContainer metadataContainer = metadataFile.exists() ? om.readValue(fileSystemJsonBlobManager.readFile(metadataFile), BlobMetadataContainer.class) : new BlobMetadataContainer();
        log.debug("Adding {} last accessed timestamp from metadata {}", metadataContainer.getLastAccessedByBlobId().size(), metadataFile.getAbsolutePath());
        lastAccessed.putAll(metadataContainer.getLastAccessedByBlobId());
        log.debug("Determining which blobs to remove from {}", dir);
        Map<String, DateTime> toRemove = Maps.filterEntries(lastAccessed, input -> input.getValue().plusMillis((int) blobAccessTtl.toMilliseconds()).isBefore(DateTime.now()));
        log.info("Identified {} blobs to remove in {}", toRemove.size(), dir);

        if (toRemove.keySet().isEmpty()) {
          return;
        }

        if (toRemove.size() == blobs.size()) {
          log.info("All {} files in {} should be deleted, so we'll delete the directory", toRemove.size(), dir);
          dir.delete();
          log.info("{} was deleted", dir);
        }

        log.debug("Submitting BulkBlobDeleteJobs for {} blobs in {}", toRemove.size(), dir);
        List<List<String>> subSets = Lists.partition(Lists.newArrayList(toRemove.keySet()), 1000);
        subSets.parallelStream().forEach(list -> {
          BulkBlobDeleteJob deleteJob = new BulkBlobDeleteJob(Sets.newHashSet(list), fileSystemJsonBlobManager, deleteEnabled);
          deleteJob.run();
//          executorService.submit(deleteJob);
        });
      } catch (IOException e) {
        log.warn("Couldn't load metadata file from {}", dir.getAbsolutePath(), e);
      }
    } catch (Exception e) {
      log.warn("Caught Exception while trying to remove un-accessed blobs in {}", dir, e);
    }
  }
}
