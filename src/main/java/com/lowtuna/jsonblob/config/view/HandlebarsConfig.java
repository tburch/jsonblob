package com.lowtuna.jsonblob.config.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.StringHelpers;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = false, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DevHandlebarsConfig.class, name = "dev"),
        @JsonSubTypes.Type(value = ProdHandlebarsConfig.class, name = "prod")
})

@JsonIgnoreProperties(ignoreUnknown = true, value = "type")
public abstract class HandlebarsConfig {

    @JsonIgnore
    public Handlebars getInstance() {
        Handlebars handlebars = createInstance();
        StringHelpers.register(handlebars);
        return handlebars;
    }

    @JsonIgnore
    public abstract Handlebars createInstance();
}
