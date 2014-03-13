package com.lowtuna.jsonblob.core;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BlobCleanupJob implements Runnable {
    private final DBCollection collection;
    private final Duration blobAccessTtl;

    public BlobCleanupJob(final DBCollection collection, Duration blobAccessTtl, MetricRegistry metricRegistry) {
        this.collection = collection;
        this.blobAccessTtl = blobAccessTtl;

        metricRegistry.register(MetricRegistry.name(getClass(), "accessed", "24Hours"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(1);
                BasicDBObject query = new BasicDBObject();
                query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                return collection.getCount(query);
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "accessed", "7Days"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(7);
                BasicDBObject query = new BasicDBObject();
                query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                return collection.getCount(query);
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "accessed", "30Days"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(30);
                BasicDBObject query = new BasicDBObject();
                query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                return collection.getCount(query);
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "accessed", "60Days"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(60);
                BasicDBObject query = new BasicDBObject();
                query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                return collection.getCount(query);
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "accessed", "90Days"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(90);
                BasicDBObject query = new BasicDBObject();
                query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                return collection.getCount(query);
            }
        });
    }

    @Override
    public void run() {
        DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minus(blobAccessTtl.toMilliseconds());
        BasicDBObject query = new BasicDBObject();
        query.put(BlobManager.ACCESSED_ATTR_NAME, BasicDBObjectBuilder.start("$lte", new Date(minLastAccessed.getMillis())).get());
        log.info("removing all blobs that haven't been accessed since {}", minLastAccessed);
        WriteResult result = collection.remove(query);
        log.info("successfully removed {} blob(s)", result.getN());
    }
}
