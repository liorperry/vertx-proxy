package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;

class RedisPubSubSubscriber implements Subscriber{
    private JedisPool pool;
    private MessageProcessor[] processors;

    public RedisPubSubSubscriber(JedisPool pool, MessageProcessor ... processors) {
        this.pool = pool;
        this.processors = processors;
    }

    @Override
    public Optional subscribe(String key) {
        Jedis jedis = pool.getResource();
        try {
            jedis.subscribe(new RedisPubSubAdaptor(processors), key);
            return Optional.of(true);
        } finally {
            jedis.close();
        }
    }

    @Override
    public Optional subscribe(String key, MessageProcessor... messageProcessor) {
        Jedis jedis = pool.getResource();
        try {
            jedis.subscribe(new RedisPubSubAdaptor(messageProcessor), key);
            return Optional.of(true);
        } finally {
            jedis.close();
        }
    }
}
