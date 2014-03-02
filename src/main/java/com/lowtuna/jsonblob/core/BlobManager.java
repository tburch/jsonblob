package com.lowtuna.jsonblob.core;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BlobManager implements Managed {
    public static final String UPDATED_ATTR_NAME = "updated";
    public static final String CREATED_ATTR_NAME = "created";
    public static final String ACCESSED_ATTR_NAME = "accessed";

    private final ScheduledExecutorService scheduledExecutorService;
    private final Duration blobCleanupFrequency;
    @Getter
    private final Duration blobAccessTtl;

    private final DBCollection collection;
    private final Timer createTimer;
    private final Timer readTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;
    private final Meter createMeter;
    private final Meter readMeter;
    private final Meter updateMeter;
    private final Meter deleteMeter;


    public BlobManager(DB mongoDb, String blobCollectionName, ScheduledExecutorService scheduledExecutorService, Duration blobCleanupFrequency, Duration blobAccessTtl, MetricRegistry metrics) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.blobCleanupFrequency = blobCleanupFrequency;
        this.blobAccessTtl = blobAccessTtl;

        this.collection = mongoDb.getCollection(blobCollectionName);
        this.createTimer = metrics.timer(MetricRegistry.name(getClass(), "create"));
        this.readTimer = metrics.timer(MetricRegistry.name(getClass(), "read"));
        this.updateTimer = metrics.timer(MetricRegistry.name(getClass(), "update"));
        this.deleteTimer = metrics.timer(MetricRegistry.name(getClass(), "delete"));
        this.createMeter = metrics.meter(MetricRegistry.name(getClass(), "create", "calls"));
        this.readMeter = metrics.meter(MetricRegistry.name(getClass(), "read", "calls"));
        this.updateMeter = metrics.meter(MetricRegistry.name(getClass(), "update", "calls"));
        this.deleteMeter = metrics.meter(MetricRegistry.name(getClass(), "delete", "calls"));

        metrics.register(MetricRegistry.name(getClass(), "blobCount"), new CachedGauge<Long>(1, TimeUnit.HOURS) {
            @Override
            protected Long loadValue() {
                return collection.count();
            }
        });
    }

    private BasicDBObject getDBObject(ObjectId objectId) {
        return new BasicDBObject("_id", objectId);
    }

    private DBObject createDBObject(String json, boolean setCreated) {
        final DateTime now = DateTime.now(DateTimeZone.UTC);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start(UPDATED_ATTR_NAME, new Date(now.getMillis()))
                .append(ACCESSED_ATTR_NAME, new Date(now.getMillis()))
                .append("blob", JSON.parse(json));

        if (setCreated) {
            builder = builder.append(CREATED_ATTR_NAME, new Date(now.getMillis()));
        }

        return builder.get();
    }

    public static boolean isValidJson(String json) {
        try {
            JSON.parse(json);
            return true;
        } catch (JSONParseException e) {
            return false;
        }
    }

    public DBObject create(String json) {
        createMeter.mark();
        try (Timer.Context timerContext = createTimer.time()) {
            log.debug("inserting blob with json='{}'", json);
            DBObject parsed = createDBObject(json, true);
            collection.insert(parsed);
            log.debug("successfully inserted blob of json as objectId='{}'", parsed.get("_id"));
            return parsed;
        }
    }

    public DBObject read(final ObjectId id) throws BlobNotFoundException {
        readMeter.mark();
        try (Timer.Context timerContext = readTimer.time()) {
            log.debug("attempting to retrieve blob with id='{}'", id);
            DBObject objectId = getDBObject(id);
            if (objectId != null) {
                log.debug("finding blob with objectId='{}'", objectId);
                final DBObject obj = collection.findOne(objectId);
                if (obj != null) {
                    final DateTime accessed = DateTime.now(DateTimeZone.UTC);
                    scheduledExecutorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            log.debug("updating last accessed time for block with objectId='{}' to {}", id, accessed);
                            BasicDBObject updatedAccessedDbObject = new BasicDBObject();
                            updatedAccessedDbObject.append("$set", new BasicDBObject().append(ACCESSED_ATTR_NAME, new Date(accessed.getMillis())));
                            collection.update(obj, updatedAccessedDbObject, false, false);
                            log.debug("updated last accessed time for block with objectId='{}' to {}", id, accessed);
                        }
                    });
                    return obj;
                }
            }
            log.debug("couldn't retrieve blob with id='{}'", id);
            throw new BlobNotFoundException(id);
        }
    }

    public DBObject update(ObjectId id, String json) throws BlobNotFoundException {
        updateMeter.mark();
        try (Timer.Context timerContext = updateTimer.time()) {
            log.debug("attempting to update blob with id='{}' and json='{}'", id, json);
            DBObject objectId = getDBObject(id);
            if (objectId != null) {
                log.debug("finding blob to update with objectId='{}'", objectId);
                DBObject obj = collection.findOne(objectId);
                if (obj != null) {
                    DBObject parsed = createDBObject(json, false);
                    collection.update(obj, parsed);
                    log.debug("successfully updated blob of json with objectId='{}'", id);
                    return parsed;
                }
            }
            log.debug("couldn't update blob with id='{}'", id);
            throw new BlobNotFoundException(id);
        }
    }

    public boolean delete(ObjectId id) throws BlobNotFoundException {
        deleteMeter.mark();
        try (Timer.Context timerContext = deleteTimer.time();) {
            log.debug("attempting to delete blob with id='{}'", id);
            DBObject objectId = getDBObject(id);
            if (objectId != null) {
                log.debug("finding blob to delete with objectId='{}'", objectId);
                DBObject obj = collection.findOne(objectId);
                if (obj != null) {
                    WriteResult result = collection.remove(obj);
                    boolean removed = result.getN() > 0 && result.getLastError().ok();
                    if (removed) {
                        log.debug("successfully removed {} blob(s) of json with objectId='{}'", result.getN(), id);
                    } else {
                        log.debug("did not remove any blob(s) of json with objectId='{}'", id);
                    }
                    return removed;
                }
            }
            log.debug("couldn't remove blob with id='{}'", id);
            throw new BlobNotFoundException(id);
        }
    }

    @Override
    public void start() throws Exception {
        BlobCleanupJob blobCleanupJob = new BlobCleanupJob(collection, blobAccessTtl);
        scheduledExecutorService.scheduleWithFixedDelay(
                blobCleanupJob,
                0,
                blobCleanupFrequency.getQuantity(),
                blobCleanupFrequency.getUnit());
    }

    @Override
    public void stop() throws Exception {
        // nothing to do
    }
}
