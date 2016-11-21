package com.lowtuna.jsonblob;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.github.jknack.handlebars.Handlebars;
import com.lowtuna.dropwizard.extras.heroku.RequestIdFilter;
import com.lowtuna.dropwizard.extras.view.handlebars.ConfiguredHandlebarsViewBundle;
import com.lowtuna.jsonblob.config.JsonBlobConfiguration;
import com.lowtuna.jsonblob.core.BlobMigrationJob;
import com.lowtuna.jsonblob.core.FileSystemJsonBlobManager;
import com.lowtuna.jsonblob.core.MongoDbJsonBlobManager;
import com.lowtuna.jsonblob.healthcheck.CreateDeleteBlobHealthCheck;
import com.lowtuna.jsonblob.healthcheck.MongoHealthCheck;
import com.lowtuna.jsonblob.resource.ApiResource;
import com.lowtuna.jsonblob.resource.JsonBlobEditorResource;
import com.lowtuna.jsonblob.util.jersey.GitTipHeaderFilter;
import com.lowtuna.jsonblob.util.mongo.JacksonMongoDbModule;
import com.mongodb.DB;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.ws.rs.core.MultivaluedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JsonBlobApplication extends Application<JsonBlobConfiguration> {

    private final long startTIme = System.currentTimeMillis();
    private final PeriodFormatter uptimePeriodFormatter = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix("d")
            .appendHours()
            .appendSuffix("h")
            .appendMinutes()
            .appendSuffix("m")
            .appendSeconds()
            .appendSuffix("s")
            .toFormatter();

    public static void main(String[] args) throws Exception {
        if (args.length >= 2 && args[1].startsWith("~")) {
            args[1] = System.getProperty("user.home") + args[1].substring(1);
        }
        new JsonBlobApplication().run(args);
    }

    @Override
    public String getName() {
        return "jsonblob";
    }

    @Override
    public void initialize(final Bootstrap<JsonBlobConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());

        bootstrap.addBundle(new ConfiguredHandlebarsViewBundle<JsonBlobConfiguration>() {
            @Override
            public Handlebars getInstance(JsonBlobConfiguration configuration) {
                log.info("Using Handlebars configuration of {}", configuration.getHandlebarsConfig().getClass().getCanonicalName());
                return configuration.getHandlebarsConfig().getInstance(bootstrap.getMetricRegistry());
            }
        });
    }

    @Override
    public void run(JsonBlobConfiguration configuration, Environment environment) throws ClassNotFoundException {
        environment.metrics().register(MetricRegistry.name(getClass(), "uptime"), new Gauge<String>() {
            @Override
            public String getValue() {
                return uptimePeriodFormatter.print(new Duration(System.currentTimeMillis() - startTIme).toPeriod());
            }
        });

        environment.getObjectMapper().registerModule(new JacksonMongoDbModule());

        DB mongoDBInstance = configuration.getMongoDbConfig().instance();

        ScheduledExecutorService scheduledExecutorService = configuration.getBlobManagerConfig().getScheduledExecutorService().instance(environment);

        MongoDbJsonBlobManager mongoDbBlobManager = new MongoDbJsonBlobManager(
                mongoDBInstance,
                configuration.getBlobManagerConfig().getBlobCollectionName(),
                scheduledExecutorService,
                configuration.getBlobManagerConfig().getBlobCleanupFrequency(),
                configuration.getBlobManagerConfig().getBlobAccessTtl(),
                environment.metrics()
        );
        environment.lifecycle().manage(mongoDbBlobManager);

        FileSystemJsonBlobManager fileSystemBlobManager = new FileSystemJsonBlobManager(configuration.getBlobManagerConfig().getFileSystemBlogDataDirectory());

        environment.healthChecks().register("MongoDB", new MongoHealthCheck(mongoDBInstance));
        environment.healthChecks().register("MongoDbJsonBlobManager", new CreateDeleteBlobHealthCheck(fileSystemBlobManager));

        environment.jersey().register(new ApiResource(mongoDbBlobManager, fileSystemBlobManager, configuration.getGoogleAnalyticsConfig()));
        environment.jersey().register(new JsonBlobEditorResource(fileSystemBlobManager, mongoDbBlobManager, configuration.getGoogleAnalyticsConfig()));
        environment.jersey().getResourceConfig().getContainerResponseFilters().add(new GitTipHeaderFilter());
        environment.jersey().getResourceConfig().getContainerRequestFilters().add(new RequestIdFilter("X-Request-ID"));

        // Support CORS
        environment.jersey().getResourceConfig().getContainerResponseFilters().add(new ContainerResponseFilter() {
            @Override
            public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
                MultivaluedMap headers = response.getHttpHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,HEAD,OPTIONS");
                headers.add("Access-Control-Expose-Headers", "X-Requested-With,X-jsonblob,X-Hello-Human,Location,Date,Content-Type,Accept,Origin");

                String reqHead = request.getHeaderValue("Access-Control-Request-Headers");
                if (StringUtils.isNotEmpty(reqHead)) {
                    headers.add("Access-Control-Allow-Headers", reqHead);
                }

                return response;
            }
        });

        scheduledExecutorService.scheduleWithFixedDelay(new BlobMigrationJob(mongoDbBlobManager, fileSystemBlobManager), 0, 1, TimeUnit.MINUTES);
    }

}
