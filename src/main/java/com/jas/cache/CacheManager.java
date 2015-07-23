package com.jas.cache;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class CacheManager<K extends Serializable, V extends Serializable> {

    private MetaData<K, V> metaData;

    private Cache<K, V> cache;

    private Map<Object, Cache<Object, Set<K>>> indexCacheMap;

    private List<CacheManagerListener> listenerList = new ArrayList<CacheManagerListener>();

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private CacheEnum ref;

    public CacheEnum getCacheEnum() {
        return ref;
    }

    public CacheManager(CacheEnum ref, MetaData<K, V> metaData, Cache<K, V> cache) {
        this.ref = ref;
        this.metaData = metaData;
        this.cache = cache;
    }

    public CacheManager(CacheEnum ref, MetaData<K, V> metaData, Cache<K, V> cache, Map<Object, Cache<Object, Set<K>>> indexCacheMap) {
        this.ref = ref;
        this.metaData = metaData;
        this.cache = cache;
        this.indexCacheMap = indexCacheMap;
    }

    public void lock() {
        lock.writeLock().lock();
    }

    public void unlock() {
        lock.writeLock().unlock();
    }

    public void add(V value) {
        lock();
        try {
            K key = metaData.getKey(value);
            remove(key);
            cache.put(key, value);
            if (null != indexCacheMap) {
                for (Object index : indexCacheMap.keySet()) {
                    // set up object to insert
                    Object altkey = metaData.getAlternateKey(value, index);
                    if (null == altkey) break;
                    if (altkey instanceof MetaData.KeySet) {
                        for (Object each : (MetaData.KeySet) altkey) {
                            if (null == altkey) break;
                            cachePut(indexCacheMap.get(index), each, key);
                        }
                    } else {
                        cachePut(indexCacheMap.get(index), altkey, key);
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    private void cachePut(Cache<Object, Set<K>> indexCache, Object altkey, K key) {
        if (null == altkey || null == key || null == indexCache) return;
        Set<K> kset = indexCache.get(altkey);
        if (null == kset) {
            kset = new HashSet<K>();
        }
        kset.add(key);
        indexCache.put(altkey, kset);
    }

    private void cacheRemove(Cache<Object, Set<K>> indexCache, Object altkey, K key) {
        if (null == altkey || null == key || null == indexCache) return;
        Set<K> kset = indexCache.get(altkey);
        kset.remove(key);
        if (kset.isEmpty()) {
            indexCache.remove(altkey);
        } else {
            indexCache.put(altkey, kset);
        }
    }

    public void add(Collection<V> collection) {
        lock.writeLock().lock();
        try {
            for (V value : collection) {
                add(value);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void remove(K key) {
        lock();
        try {
            V value = get(key);
            cache.remove(key);

            // only needs to update alternate key if found value
            if(null!=value){
                if (null != indexCacheMap) {
                    for (Object index : indexCacheMap.keySet()) {
                        Object altkey = metaData.getAlternateKey(value, index);
                        if (null == altkey) break;
                        if (altkey instanceof MetaData.KeySet) {
                            for (Object each : (MetaData.KeySet) altkey) {
                                cacheRemove(indexCacheMap.get(index), each, key);
                            }
                        } else {
                            cacheRemove(indexCacheMap.get(index), altkey, key);
                        }
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    public void removeValue(V value) {
        K key = metaData.getKey(value);
        remove(key);
    }

    public void remove(Collection<K> collection) {
        lock();
        try {
            for (K key : collection) {
                remove(key);
            }
        } finally {
            unlock();
        }
    }

    public void removeValues(Collection<V> collection) {
        lock();
        try {
            for (V value : collection) {
                remove(metaData.getKey(value));
            }
        } finally {
            unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            cache.removeAll();
            if (null != indexCacheMap) {
                for (Cache<Object, Set<K>> indexCache : indexCacheMap.values()) {
                    indexCache.removeAll();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void reload(Collection<V> collection) {
        lock();
        try {
            clear();
            add(collection);
        } finally {
            unlock();
        }
    }

    public List<V> getAll() {
        lock();
        try {
            List<K> kset = cache.getKeys();
            List<V> vset = new ArrayList<V>();
            for (K key : kset) {
                vset.add(get(key));
            }
            return vset;
        } catch (RuntimeException e) {
            fireReadErrorOccurred(e);
            throw e;
        } finally {
            unlock();
        }
    }

    public List<K> getKeys() {
        lock();
        try {
            return cache.getKeys();
        } catch (RuntimeException e) {
            fireReadErrorOccurred(e);
            throw e;
        } finally {
            unlock();
        }
    }

    @SuppressWarnings("rawtypes")
    public List getAltKeys(Object index) {
        lock();
        try {
            Cache cache = indexCacheMap.get(index);
            return cache.getKeys();
        } catch (RuntimeException e) {
            fireReadErrorOccurred(e);
            throw e;
        } finally {
            unlock();
        }
    }

    public int size() {
        lock();
        try {
            return cache.size();
        } finally {
            unlock();
        }
    }

    public V get(K key) {
        lock();
        try {
            return cache.get(key);
        } catch (RuntimeException e) {
            fireReadErrorOccurred(e);
            throw e;
        } finally {
            unlock();
        }
    }

    public Set<V> get(Object index, Object altkey) {
        lock();
        try {
            if (null == indexCacheMap) return Collections.emptySet();
            Cache<Object, Set<K>> indexCache = indexCacheMap.get(index);
            Set<K> kset = indexCache.get(altkey);
            if (null == kset) return null;
            Set<V> vset = new HashSet<V>();
            for (K key : kset) {
                vset.add(get(key));
            }
            return vset;
        } catch (RuntimeException e) {
            fireReadErrorOccurred(e);
            throw e;
        } finally {
            unlock();
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n");
        buffer.append("----------------------------------------------------------------");
        buffer.append("\n");
        buffer.append("Cache Dump for: ").append(cache.getName());
        buffer.append("\n");
        buffer.append("\n");
        dumpCache(buffer, cache);
        if (indexCacheMap != null) {
            for (Object index : indexCacheMap.keySet()) {
                Cache<Object, Set<K>> cache = indexCacheMap.get(index);
                buffer.append("\n");
                buffer.append("-------------------------------");
                buffer.append("\n");
                buffer.append("Index Dump for: ").append(cache.getName()).append(" (").append(index).append(")");
                buffer.append("\n");
                buffer.append("\n");
                dumpCache(buffer, cache);
            }
        }
        return buffer.toString();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void dumpCache(StringBuffer buffer, Cache cache) {
        List l = cache.getKeys();
        for (Object key : l) {
            buffer.append("\n");
            buffer.append("--KEY--");
            buffer.append("\n");
            buffer.append(key);
            Object o = cache.get(key);
            buffer.append("\n");
            buffer.append("--VALUE--");
            buffer.append("\n");
            buffer.append(o);		// TODO Do JSON deserialization here?
            buffer.append("\n");
        }
    }

    public void shutdown() {
        System.out.println("CacheManager: In shutdown");
        cache.shutdown();
    }

    public boolean isWriteLocked() {
        return lock.isWriteLocked();
    }

    public void addListener(CacheManagerListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(CacheManagerListener listener) {
        listenerList.remove(listener);
    }

    public void fireReadErrorOccurred(Throwable throwable) {
        for (CacheManagerListener listener : listenerList) {
            listener.readErrorOccurred(ref, throwable);
        }
    }

}

