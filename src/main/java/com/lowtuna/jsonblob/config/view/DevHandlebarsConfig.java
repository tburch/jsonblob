package com.lowtuna.jsonblob.config.view;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.NullTemplateCache;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class DevHandlebarsConfig extends HandlebarsConfig {

  @JsonProperty
  @NotEmpty
  private String templateBaseDir;

  @Override
  @JsonIgnore
  public Handlebars createInstance() {
    return new Handlebars().with(new FileTemplateLoader(templateBaseDir, StringUtils.EMPTY));
  }

  @Override
  protected Handlebars setupTemplateCache(Handlebars handlebars, MetricRegistry metricRegistry) {
    return handlebars.with(NullTemplateCache.INSTANCE);
  }
}
