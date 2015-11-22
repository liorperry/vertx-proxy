package io.vertx.example.web.proxy.events;

import java.util.Optional;

public interface Subscriber {
    Subscriber EMPTY = new Subscriber() {
        @Override
        public Optional subscribe(String key) {
            return Optional.empty();
        }

        @Override
        public Optional subscribe(String key, MessageProcessor... messageProcessor) {
            return Optional.empty();
        }
    };

    Optional subscribe(String key);

    Optional subscribe(String key,MessageProcessor ... messageProcessor);
}
