package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;

import java.util.Optional;

class RedisSubscriber implements Subscriber{
    private Jedis jedis;

    public RedisSubscriber(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public Optional subscribe(String key) {
        return Optional.ofNullable(jedis.get(key));
    }
}
