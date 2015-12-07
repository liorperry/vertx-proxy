package io.vertx.example.util.kafka;

import java.util.Collection;

public interface OutlierDetector {
    /**
     * return all samples (within the sample size window) that differ more > 2 time sdtDev
     */
    Collection<SampleData> getOutlier(String publisherId,int sampleSize);
}
