package com.jas.cache;

public interface CacheShutdownListener {
    /**
     * Called when shutting down the cache
     */
    public void shutdown();
}
