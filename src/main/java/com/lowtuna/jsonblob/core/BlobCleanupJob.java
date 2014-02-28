package com.lowtuna.jsonblob.core;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;

@Slf4j
public class BlobCleanupJob implements Runnable {
    private final DBCollection collection;
    private final Duration blobAccessTtl;

    public BlobCleanupJob(DBCollection collection, Duration blobAccessTtl) {
        this.collection = collection;
        this.blobAccessTtl = blobAccessTtl;
    }

    @Override
    public void run() {
        DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minus(blobAccessTtl.toMilliseconds());
        BasicDBObject query = new BasicDBObject();
        query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$lte", new Date(minLastAccessed.getMillis())).get());
        log.info("removing all blobs that haven't been accessed since {}", minLastAccessed);
        WriteResult result =collection.remove(query);
        log.info("successfully removed {} blob(s)", result.getN());
    }
}
