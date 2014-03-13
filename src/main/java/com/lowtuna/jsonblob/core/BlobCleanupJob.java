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

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BlobCleanupJob implements Runnable {
    private final DBCollection collection;
    private final Duration blobAccessTtl;

    public BlobCleanupJob(final DBCollection collection, Duration blobAccessTtl, MetricRegistry metricRegistry) {
        this.collection = collection;
        this.blobAccessTtl = blobAccessTtl;

        String[] attributes = new String[] { BlobManager.ACCESSED_ATTR_NAME, BlobManager.CREATED_ATTR_NAME, BlobManager.UPDATED_ATTR_NAME};
        DecimalFormat periodFormat = new DecimalFormat("00");

        for (final String attribute: attributes) {
            for (int period = 1; period <= 90; period += period == 1 ? 6 : 7) {
                final int p = period;
                String formattedPeriod = periodFormat.format(period);
                metricRegistry.register(MetricRegistry.name(getClass(), attribute, formattedPeriod + "Days"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
                    @Override
                    protected Long loadValue() {
                        DateTime minLastAccessed = DateTime.now(DateTimeZone.UTC).minusDays(p);
                        BasicDBObject query = new BasicDBObject();
                        query.put(attribute, BasicDBObjectBuilder.start("$gt", new Date(minLastAccessed.getMillis())).get());
                        return collection.getCount(query);
                    }
                });
            }
        }
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
