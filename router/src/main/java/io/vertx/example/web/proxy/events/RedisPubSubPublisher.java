package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

class RedisPubSubPublisher implements Publisher{
    private JedisPool pool;

    public RedisPubSubPublisher(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public Object publish(String key, String value) {
        Jedis jedis = pool.getResource();
        try {
            return jedis.publish(key,value);
        } finally {
            jedis.close();
        }
    }
}
