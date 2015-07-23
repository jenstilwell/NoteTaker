package com.jas.cache;

import java.util.List;

public interface Cache<K, V> {
    String getName();
    V get(K key);
    List<K> getKeys();
    int size();
    void put(K key, V value);
    void remove(K key);
    void removeAll();
    void shutdown();
}
