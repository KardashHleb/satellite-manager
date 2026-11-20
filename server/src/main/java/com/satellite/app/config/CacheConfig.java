package com.satellite.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Redis cache settings. The cache manager itself is created by Spring Boot ({@code spring.cache.type=redis});
 * this class only provides the default configuration and the per-cache TTLs, so the same code also works with
 * {@code spring.cache.type=none} or {@code simple} in tests.
 * <p>
 * Evictions after writes go through {@code SatelliteCacheEvictionService}, which runs them after the transaction
 * commits and only logs cache errors. The cache manager is therefore not made {@code transactionAware()}:
 * a transaction-aware cache would defer an eviction issued from an after-commit callback once more, and it would
 * never run; it would also turn a Redis error after commit into a failed request.
 */
@Configuration
public class CacheConfig implements CachingConfigurer {

    public static final String SATELLITE = "satellite";
    public static final String CONSTELLATION = "constellation";
    public static final String SATELLITES = "satellites";
    public static final String ALL_SATELLITES_KEY = "all";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Default Redis cache configuration: JSON values, no caching of {@code null}, 10 minutes TTL.
     * Computed getters of the entities (for example {@code batteryPercentage}) are written to JSON but have
     * no setter, so unknown properties must be ignored when reading a value back.
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        serializer.configure(mapper -> mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10));
    }

    /** Per-cache TTLs, applied to the cache manager that Spring Boot builds. */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheTtlCustomizer(RedisCacheConfiguration defaults) {
        return builder -> builder.withInitialCacheConfigurations(Map.of(
                SATELLITE, defaults.entryTtl(Duration.ofMinutes(10)),
                CONSTELLATION, defaults.entryTtl(Duration.ofMinutes(15)),
                SATELLITES, defaults.entryTtl(Duration.ofMinutes(5))
        ));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get failed for cache={}, key={}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put failed for cache={}, key={}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict failed for cache={}, key={}: {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear failed for cache={}: {}", cache.getName(), exception.getMessage());
            }
        };
    }
}
