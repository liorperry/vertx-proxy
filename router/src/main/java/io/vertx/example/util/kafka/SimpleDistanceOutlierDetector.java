package io.vertx.example.util.kafka;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;
import java.util.Optional;
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
    public List<SampleData> getOutlier(String publisherId, int sampleSize, Optional<Double> outlierFactor) {
        List<SampleData> fetch = persister.fetch(publisherId, sampleSize);
        double[] values = fetch.stream().mapToDouble(SampleData::getMedian).toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(values);
        double deviation = stats.getStandardDeviation();
        double mean = stats.getMean();
        List<SampleData> outliers = fetch.stream().filter(sampleData -> isOutlier(deviation, mean, sampleData,outlierFactor.orElse(2d))).collect(Collectors.toList());
        return outliers;
    }

    public boolean isOutlier(double deviation, double mean, SampleData sampleData, double outlierFactor) {
        return distance(sampleData, mean) > outlierFactor*deviation;
    }

    public double distance(SampleData sampleData, double mean) {
        return Math.abs(sampleData.getMedian() - mean);
    }
}
