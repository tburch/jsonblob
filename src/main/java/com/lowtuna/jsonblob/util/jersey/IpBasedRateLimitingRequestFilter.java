package com.lowtuna.jsonblob.util.jersey;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.RateLimiter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.dropwizard.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class IpBasedRateLimitingRequestFilter implements ContainerRequestFilter {
    private final LoadingCache<String, RateLimiter> rateLimiterBySourceIp = CacheBuilder.newBuilder()
            .recordStats()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) throws Exception {
                    return RateLimiter.create(rateLimit.toSeconds());
                }
            });

    private final Duration rateLimit;
    private final Duration rateLimitTimeout;
    private final Timer waitTimer;
    private final Meter timeoutExceededMeter;
    private final RateLimiter defaultRateLimiter;
    private final String originatingIpHeader;

    @Context
    private HttpServletRequest httpRequest;

    public IpBasedRateLimitingRequestFilter(Duration rateLimit, Duration rateLimitTimeout, MetricRegistry metricRegistry) {
        this (rateLimit, rateLimitTimeout, metricRegistry, "X-Forwarded-For");
    }

    public IpBasedRateLimitingRequestFilter(Duration rateLimit, Duration rateLimitTimeout, MetricRegistry metricRegistry, String originatingIpHeader) {
        this.rateLimit = rateLimit;
        this.rateLimitTimeout = rateLimitTimeout;
        this.originatingIpHeader = originatingIpHeader;

        this.defaultRateLimiter = RateLimiter.create(rateLimit.toSeconds());
        this.timeoutExceededMeter = metricRegistry.meter(MetricRegistry.name(getClass(), "timeoutExceeded"));
        this.waitTimer = metricRegistry.timer(MetricRegistry.name(getClass(), "wait"));

        metricRegistry.register(MetricRegistry.name(getClass(), "rateLimiter", "cache", "size"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return rateLimiterBySourceIp.size();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "rateLimiter", "cache", "hits"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return rateLimiterBySourceIp.stats().hitCount();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "rateLimiter", "cache", "misses"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return rateLimiterBySourceIp.stats().missCount();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "rateLimiter", "cache", "evictions"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return rateLimiterBySourceIp.stats().evictionCount();
            }
        });

        metricRegistry.register(MetricRegistry.name(getClass(), "rateLimiter", "cache", "loadPenalty"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return rateLimiterBySourceIp.stats().averageLoadPenalty();
            }
        });
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        String sourceIpHeaderValue = request.getHeaderValue(originatingIpHeader);
        RateLimiter rateLimiter = defaultRateLimiter;
        String sourceIp = "unknown";
        if (StringUtils.isNotEmpty(sourceIpHeaderValue)) {
            Iterable<String> sourceIps = Arrays.asList(sourceIpHeaderValue.split(","));
            try {
                sourceIp = Iterables.getLast(sourceIps);
                InetAddress.getByName(sourceIp);
                rateLimiter = rateLimiterBySourceIp.getUnchecked(sourceIp);
                log.debug("Using cached RateLimiter for source ip {}", sourceIp);
            } catch (NoSuchElementException e) {
                log.debug("Using default RateLimiter because a source ip could not be found in {}", sourceIps);
            } catch (UnknownHostException e) {
                log.debug("Using default RateLimiter because a source ip {} is not a valid InetAddress", sourceIps);
            }
        }

        if (!rateLimiter.tryAcquire(rateLimitTimeout.getQuantity(), rateLimitTimeout.getUnit())) {
            timeoutExceededMeter.mark();
            log.debug("Returning a 429 status code - wasn't able to acquire within the timeout={}", rateLimitTimeout);
            throw new WebApplicationException(Response.status(429).entity("Rate limit of " + rateLimiter.getRate() + " requests per second exceeded for your ip " + sourceIp).build());
        }

        double waitMillis = rateLimiter.acquire() * 1000;
        waitTimer.update((long) waitMillis, TimeUnit.MILLISECONDS);
        return request;
    }
}
