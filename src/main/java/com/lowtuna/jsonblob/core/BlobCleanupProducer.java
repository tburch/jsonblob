package com.lowtuna.jsonblob.core;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Stopwatch;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tburch on 8/18/17.
 */
@Slf4j
public class BlobCleanupProducer extends DirectoryWalker<Void> implements Runnable {
  private final Path dataDirectoryPath;
  private final Duration blobAccessTtl;
  private final BlockingQueue<File> filesToProcess;

  public BlobCleanupProducer(Path dataDirectoryPath, Duration blobAccessTtl, BlockingQueue<File> filesToProcess, MetricRegistry metricRegistry) {
    super(null, 3);
    this.dataDirectoryPath = dataDirectoryPath;
    this.blobAccessTtl = blobAccessTtl;
    this.filesToProcess = filesToProcess;
    metricRegistry.register(MetricRegistry.name(getClass(), "filesToProcessCount"), (Gauge<Integer>) () -> filesToProcess.size());
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
                    filesToProcess.put(file);
                  } catch (InterruptedException e) {
                    log.warn("Interrupted while trying to add file to be processed at {}", file.getAbsolutePath(), e);
                  }
                });

        log.info("Processed {} blobs in {}", fileCount.get(), directory.getAbsolutePath());

        if (fileCount.get() == 0) {
          log.info("{} has no files, so it's being deleted", directory.getAbsolutePath());
        } else if (fileCount.get() == 1) {
          File[] files = directory.listFiles();
          if (files != null && files[0].getName().startsWith(FileSystemJsonBlobManager.BLOB_METADATA_FILE_NAME)) {
            if (directory.delete()) {
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
