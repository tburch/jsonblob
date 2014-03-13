package com.lowtuna.jsonblob.task;

import com.google.common.collect.ImmutableMultimap;
import com.lowtuna.jsonblob.core.BlobManager;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import io.dropwizard.servlets.tasks.Task;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;

public class UpdateAccessedTimesTask extends Task {
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final DBCollection collection;

    public UpdateAccessedTimesTask(DBCollection collection) {
        super(UpdateAccessedTimesTask.class.getSimpleName());
        this.collection = collection;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        Collection<String> accessedParams = parameters.get("accessed");
        if (accessedParams != null && !accessedParams.isEmpty()) {
            String dateString = accessedParams.iterator().next();
            DateTime dateTime = fmt.withZoneUTC().parseDateTime(dateString);

            BasicDBObjectBuilder exists = BasicDBObjectBuilder.start("$exists", false);
            BasicDBObjectBuilder query = BasicDBObjectBuilder.start(BlobManager.ACCESSED_ATTR_NAME, exists.get());
            BasicDBObjectBuilder setValue = BasicDBObjectBuilder.start(BlobManager.ACCESSED_ATTR_NAME, new Date(dateTime.getMillis()));
            BasicDBObjectBuilder set = BasicDBObjectBuilder.start("$set", setValue.get());

            WriteResult result = collection.update(query.get(), set.get(), false, true);
            output.append("updated " + result.getN() + " blobs");
        }
    }
}
