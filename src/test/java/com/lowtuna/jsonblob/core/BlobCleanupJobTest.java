package com.lowtuna.jsonblob.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.util.Duration;
import lombok.extern.java.Log;
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
@Log
public class BlobCleanupJobTest {

  private static final File TEMP;
  static {
    File temp = FileUtils.getTempDirectory();
    File dir = new File(temp, UUID.randomUUID().toString());
    dir.deleteOnExit();
    TEMP = dir;
  }

  private final Duration blobTtl = Duration.minutes(1);

  private FileSystemJsonBlobManager blobManager;

  @Before
  public void initBlobManage() {
    this.blobManager = new FileSystemJsonBlobManager(TEMP, Executors.newSingleThreadScheduledExecutor(), Executors.newSingleThreadScheduledExecutor(), new ObjectMapper(), blobTtl, true);
  }

  @Test
  public void testCleanup() throws Exception {
    DateTime now = DateTime.now();

    String oldBlobId = (new ObjectId(now.minusDays((int) (blobTtl.toMinutes() * 2)).toDate())).toString();

    Assert.assertEquals(0, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", oldBlobId);
    Assert.assertEquals(1, countFiles());
    blobManager.createBlob("{\"foo\":|\"bar\"}", (new ObjectId(now.toDate())).toString());
    Assert.assertEquals(2, countFiles());

    blobManager.run();

    log.info("Starting blob manager");
    blobManager.start();

    Thread.sleep(2000);

    Assert.assertEquals(1, countFiles());
  }

  private long countFiles() throws IOException {
    return Files.find(TEMP.toPath(), 999, (p, bfa) -> bfa.isRegularFile()).count();
  }

}