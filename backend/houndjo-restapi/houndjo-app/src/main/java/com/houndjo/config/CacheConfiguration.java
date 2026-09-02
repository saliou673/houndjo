package com.houndjo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's cache abstraction and declares the application's named caches.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String USER_LANGUAGE_KEY_CACHE = "userLanguageKey";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(USER_LANGUAGE_KEY_CACHE);
    }
}
