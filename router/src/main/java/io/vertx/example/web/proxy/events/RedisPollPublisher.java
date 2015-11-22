package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

class RedisPollPublisher implements Publisher{
    private JedisPool pool;

    public RedisPollPublisher(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public Object publish(String key, String value) {
        Jedis jedis = pool.getResource();
        try {
            return jedis.set(key,value);
        } finally {
            jedis.close();
        }
    }
}
