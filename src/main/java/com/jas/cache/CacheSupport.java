package com.jas.cache;

import java.util.Date;

public interface CacheSupport {
    public void addCacheListener(CacheEnum cache, CacheManagerListener listener);
    public void update(CacheEnum cache, Date updateDate) throws CacheException;

    /**
     * This will update the cache based on an offset and limit. The boolean returned will
     * tell if all rows were pulled, or if rows still need to be pulled
     * Update the cache
     * @param cache
     * @param updateDate
     * @param offset of data to get
     * @param limit the number of rows returned
     * @return true if all rows were updated, false if more rows exist to be pulled
     * @throws CacheException
     */
    public boolean update(CacheEnum cache, Date updateDate, int offset, int limit) throws CacheException;

    /**
     * Shutdown the cache. Called when cleanly exiting the application.
     */
    public void shutdown();
}
