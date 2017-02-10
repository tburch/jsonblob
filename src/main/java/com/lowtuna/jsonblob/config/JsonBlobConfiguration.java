package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lowtuna.dropwizard.extras.config.GoogleAnalyticsConfig;
import com.lowtuna.jsonblob.config.view.HandlebarsConfig;
import com.lowtuna.jsonblob.config.view.ProdHandlebarsConfig;
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
  @JsonProperty("ga")
  private GoogleAnalyticsConfig googleAnalyticsConfig = new GoogleAnalyticsConfig();

  @Valid
  @JsonProperty("handlebars")
  private HandlebarsConfig handlebarsConfig = new ProdHandlebarsConfig();

}
