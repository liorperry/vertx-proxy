package io.vertx.example.web.proxy.events;

import redis.clients.jedis.JedisPool;

import java.util.Optional;

public class RedisEventBus implements EventBus{
    private RedisPublisher publisher;
    private RedisSubscriber subscriber;

    public RedisEventBus(JedisPool pool) {
        // Create the redis client
        publisher = new RedisPublisher(pool);
        subscriber = new RedisSubscriber(pool);
    }

    @Override
    public Object publish(String key, String value) {
        return publisher.publish(key, value);
    }

    @Override
    public Optional subscribe(String key) {
        return subscriber.subscribe(key);
    }


}
