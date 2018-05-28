package org.aion.db.impl.redisdb;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class RedisClusterConstants {
    private static volatile List<JedisPool> cluster = null;
    static final int DATABASE_COUNT = 5;

    private RedisClusterConstants() {}

    private static List<JedisPool> init() {
        List<JedisPool> cluster = new ArrayList<>();

        /*cluster.add(new JedisPool(new JedisPoolConfig(),"168.62.170.124",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"40.114.66.31",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"40.114.75.97",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"40.114.78.219",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"40.114.125.118", 7000));*/

        cluster.add(new JedisPool(new JedisPoolConfig(),"127.0.0.1",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"10.0.69.7",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"10.0.69.8",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"10.0.69.9",  7000));
        cluster.add(new JedisPool(new JedisPoolConfig(),"10.0.69.10", 7000));

        return cluster;
    }

    public static Jedis getDb(int databaseId) {
        if (cluster == null) {
            synchronized (RedisClusterConstants.class) {
                if (cluster == null) {
                    cluster = init();
                }
            }
        }

        return cluster.get(databaseId).getResource();
    }

    static void closeDbs() {
        for (JedisPool jp : cluster) {
            jp.close();
        }
    }
}
