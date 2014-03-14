package com.lowtuna.jsonblob.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class RateLimiterConfig {

    @JsonProperty
    @NotNull
    private Duration rateLimit = Duration.seconds(10);

    @JsonProperty
    @NotNull
    private Duration rateLimitTimeout = Duration.seconds(5);

}
