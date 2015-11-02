package io.vertx.example.web.proxy.events;

public interface Publisher {
    public Object publish(String key,String value);
}
