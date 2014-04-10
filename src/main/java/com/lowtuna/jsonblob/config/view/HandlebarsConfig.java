package com.lowtuna.jsonblob.config.view;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.GuavaTemplateCache;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateSource;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lowtuna.jsonblob.util.mustache.Base64StringHelpers;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = false, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DevHandlebarsConfig.class, name = "dev"),
        @JsonSubTypes.Type(value = ProdHandlebarsConfig.class, name = "prod")
})

@JsonIgnoreProperties(ignoreUnknown = true, value = "type")
public abstract class HandlebarsConfig {

    @JsonIgnore
    public Handlebars getInstance(MetricRegistry metricRegistry) {
        Handlebars handlebars = createInstance();
        handlebars = setupTemplateCache(handlebars, metricRegistry);
        StringHelpers.register(handlebars);
        Base64StringHelpers.register(handlebars);
        return handlebars;
    }

    protected Handlebars setupTemplateCache(Handlebars handlebars, MetricRegistry metricRegistry) {
        final Cache<TemplateSource, Template> templateCache = CacheBuilder.newBuilder().recordStats().build();

        metricRegistry.register(MetricRegistry.name(GuavaTemplateCache.class, "size"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return templateCache.size();
            }
        });
        metricRegistry.register(MetricRegistry.name(GuavaTemplateCache.class, "hits"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return templateCache.stats().hitCount();
            }
        });
        metricRegistry.register(MetricRegistry.name(GuavaTemplateCache.class, "misses"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return templateCache.stats().missCount();
            }
        });
        metricRegistry.register(MetricRegistry.name(GuavaTemplateCache.class, "eviction-count"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return templateCache.stats().evictionCount();
            }
        });
        metricRegistry.register(MetricRegistry.name(GuavaTemplateCache.class, "average-load-penalty"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return templateCache.stats().averageLoadPenalty();
            }
        });

        return handlebars.with(new GuavaTemplateCache(templateCache));
    }

    @JsonIgnore
    public abstract Handlebars createInstance();

}
