package com.lowtuna.jsonblob.core;

import com.google.common.base.Stopwatch;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.util.concurrent.TimeUnit;

/**
 * Created by tburch on 11/15/16.
 */
@Slf4j
public class BlobMigrationJob implements Runnable {

  private final MongoDbJsonBlobManager mongoDbJsonBlobManager;
  private final FileSystemJsonBlobManager fileSystemJsonBlobManager;

  public BlobMigrationJob(MongoDbJsonBlobManager mongoDbJsonBlobManager, FileSystemJsonBlobManager fileSystemJsonBlobManager) {
    this.mongoDbJsonBlobManager = mongoDbJsonBlobManager;
    this.fileSystemJsonBlobManager = fileSystemJsonBlobManager;
  }

  @Override
  public void run() {
    Stopwatch stopwatch = new Stopwatch();
    stopwatch.start();
    int migratedBlobs = 0;

    log.info("Starting blob migration");

    DBCursor curs = mongoDbJsonBlobManager.getCollection().find();
    try {
      while (curs.hasNext()) {
        try {
          if (curs.hasNext()) {
            migratedBlobs++;
            DBObject o = curs.next();

            ObjectId id = (ObjectId) o.get(MongoDbJsonBlobManager.ID_ATTR_NAME);
            try {
              String json = o.get(MongoDbJsonBlobManager.BLOB_ATTR_NAME).toString();

              log.trace("Migrating blob {}", id);
              fileSystemJsonBlobManager.createBlob(json, id.toString());
              log.trace("Completed migrating blob {}", id);
            } finally {
              mongoDbJsonBlobManager.deleteBlob(id.toString());
            }

            if (migratedBlobs % 100 == 0) {
              log.info("Migrated {} blobs... (~{} per second)", migratedBlobs, migratedBlobs/stopwatch.elapsed(TimeUnit.SECONDS));
            }
          }
        } catch (MongoException e) {
          log.warn("Error while migrating blob", e);
        } catch (Exception e) {
          log.warn("Caught exception while migrating blob", e);
        }
      }
      } catch (Exception e) {
      log.warn("Caught exception while migrating blobs", e);
    }
    log.info("Completed migrating {} blobs in {}ms", migratedBlobs, stopwatch.elapsed(TimeUnit.MILLISECONDS));
  }
}
