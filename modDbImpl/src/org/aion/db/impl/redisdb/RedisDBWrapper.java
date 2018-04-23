package org.aion.db.impl.redisdb;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.db.impl.AbstractDB;
import redis.clients.jedis.*;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RedisDBWrapper extends AbstractDB {
    private JedisPool pool;
    private Pipeline batch;

    public static final String BLOCK = "block";
    public static final String INDEX = "index";

    public static final String DETAILS = "details";
    public static final String STORAGE = "storage";

    public static final String STATE = "state";
    public static final String TRANSACTION = "transaction";

    public static final String TX_CACHE = "pendingtxCache";
    public static final String TX_POOL = "pendingtxPool";

    protected RedisDBWrapper(String name) {
        super(name);
    }

    public RedisDBWrapper(String name, String path, boolean enableDbCache, boolean enableDbCompression) {
        super(name, path, enableDbCache, enableDbCompression);
        switch(name){
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
                pool = new JedisPool("localhost");
        }

    }

    @Override
    public boolean commitCache(Map<ByteArrayWrapper, byte[]> cache) {

        check();
        try (Jedis db = pool.getResource()) {

            for (Map.Entry<ByteArrayWrapper, byte[]> e : cache.entrySet()) {
                if (e.getValue() == null) {
                    db.del(formatKey(e.getKey().getData()));
                } else {
                    db.set(formatKey(e.getKey().getData()), new String(e.getValue(), "ISO-8859-1"));
                }
            }

        } catch (Exception e) {
            LOG.error("Error: " + e.toString());
        }

        return false;
    }

    @Override
    protected byte[] getInternal(byte[] k) {
        check();

        try (Jedis db = pool.getResource()) {
            return db.get(formatKey(k)).getBytes("ISO-8859-1");
        } catch (Exception e ) {
            return null;
        }
    }

    @Override
    public boolean open() {
        try (Jedis db = pool.getResource()) {
            return db.isConnected();
        } catch (Exception e ) {
            return false;
        }
    }

    @Override
    public boolean isOpen() {
        try (Jedis db = pool.getResource()) {
            return db.isConnected();
        } catch (Exception e ) {
            return false;
        }
    }

    //TODO @Robert is this relevant for system behaviour
    @Override
    public boolean isCreatedOnDisk() {
        return true;
    }

    @Override
    public long approximateSize() {
        check();

        try (Jedis db = pool.getResource()) {
            return db.dbSize();
        } catch (Exception e ) {
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        check();

        try (Jedis db = pool.getResource()) {
            return !(db.dbSize() > 0);
        } catch (Exception e ) {
            return false;
        }
    }

    @Override
    public Set<byte[]> keys() {
        check();

        try (Jedis db = pool.getResource()) {
            //Set<String> keys = db.keys(name + "-*");
            Set<String> keys = db.keys("*");
            Set<byte[]> keysAsByes = new LinkedHashSet<>();
            for (String k : keys) {
                //keysAsByes.add(k.substring(name.length()+1).getBytes("ISO-8859-1"));
                keysAsByes.add(k.getBytes("ISO-8859-1"));
            }
            return keysAsByes;
        } catch (Exception e ) {
            return null;
        }
    }

    @Override
    public void put(byte[] bytes, byte[] bytes2) {
        check(bytes);

        check();

        try (Jedis db = pool.getResource()) {
            db.set(formatKey(bytes), new String(bytes2, "ISO-8859-1"));
        } catch (Exception e) {
            LOG.error("Error when setting a key to redis pool");
        }
    }

    @Override
    public void delete(byte[] bytes) {
        check(bytes);
        check();

        try (Jedis db = pool.getResource()) {
            db.del(formatKey(bytes));
        } catch (Exception e) {
            LOG.error("Error when removing a key to redis pool");
        }
    }

    @Override
    public void putBatch(Map<byte[], byte[]> inputMap) {
        check(inputMap.keySet());

        check();

        try (Jedis db = pool.getResource()) {

            Pipeline pipe = db.pipelined();

            for (byte[] val : inputMap.keySet()) {
                pipe.set(formatKey(val), new String(inputMap.get(val), "ISO-8859-1"));
            }

            pipe.sync();

        }  catch (Exception e) {
            LOG.error("Error when putting a batch to redis pool");
        }
    }

    @Override
    public void putToBatch(byte[] key, byte[] value) {
        check(key);

        check();

        try (Jedis db = pool.getResource()) {

            if(batch == null) {
                batch = db.pipelined();
            }

            batch.set(formatKey(key), new String(value, "ISO-8859-1"));

            //TODO @Robert this should not be here...
            batch.sync();

        } catch (Exception e) {
            LOG.error("Error when putting a batch to redis pool");
        }
    }

    @Override
    public void commitBatch() {
        check();

        try (Jedis db = pool.getResource()) {

            if(batch != null) {
                batch.sync();
            }

            batch = null;

        } catch (Exception e) {
            LOG.error("Error when putting a batch to redis pool");
        }
    }

    @Override
    public void deleteBatch(Collection<byte[]> keys) {
        check(keys);

        check();

        try (Jedis db = pool.getResource()) {

            for (byte[] key : keys) {
                db.del(formatKey(key));
            }

        } catch (Exception e) {
            LOG.error("Error when deleting a batch to redis pool");
        }
    }

    @Override
    public void close() {
        try (Jedis db = pool.getResource()) {

            if (db != null) {
                db.close();
            } else {
                LOG.error("We cannot close a null DB");
            }
        }  catch (Exception e) {
            LOG.error("Error when closing a redis pool");
        }
    }

    private String formatKey(byte[] key){
        try {
            String keyAsString = new String(key, "ISO-8859-1");
            //String keyWithPrefix = name + "-" + keyAsString;
            String keyWithPrefix = keyAsString;
            return keyWithPrefix;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
