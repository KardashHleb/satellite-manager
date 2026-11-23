package com.satellite.app.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Redis cache settings. The cache manager itself is created by Spring Boot ({@code spring.cache.type=redis});
 * this class provides the default configuration and the per-cache TTLs, so the same code also works with
 * {@code spring.cache.type=none} or {@code simple} in tests.
 * <p>
 * The cache manager is made transaction-aware ({@link ResilientTransactionAwareCacheManager}): cache writes and
 * evictions issued inside a transaction run after it commits and are skipped on rollback. Cache errors are only
 * logged, both in the cache annotations ({@link #errorHandler()}) and in the deferred after-commit operations.
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

    /**
     * Wraps the cache manager that Spring Boot creates (Redis, or the in-memory / no-op one in tests) into a
     * transaction-aware one. A post-processor is used because the manager itself is auto-configured.
     */
    @Bean
    public static BeanPostProcessor transactionAwareCacheManagerPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof CacheManager cacheManager
                        && !(bean instanceof TransactionAwareCacheManagerProxy)) {
                    return new ResilientTransactionAwareCacheManager(cacheManager);
                }
                return bean;
            }
        };
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
