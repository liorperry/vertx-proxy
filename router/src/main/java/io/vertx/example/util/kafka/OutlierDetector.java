package io.vertx.example.util.kafka;

public interface OutlierDetector {
    SampleData getOutlier(int sampleSize);
}
