package be.ordina.msdashboard.cache;

import be.ordina.msdashboard.constants.Constants;
import org.springframework.cache.annotation.CacheEvict;

/**
 * @author Andreas Evers
 */
public interface NodeCache {
    @CacheEvict(value = Constants.HEALTH_CACHE_NAME, allEntries = true)
    void evictHealthsCache();

    @CacheEvict(value = Constants.INDEX_CACHE_NAME, allEntries = true)
    void evictIndexesCache();

    @CacheEvict(value = Constants.PACTS_CACHE_NAME, allEntries = true)
    void evictPactsCache();
}
