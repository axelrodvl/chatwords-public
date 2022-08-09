package co.axelrod.chatwords.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWordsCacheManager {
    private static final int CACHE_EVICTION_RATE_ONCE_AN_HOUR = 2 * 60 * 1000;

    private final CacheManager cacheManager;

    public void evictAllCacheValues(String cacheName) {
        log.info("Evicting cache: " + cacheName);
        cacheManager.getCache(cacheName).clear();
        log.info("Cache evicted: " + cacheName);
    }

    @Scheduled(fixedRate = CACHE_EVICTION_RATE_ONCE_AN_HOUR)
    public void evictCaches() {
        log.info("Evicting caches");
        ChatWordsCache.getCachesToEvict().forEach(this::evictAllCacheValues);
    }
}
