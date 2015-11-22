package io.vertx.example.web.proxy.events;

public interface MessageProcessor {
    void onMessage(String channel, String message);
    void onPMessage(String pattern, String channel, String message);
}
