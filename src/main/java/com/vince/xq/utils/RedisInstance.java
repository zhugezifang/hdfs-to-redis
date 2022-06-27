package com.vince.xq.utils;

import redis.clients.jedis.Jedis;

public class RedisInstance {
    private static volatile Jedis jedis = null;

    private RedisInstance() {

    }

    public static Jedis getInstance(String ip, int port, String pwd) {
        if (jedis == null) {
            synchronized (RedisInstance.class) {
                if (jedis == null) {
                    jedis = new Jedis(ip,  port);
                    jedis.auth(pwd);
                    return jedis;
                }
            }
        }
        return jedis;
    }
}
