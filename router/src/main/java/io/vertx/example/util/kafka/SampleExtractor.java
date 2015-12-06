package io.vertx.example.util.kafka;

import io.vertx.core.json.JsonObject;

import java.util.Optional;

public interface SampleExtractor {
    Optional<SampleData> extractSample(JsonObject data);
}
