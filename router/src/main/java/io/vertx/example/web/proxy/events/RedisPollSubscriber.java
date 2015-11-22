package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.Optional;

class RedisPollSubscriber implements Subscriber{
    private JedisPool pool;

    public RedisPollSubscriber(JedisPool pool) {
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

    @Override
    public Optional subscribe(String key, MessageProcessor... messageProcessor) {
        Optional<String> result ;
        Jedis jedis = pool.getResource();
        try {
            result = Optional.ofNullable(jedis.get(key));
        } finally {
            jedis.close();
        }
        if(result!=null && result.isPresent()) {
            Arrays.asList(messageProcessor).forEach(p -> p.onMessage(key, result.get()));
        }
        return result;
    }
}
