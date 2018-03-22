package org.aion.mcf.trie;

import org.aion.base.db.IByteArrayKeyValueStore;

import java.util.Map;

public interface Cache<K, V> {
    Object put(Object o);
    V get(K key);
    void delete(K key);
    void commit(boolean flush);
    void undo();
    Map<K, V> getNodes();
    boolean isDirty();
    IByteArrayKeyValueStore getDb(); // why ?
    void setDB(IByteArrayKeyValueStore kvds);
    int getSize();
    void markRemoved(K key);
}
