package com.lowtuna.jsonblob.core;

import com.google.common.base.Stopwatch;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tburch on 11/15/16.
 */
@Slf4j
public class BlobMigrationJob implements Runnable {

  private final MongoDbJsonBlobManager mongoDbJsonBlobManager;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;
  private final ExecutorService executorService = Executors.newFixedThreadPool(50);

  public BlobMigrationJob(MongoDbJsonBlobManager mongoDbJsonBlobManager, FileSystemJsonBlobManager fileSystemJsonBlobManager) {
    this.mongoDbJsonBlobManager = mongoDbJsonBlobManager;
    this.fileSystemJsonBlobManager = fileSystemJsonBlobManager;
  }

  @Override
  public void run() {
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    final AtomicInteger migratedBlobs = new AtomicInteger(0);

    log.info("Starting blob migration");
    int limit = 2500;
    final CountDownLatch latch = new CountDownLatch(limit);

    DBCursor curs = mongoDbJsonBlobManager.getCollection().find().skip(5000).limit(limit);
    try {
      while (curs.hasNext()) {
          if (curs.hasNext()) {
            DBObject o = curs.next();
            ObjectId id = (ObjectId) o.get(MongoDbJsonBlobManager.ID_ATTR_NAME);
            try {
              final String json = o.get(MongoDbJsonBlobManager.BLOB_ATTR_NAME).toString();
              executorService.submit(new Runnable() {
                @Override
                public void run() {
                  int completed = migratedBlobs.incrementAndGet();
                  try {
                    log.trace("Migrating blob {}", id);
                    fileSystemJsonBlobManager.createBlob(json, id.toString());
                    log.trace("Completed migrating blob {}", id);
                  } catch (MongoException e) {
                    log.warn("Error while migrating blob", e);
                  } catch (Exception e) {
                    log.warn("Caught exception while migrating blob", e);
                  } finally {
                    latch.countDown();
                    if (completed % 25 == 0) {
                      log.info("Migrated {} blobs... (~{} per second)", completed, completed / stopwatch.elapsed(TimeUnit.SECONDS));
                    }
                    try {
                      mongoDbJsonBlobManager.deleteBlob(id.toString());
                    } catch (BlobNotFoundException e) {
                      //this shouldn't happen...
                    }
                  }
                }
              });
            } catch (NullPointerException e) {
              mongoDbJsonBlobManager.deleteBlob(id.toString());
            }
          }
      }
      latch.await();
    } catch (Exception e) {
      log.warn("Caught exception while migrating blobs", e);
    } finally {
      curs.close();
    }
    log.info("Completed migrating {} blobs in {}ms", migratedBlobs, stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }
}
