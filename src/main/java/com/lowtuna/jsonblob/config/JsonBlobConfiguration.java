package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import com.lowtuna.dropwizard.extras.config.MongoDbConfig;
import com.lowtuna.dropwizard.extras.config.MongoDbPropertiesConfig;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class JsonBlobConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("blobManager")
    private BlobManagerConfig blobManagerConfig = new BlobManagerConfig();

    @Valid
    @NotNull
    @JsonProperty("mongo")
    private MongoDbConfig mongoDbConfig = new MongoDbPropertiesConfig();

    @Valid
    @NotNull
    @JsonProperty("ga")
    private GoogleAnalyticsConfig googleAnalyticsConfig = new GoogleAnalyticsConfig();

    @Valid
    @JsonProperty("handlebars")
    private HandlebarsConfig handlebarsConfig = new ProdHandlebarsConfig();

}
