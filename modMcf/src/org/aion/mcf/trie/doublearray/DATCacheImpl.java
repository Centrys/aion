package org.aion.mcf.trie.doublearray;

import org.aion.base.db.IByteArrayKeyValueStore;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DATCacheImpl {
    private static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.DB.name());

    private IByteArrayKeyValueStore dataSource;

    private Map<Integer, byte[]> nodes = new HashMap<>();
    private Map<Integer, Object> nodesHashes = new HashMap<>();
}
