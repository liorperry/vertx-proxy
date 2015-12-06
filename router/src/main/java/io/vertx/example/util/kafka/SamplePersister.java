package io.vertx.example.util.kafka;

import java.util.List;

public interface SamplePersister {
    void persist(SampleData data);
    List<SampleData> fetch(int latestNSamples);
    List<SampleData> fetchAll();
}
