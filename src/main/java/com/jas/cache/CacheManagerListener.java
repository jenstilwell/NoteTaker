package com.jas.cache;


public interface CacheManagerListener {
    public void readErrorOccurred(CacheEnum cache, Throwable exception);
}
