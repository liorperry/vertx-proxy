package io.vertx.example.web.proxy.events;

import java.util.Optional;

public interface Subscriber {
    public Optional subscribe(String key);
}
