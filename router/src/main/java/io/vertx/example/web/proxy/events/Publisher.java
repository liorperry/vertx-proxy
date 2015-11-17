package io.vertx.example.web.proxy.events;

public interface Publisher {
    Object publish(String key, String value);
}
