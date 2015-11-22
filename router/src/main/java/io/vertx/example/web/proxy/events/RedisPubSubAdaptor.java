package io.vertx.example.web.proxy.events;


import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;

public class RedisPubSubAdaptor extends JedisPubSub {

    private MessageProcessor[] processors;

    public RedisPubSubAdaptor(MessageProcessor[] processors) {
        this.processors = processors;
    }

    @Override
    public void onMessage(String channel, String message) {
        super.onMessage(channel, message);
        Arrays.asList(processors).stream().forEach(messageProcessor -> messageProcessor.onMessage(channel,message) );
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        super.onPMessage(pattern, channel, message);
        Arrays.asList(processors).stream().forEach(messageProcessor -> messageProcessor.onPMessage(pattern,channel, message));
    }
}
