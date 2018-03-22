package org.aion.mcf.trie.doublearray;

import org.aion.base.db.IByteArrayKeyValueStore;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.trie.Cache;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DATCacheImpl implements Cache<Integer, byte[]> {
    private static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.DB.name());

    private IByteArrayKeyValueStore dataSource;

    private Map<Integer, byte[]> nodes = new HashMap<>();
    private Map<Integer, Object> nodesHashes = new HashMap<>();

    @Override
    public Object put(Object o) {
        return null;
    }

    @Override
    public byte[] get(Integer key) {
        return new byte[0];
    }

    @Override
    public void delete(Integer key) {

    }

    @Override
    public void commit(boolean flush) {

    }

    @Override
    public void undo() {

    }

    @Override
    public Map<Integer, byte[]> getNodes() {
        return null;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public IByteArrayKeyValueStore getDb() {
        return null;
    }

    @Override
    public void setDB(IByteArrayKeyValueStore kvds) {

    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public void markRemoved(Integer key) {

    }
}
