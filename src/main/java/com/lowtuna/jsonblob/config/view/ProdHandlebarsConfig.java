package com.lowtuna.jsonblob.config.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class ProdHandlebarsConfig extends HandlebarsConfig {

    @NotEmpty
    @JsonProperty
    private String classPathTemplatesBaseDir = "/views";

    @Override
    @JsonIgnore
    public Handlebars createInstance() {
        Handlebars hbs = new Handlebars()
                .with(new ClassPathTemplateLoader(classPathTemplatesBaseDir, StringUtils.EMPTY))
                .with(new HighConcurrencyTemplateCache());
        return hbs;
    }

}
