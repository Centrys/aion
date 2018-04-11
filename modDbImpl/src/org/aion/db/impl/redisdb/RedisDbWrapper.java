package org.aion.db.impl.redisdb;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.db.impl.AbstractDB;

import java.util.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RedisDbWrapper extends AbstractDB {

    private Jedis database;

    protected RedisDbWrapper(String name) {
        super(name);
        database = new Jedis("localhost");
        LOG.info("Connection with the redis server finished...");
        LOG.info("Server is running: " + database.ping());
    }

    public RedisDbWrapper(String name, String path, boolean enableDbCache, boolean enableDbCompression) {
        super(name, path, enableDbCache, enableDbCompression);
    }

    @Override
    public boolean commitCache(Map<ByteArrayWrapper, byte[]> cache) {
        for (Map.Entry<ByteArrayWrapper, byte[]> e : cache.entrySet()) {
            if (e.getValue() == null) {
                this.delete(e.getKey().getData());
            } else {
                this.put(e.getKey().getData(), e.getValue());
            }
        }

        return true;
    }

    @Override
    protected byte[] getInternal(byte[] k) {
        return database.get(k);
    }

    @Override
    public boolean open() {
        if(isOpen()){
            return true;
        }

        database = new Jedis("localhost");
        LOG.info("Connection with the redis server finished...");
        LOG.info("Server is running: " + database.ping());

        return isOpen();
    }

    @Override
    public void close() {
        if(database == null){
            return;
        }

        database.close();
        database = null;
    }

    @Override
    public boolean isOpen() {
        return database != null && database.isConnected();
    }

    @Override
    public boolean isCreatedOnDisk() {
        //TODO @Robert this being a ram Database with snapshoting it will manage the disk storage itself
        return isOpen();
    }

    @Override
    public long approximateSize() {
        return database.dbSize();
    }

    @Override
    public boolean isEmpty() {
        return database.dbSize() == 0;
    }

    @Override
    public Set<byte[]> keys() {
        Set<String> names = database.keys("*");
        Set<byte[]> keys = new LinkedHashSet<>();

        for (String s : names) {
            keys.add(s.getBytes());
        }
        return keys;
    }

    @Override
    public void put(byte[] bytes, byte[] bytes2) {
        check(bytes);

        check();

        database.set(bytes, bytes2);
    }

    @Override
    public void delete(byte[] bytes) {
        check(bytes);

        check();

        database.del(bytes);
    }

    @Override
    public void putBatch(Map<byte[], byte[]> inputMap) {
        check(inputMap.keySet());

        check();

        Pipeline p = database.pipelined();

        for (Map.Entry<byte[], byte[]> e : inputMap.entrySet()) {
            byte[] key = e.getKey();
            byte[] value = e.getValue();

            if (value == null) {
                p.del(key);
            } else {
                p.set(key, value);
            }
        }

        List<Object> results = p.syncAndReturnAll();
    }

    @Override
    public void deleteBatch(Collection<byte[]> keys) {
        check(keys);

        check();

        Pipeline p = database.pipelined();

        for (byte[] k : keys) {
            p.del(k);
        }

        List<Object> results = p.syncAndReturnAll();
    }
}
