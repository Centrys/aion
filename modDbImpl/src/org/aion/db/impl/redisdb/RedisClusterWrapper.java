package org.aion.db.impl.redisdb;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.db.impl.AbstractDB;
import org.aion.db.impl.DBConstants;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class RedisClusterWrapper extends AbstractDB {
    private List<Pipeline> pipelineArray = new ArrayList<>(RedisClusterConstants.DATABASE_COUNT);

    public RedisClusterWrapper(String name, String path, boolean enableDbCache, boolean enableDbCompression) {
        super(name, path, enableDbCache, enableDbCompression);

        for (int i = 0; i < RedisClusterConstants.DATABASE_COUNT; i++) {
            pipelineArray.add(RedisClusterConstants.getDb(i).pipelined());
        }
    }

    private int dbNum(byte[] key) {
        Checksum checksum = new CRC32();
        checksum.update(key);
        return (int) (checksum.getValue() % RedisClusterConstants.DATABASE_COUNT);
    }

    @Override
    public boolean commitCache(Map<ByteArrayWrapper, byte[]> cache) {
        try {
            for (Map.Entry<ByteArrayWrapper, byte[]> e : cache.entrySet()) {
                Pipeline tmpBatch = pipelineArray.get(dbNum(e.getKey().getData()));

                if (e.getValue() == null) {
                    tmpBatch.del(new String(e.getKey().getData(), DBConstants.CHARSET));
                } else {
                    tmpBatch.set(new String(e.getKey().getData(), DBConstants.CHARSET), new String(e.getValue(), DBConstants.CHARSET));
                }
            }

            for (Pipeline pipeline : pipelineArray)
                pipeline.sync();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected byte[] getInternal(byte[] key) {
        try (Jedis db = RedisClusterConstants.getDb(dbNum(key))) {
            String value = db.get(new String(key, DBConstants.CHARSET));
            if (value != null) {
                return value.getBytes(DBConstants.CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean open() {
        return true;
    }

    @Override
    public boolean isOpen() {
        boolean connected = true;

        for (int i = 0; i < RedisClusterConstants.DATABASE_COUNT; i++) {
            try (Jedis db = RedisClusterConstants.getDb(i)) {
                connected = connected && db.isConnected();
            }
        }

        return connected;
    }

    @Override
    public boolean isCreatedOnDisk() {
        return true;
    }

    @Override
    public long approximateSize() {
        check();

        long dbSize = 0;
        for (int i = 0; i < RedisClusterConstants.DATABASE_COUNT; i++) {
            try (Jedis db = RedisClusterConstants.getDb(i)) {
                dbSize += db.keys("*").size();
            }
        }

        return dbSize;
    }

    @Override
    public boolean isEmpty() {
        check();

        long dbSize = 0;
        for (int i = 0; i < RedisClusterConstants.DATABASE_COUNT; i++) {
            try (Jedis db = RedisClusterConstants.getDb(i)) {
                dbSize += db.keys("*").size();
            }
        }

        return dbSize <= 0;
    }

    @Override
    public Set<byte[]> keys() {
        check();

        Set<byte[]> set = new HashSet<>();
        for (int i = 0; i < RedisClusterConstants.DATABASE_COUNT; i++) {
            try (Jedis db = RedisClusterConstants.getDb(i)) {
                Set<String> keysAsString = db.keys("*");
                for (String key : keysAsString) {
                    try {
                        set.add(key.getBytes(DBConstants.CHARSET));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return set;
    }

    @Override
    public void put(byte[] key, byte[] value) {
        check(key);
        check();

        try (Jedis db = RedisClusterConstants.getDb(dbNum(key))) {
            db.set(new String(key, DBConstants.CHARSET), new String(value, DBConstants.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(byte[] key) {
        check(key);
        check();

        try (Jedis db = RedisClusterConstants.getDb(dbNum(key))) {
            db.del(new String(key, DBConstants.CHARSET));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putBatch(Map<byte[], byte[]> inputMap) {
        check(inputMap.keySet());
        check();

        try {
            for (Map.Entry<byte[], byte[]> e : inputMap.entrySet()) {
                byte[] key = e.getKey();
                byte[] value = e.getValue();

                Pipeline tmpBatch = pipelineArray.get(dbNum(key));

                if (e.getValue() == null) {
                    tmpBatch.del(new String(key, DBConstants.CHARSET));
                } else {
                    tmpBatch.set(new String(key, DBConstants.CHARSET), new String(value, DBConstants.CHARSET));
                }
            }

            for (Pipeline pipeline : pipelineArray)
                pipeline.sync();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putToBatch(byte[] key, byte[] value) {
        check(key);
        check();

        try {
            Pipeline tmpBatch = pipelineArray.get(dbNum(key));
            if (value == null) {
                tmpBatch.del(new String(key, DBConstants.CHARSET));
            } else {
                tmpBatch.set(new String(key, DBConstants.CHARSET), new String(value, DBConstants.CHARSET));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commitBatch() {
        for (Pipeline pipeline : pipelineArray)
            pipeline.sync();
    }

    @Override
    public void deleteBatch(Collection<byte[]> keys) {
        check(keys);
        check();

        try {
            for(byte[] key: keys) {
                Pipeline tmpBatch = pipelineArray.get(dbNum(key));
                tmpBatch.del(new String(key, DBConstants.CHARSET));
            }

            for (Pipeline pipeline : pipelineArray)
                pipeline.sync();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        RedisClusterConstants.closeDbs();
    }
}
