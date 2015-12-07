package io.vertx.example.util.kafka;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleDistanceOutlierDetector implements OutlierDetector {
    private SamplePersister persister;

    public SimpleDistanceOutlierDetector(SamplePersister persister) {
        this.persister = persister;
    }

    /**
     * return all samples (within the sample size window) that differ more > 2 time sdtDev
     */
    @Override
    public Collection<SampleData> getOutlier(String publisherId, int sampleSize) {
        List<SampleData> fetch = persister.fetch(publisherId, sampleSize);
        double[] values = fetch.stream().mapToDouble(SampleData::getMedian).toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        double deviation = stats.getStandardDeviation();
        double mean = stats.getMean();
        List<SampleData> outliers = fetch.stream().filter(sampleData -> isOutlier(deviation, mean, sampleData)).collect(Collectors.toList());
        return outliers;
    }

    public boolean isOutlier(double deviation, double mean, SampleData sampleData) {
        return distance(sampleData, mean) > 2*deviation;
    }

    public double distance(SampleData sampleData, double mean) {
        return Math.abs(sampleData.getMedian() - mean);
    }
}
