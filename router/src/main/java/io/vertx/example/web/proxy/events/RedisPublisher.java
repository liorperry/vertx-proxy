package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;

class RedisPublisher implements Publisher{
    private Jedis jedis;

    public RedisPublisher(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public Object publish(String key, String value) {
        return jedis.set(key,value);
    }
}
