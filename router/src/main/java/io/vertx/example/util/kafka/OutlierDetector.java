package io.vertx.example.util.kafka;

import java.util.List;
import java.util.Optional;

public interface OutlierDetector {
    /**
     * return all samples (within the sample size window) that differ more > 2 time sdtDev
     */
    List<SampleData> getOutlier(String publisherId, int sampleSize,Optional<Double> outlierFactor);
}
