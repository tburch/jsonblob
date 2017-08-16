package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.dropwizard.util.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class BlobCleanupJob implements Runnable {
  private final Path blobDirectory;
  private final Duration blobAccessTtl;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ObjectMapper om;
  private final boolean deleteEnabled;
  private final ExecutorService executorService;

  @Override
  public void run() {
    try {
      List<String> dataDirs = Lists.newCopyOnWriteArrayList();

      Files.find(blobDirectory, 3, (p, bfa) -> !bfa.isRegularFile())
              .forEach(dataDir -> dataDirs.add(dataDir.toFile().getAbsolutePath()));

      log.debug("Found {} data directories", dataDirs.size());

      for(String dataDirPath: dataDirs) {
        File dir = new File(dataDirPath);
        if (dir.listFiles().length == 0) {
          log.debug("Deleting {} because there are no files in it", dir);
          dir.delete();
        }

        log.debug("Submitting DataDirectoryCleanupJob for {}", dataDirPath);
        executorService.submit(new DataDirectoryCleanupJob(dataDirPath, executorService, fileSystemJsonBlobManager,blobAccessTtl, om, deleteEnabled));
      }
    } catch (Exception e) {
      log.warn("Couldn't remove old blobs", e);
    }
  }
}
