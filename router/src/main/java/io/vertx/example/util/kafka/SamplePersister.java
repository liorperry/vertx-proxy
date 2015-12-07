package io.vertx.example.util.kafka;

import java.util.List;

public interface SamplePersister {
    void persist(List<SampleData> data);
    void persist(SampleData data);
    List<SampleData> fetch(String publisherId, int latestNSamples);
    List<SampleData> fetchAll(String publisherId);
}
