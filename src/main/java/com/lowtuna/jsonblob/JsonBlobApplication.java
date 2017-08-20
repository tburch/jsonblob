package com.lowtuna.jsonblob;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Jackson2Helper;
import com.lowtuna.dropwizard.extras.heroku.RequestIdFilter;
import com.lowtuna.dropwizard.extras.view.handlebars.ConfiguredHandlebarsViewBundle;
import com.lowtuna.jsonblob.config.JsonBlobConfiguration;
import com.lowtuna.jsonblob.core.FileSystemJsonBlobManager;
import com.lowtuna.jsonblob.health.BlobDirectoryFreeSpaceHealthcheck;
import com.lowtuna.jsonblob.resource.ApiResource;
import com.lowtuna.jsonblob.resource.JsonBlobEditorResource;
import com.lowtuna.jsonblob.util.jersey.GitTipHeaderFilter;
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
        Handlebars handlebars = configuration.getHandlebarsConfig().getInstance(bootstrap.getMetricRegistry());
        handlebars.registerHelper("json", new Jackson2Helper(bootstrap.getObjectMapper()));
        return handlebars;
      }
    });
  }

  @Override
  public void run(JsonBlobConfiguration configuration, Environment environment) throws ClassNotFoundException {
    environment.metrics().register(MetricRegistry.name(getClass(), "uptime"), (Gauge<String>) () -> uptimePeriodFormatter.print(new Duration(System.currentTimeMillis() - startTIme).toPeriod()));

    ScheduledExecutorService scheduledExecutorService = configuration.getBlobManagerConfig().getScheduledExecutorService().instance(environment);
    ScheduledExecutorService cleanupScheduledExecutorService = configuration.getBlobManagerConfig().getCleanupScheduledExecutorService().instance(environment);

    FileSystemJsonBlobManager fileSystemBlobManager = new FileSystemJsonBlobManager(configuration.getBlobManagerConfig().getFileSystemBlogDataDirectory(), scheduledExecutorService, cleanupScheduledExecutorService, environment.getObjectMapper(), configuration.getBlobManagerConfig().getBlobAccessTtl(), configuration.getBlobManagerConfig().isDeleteEnabled(), environment.metrics());
    environment.lifecycle().manage(fileSystemBlobManager);

    environment.healthChecks().register("freeSpace", new BlobDirectoryFreeSpaceHealthcheck(configuration.getBlobManagerConfig().getFileSystemBlogDataDirectory(), 5242880));

    environment.jersey().register(new ApiResource(fileSystemBlobManager, configuration.getGoogleAnalyticsConfig()));
    environment.jersey().register(new JsonBlobEditorResource(fileSystemBlobManager, configuration.getGoogleAnalyticsConfig(), configuration.getBlobManagerConfig().getBlobAccessTtl()));

    environment.jersey().getResourceConfig().getContainerResponseFilters().add(new GitTipHeaderFilter());
    environment.jersey().getResourceConfig().getContainerRequestFilters().add(new RequestIdFilter("X-Request-ID"));

    // Support CORS
    environment.jersey().getResourceConfig().getContainerResponseFilters().add((ContainerResponseFilter) (request, response) -> {
      MultivaluedMap headers = response.getHttpHeaders();
      headers.add("Access-Control-Allow-Origin", "*");
      headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,HEAD,OPTIONS");
      headers.add("Access-Control-Expose-Headers", "X-Requested-With,X-jsonblob,X-Hello-Human,Location,Date,Content-Type,Accept,Origin");

      String reqHead = request.getHeaderValue("Access-Control-Request-Headers");
      if (StringUtils.isNotEmpty(reqHead)) {
        headers.add("Access-Control-Allow-Headers", reqHead);
      }

      return response;
    });
  }

}
