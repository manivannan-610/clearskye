package com.clearskye.epicconnector.service;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Service class for managing cache operations.
 * This service provides methods to cache and retrieve data.
 */
@Service
public class CacheService {
    /**
     * Logger instance for logging CacheService events.
     */
    private static final Logger logger = LogManager.getLogger(CacheService.class);
    /**
     * Cache for storing Epic access token details.
     */
    private final LoadingCache<String, String> epicTokenCache;

    /**
     * Constructs a new CacheService with the given LoadingCache.
     */
    public CacheService() {
        this.epicTokenCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String key) throws Exception {
                        return null;
                    }
                });
    }

    /**
     * Saves the specified data to the cache using the given key.
     *
     * <p>This method stores the data in the cache. </p>
     *
     * @param key  The key under which the data should be saved. Must not be null or empty.
     * @param data The data to be saved to the cache. Must not be null.
     */
    public final Boolean saveTokenToCache(String key, String data) {
        try {
            epicTokenCache.put(key, data);
            return Boolean.TRUE;
        } catch (Exception e) {
            logger.error(MessageFormat.format("Save epic access token details in cache is failed, because of error {0} : ", e.getMessage()));
        }
        return Boolean.FALSE;
    }

    /**
     * Retrieves the data from the cache using the specified key.
     *
     * <p>This method looks up the data stored in the cache under the given key
     * and returns it. If no data is found for the provided key, this method
     * returns {@code null}.</p>
     *
     * @param key The key used to retrieve the data from the cache. Must not be null or empty.
     */
    public final String verifyTokenAndGetDataFromCache(String key) throws ExecutionException {
        try {
            if (epicTokenCache.getIfPresent(key) != null) {
                return epicTokenCache.get(key);
            }
        } catch (Exception e) {
            logger.error(MessageFormat.format("Retrieve epic access token details from cache is failed, because of error {0} : ", e.getMessage()));
        }
        return null;
    }
}