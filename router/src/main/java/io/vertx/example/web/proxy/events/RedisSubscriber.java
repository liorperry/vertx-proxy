package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

class RedisSubscriber implements Subscriber{
    private JedisPool pool;

    public RedisSubscriber(JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public Optional subscribe(String key) {
        Jedis jedis = pool.getResource();
        try {
            return Optional.ofNullable(jedis.get(key));
        } finally {
            jedis.close();
        }
    }
}
