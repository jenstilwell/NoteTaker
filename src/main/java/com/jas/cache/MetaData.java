package com.jas.cache;

import java.util.HashSet;

public interface MetaData<K, V> {
    K getKey(V value);
    Object getAlternateKey(V value, Object index);
    @SuppressWarnings("serial")
    class KeySet extends HashSet<Object> { }
}
