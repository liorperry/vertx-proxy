package io.vertx.example.web.proxy.events;

import redis.clients.jedis.Jedis;

import java.util.Optional;

public class RedisEventBus implements EventBus{
    private RedisPublisher publisher;
    private RedisSubscriber subscriber;
    private Jedis jedis;

    public RedisEventBus() {
        // Create the redis client
        jedis = new Jedis("localhost");
        publisher = new RedisPublisher(jedis);
        subscriber = new RedisSubscriber(jedis);
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
