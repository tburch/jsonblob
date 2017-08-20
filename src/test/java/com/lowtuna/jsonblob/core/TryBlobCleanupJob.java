package com.lowtuna.jsonblob.core;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by tburch on 8/16/17.
 */
@Slf4j
public class TryBlobCleanupJob {
  private final Duration blobTtl = Duration.days(1);

  private File tempDir;
  private FileSystemJsonBlobManager blobManager;

  @Before
  public void initBlobManage() {
    File temp = FileUtils.getTempDirectory();
    File dir = new File(temp, UUID.randomUUID().toString());
    dir.deleteOnExit();
    this.tempDir = dir;

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JodaModule());
    this.blobManager = new FileSystemJsonBlobManager(tempDir, Executors.newScheduledThreadPool(10), Executors.newScheduledThreadPool(10), objectMapper, blobTtl, true, new MetricRegistry());
  }

  @Test
  public void testCleanupWithAccessed() throws Exception {
    DateTime now = DateTime.now();

    String oldBlobId = (new ObjectId(now.minusDays((int) (blobTtl.toMinutes() * 2)).toDate())).toString();
    String newBlobId = new ObjectId(now.toDate()).toString();
    log.info("newBlobId={}, oldBlobId={}", newBlobId, oldBlobId);

    Assert.assertEquals(0, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", oldBlobId);
    Assert.assertEquals(1, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", newBlobId);
    Assert.assertEquals(2, countFiles());

    blobManager.getBlob(oldBlobId);

    blobManager.run();

    log.info("Starting blob manager");
    blobManager.start();

    Thread.sleep(2000);

    Assert.assertEquals(2, countFiles());
  }

  @Test
  public void testCleanup() throws Exception {
    DateTime now = DateTime.now();

    String oldBlobId = (new ObjectId(now.minusDays((int) (blobTtl.toMinutes() * 2)).toDate())).toString();
    String newBlobId = new ObjectId(now.toDate()).toString();
    log.info("newBlobId={}, oldBlobId={}", newBlobId, oldBlobId);

    Assert.assertEquals(0, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", oldBlobId);
    Assert.assertEquals(1, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", newBlobId);
    Assert.assertEquals(2, countFiles());

    blobManager.run();

    log.info("Starting blob manager");
    blobManager.start();

    Thread.sleep(2000);

    Assert.assertEquals(1, countFiles());
  }

  private long countFiles() throws IOException {
    return Files.find(tempDir.toPath(), 999, (p, bfa) -> bfa.isRegularFile()).count();
  }

}