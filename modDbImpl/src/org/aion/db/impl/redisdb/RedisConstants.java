package org.aion.db.impl.redisdb;

import org.aion.db.impl.DBConstants;
import redis.clients.jedis.JedisPool;

public class RedisConstants {
    public static JedisPool getDb(String dbName) {
        switch(dbName) {
            /*case DBConstants.Names.BLOCK:
                return new JedisPool("168.62.170.124", 7001);
            case DBConstants.Names.INDEX:
                return new JedisPool("168.62.170.124", 7002);
            case DBConstants.Names.DETAILS:
                return new JedisPool("168.62.170.124", 7003);
            case DBConstants.Names.STORAGE:
                return new JedisPool("168.62.170.124", 7004);
            case DBConstants.Names.STATE:
                return new JedisPool("168.62.170.124", 7000);
            case DBConstants.Names.TRANSACTION:
                return new JedisPool("168.62.170.124", 7005);
            case DBConstants.Names.TX_CACHE:
                return new JedisPool("168.62.170.124", 7006);
            case DBConstants.Names.TX_POOL:
                return new JedisPool("168.62.170.124", 7007);*/
            case DBConstants.Names.BLOCK:
                return new JedisPool("127.0.0.1", 7001);
            case DBConstants.Names.INDEX:
                return new JedisPool("127.0.0.1", 7002);
            case DBConstants.Names.DETAILS:
                return new JedisPool("127.0.0.1", 7003);
            case DBConstants.Names.STORAGE:
                return new JedisPool("127.0.0.1", 7004);
            case DBConstants.Names.STATE:
                return new JedisPool("127.0.0.1", 7000);
            case DBConstants.Names.TRANSACTION:
                return new JedisPool("127.0.0.1", 7005);
            case DBConstants.Names.TX_CACHE:
                return new JedisPool("127.0.0.1", 7006);
            case DBConstants.Names.TX_POOL:
                return new JedisPool("127.0.0.1", 7007);
            default:
                return null;
        }
    }
}