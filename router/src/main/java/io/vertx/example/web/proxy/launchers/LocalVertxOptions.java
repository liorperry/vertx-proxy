package io.vertx.example.web.proxy.launchers;

import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class LocalVertxOptions extends VertxOptions {
    private final Map<String,Object> map = new HashMap<>();

    public LocalVertxOptions() {}

    public LocalVertxOptions(VertxOptions other) {
        super(other);
    }

    public LocalVertxOptions(JsonObject json) {
        super(json);
    }

    public LocalVertxOptions put(String key,Object value) {
        map.put(key,value);
        return this;
    }
}
