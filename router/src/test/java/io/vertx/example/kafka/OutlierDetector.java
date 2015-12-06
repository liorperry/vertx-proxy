package io.vertx.example.kafka;

public interface OutlierDetector {
    SampleData getOutlier(int sampleSize);
}
