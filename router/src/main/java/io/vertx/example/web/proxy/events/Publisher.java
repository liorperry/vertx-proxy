package io.vertx.example.web.proxy.events;

import java.util.Optional;

public interface Publisher {
    Publisher EMPTY = (key, value) -> Optional.empty();

    Object publish(String key, String value);
}
