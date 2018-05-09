package org.aion.db.impl.redisdb;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.db.impl.AbstractDB;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class RedisDbWrapper extends AbstractDB {

    private JedisPool pool;
    private Pipeline batch;

    //TODO @Robert extract these to the config maybe???

    public static final String BLOCK = "block";
    public static final String INDEX = "index";

    public static final String DETAILS = "details";
    public static final String STORAGE = "storage";

    public static final String STATE = "state";
    public static final String TRANSACTION = "transaction";

    public static final String TX_CACHE = "pendingtxCache";
    public static final String TX_POOL = "pendingtxPool";

    public RedisDbWrapper(String name, String path, boolean enableDbCache, boolean enableDbCompression) {
        super(name, path, enableDbCache, enableDbCompression);
        // each db should have it's own separate redis Server and we should find a way to pass these details along

        switch(name) {
            case BLOCK:
                pool = new JedisPool("localhost", 7000);
                break;
            case INDEX:
                pool = new JedisPool("localhost", 7001);
                break;
            case DETAILS:
                pool = new JedisPool("localhost", 7002);
                break;
            case STORAGE:
                pool = new JedisPool("localhost", 7003);
                break;
            case STATE:
                pool = new JedisPool("localhost", 7004);
                break;
            case TRANSACTION:
                pool = new JedisPool("localhost", 7005);
                break;
            case TX_CACHE:
                pool = new JedisPool("localhost", 7006);
                break;
            case TX_POOL:
                pool = new JedisPool("localhost", 7007);
                break;
            default:
                pool = new JedisPool("localhost", 7008);
                break;
        }
    }

    @Override
    public boolean commitCache(Map<ByteArrayWrapper, byte[]> cache) {
        try(Jedis db = pool.getResource()){
            Pipeline tmpBatch = db.pipelined();

            for (Map.Entry<ByteArrayWrapper, byte[]> e : cache.entrySet()) {
                if (e.getValue() == null) {
                    tmpBatch.del(new String(e.getKey().getData(), "ISO-8859-1"));
                } else {
                    tmpBatch.set(new String(e.getKey().getData(), "ISO-8859-1"), new String(e.getValue() , "ISO-8859-1"));
                }
            }

            tmpBatch.sync();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected byte[] getInternal(byte[] key) {
        try(Jedis db = pool.getResource()) {
            String value = db.get(new String(key, "ISO-8859-1"));
            if(value != null){
                return value.getBytes("ISO-8859-1");
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
        try(Jedis db = pool.getResource()) {
            return db.isConnected();
        }
    }

    @Override
    public boolean isCreatedOnDisk() {
        return true;
    }

    @Override
    public long approximateSize() {
        check();

        try(Jedis db = pool.getResource()) {
            return db.keys("*").size();
        }
    }

    @Override
    public boolean isEmpty() {
        check();

        try(Jedis db = pool.getResource()) {
            return db.keys("*").size() <= 0;
        }
    }

    @Override
    public Set<byte[]> keys() {
        Set<byte[]> set = new HashSet<>();

        check();

        try(Jedis db = pool.getResource()) {
            Set<String> keysAsString = db.keys("*");
            for(String key : keysAsString){
                set.add(key.getBytes("ISO-8859-1"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return set;
    }

    @Override
    public void put(byte[] key, byte[] value) {
        check(key);

        check();

        try(Jedis db = pool.getResource()) {
            db.set(new String(key, "ISO-8859-1"), new String(value, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(byte[] key) {
        check(key);

        check();

        try(Jedis db = pool.getResource()) {
            db.del(new String(key, "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putBatch(Map<byte[], byte[]> inputMap) {
        check(inputMap.keySet());

        check();

        try(Jedis db = pool.getResource()){
            Pipeline tmpBatch = db.pipelined();

            for(Map.Entry<byte[], byte[]> e : inputMap.entrySet()) {

                byte[] key = e.getKey();
                byte[] value = e.getValue();

                if (value == null) {
                    tmpBatch.del(new String(key, "ISO-8859-1"));
                } else {
                    tmpBatch.set(new String(key, "ISO-8859-1"), new String(value, "ISO-8859-1"));
                }
            }

            tmpBatch.sync();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void putToBatch(byte[] key, byte[] value) {
        check(key);

        check();

        try(Jedis db = pool.getResource()){
            if(batch == null) {
                batch = db.pipelined();
            }

            if (value == null) {
                batch.del(new String(key, "ISO-8859-1"));
            } else {
                batch.set(new String(key, "ISO-8859-1"), new String(value, "ISO-8859-1"));
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commitBatch() {
        try(Jedis db = pool.getResource()) {
            if(batch != null) {
                batch.sync();
            }
        }
    }

    @Override
    public void deleteBatch(Collection<byte[]> keys) {
        check(keys);

        check();

        try(Jedis db = pool.getResource()){
            Pipeline tmpBatch = db.pipelined();

            for(byte[] key: keys) {
                tmpBatch.del(new String(key, "ISO-8859-1"));
            }

            tmpBatch.sync();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        pool.close();
    }
}
