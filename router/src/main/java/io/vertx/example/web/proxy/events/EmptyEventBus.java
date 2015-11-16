package io.vertx.example.web.proxy.events;

import java.util.Optional;

public class EmptyEventBus implements EventBus{
    public static EventBus EMPTY = new EmptyEventBus();

    private EmptyEventBus() {}

    @Override
    public Object publish(String key, String value) {
        return null;
    }

    @Override
    public Optional subscribe(String key) {
        return Optional.empty();
    }

}
