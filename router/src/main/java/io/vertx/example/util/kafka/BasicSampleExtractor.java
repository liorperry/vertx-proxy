package io.vertx.example.util.kafka;

import io.vertx.core.json.JsonObject;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Arrays;
import java.util.Optional;

import static io.vertx.example.util.kafka.SampleData.*;

public class BasicSampleExtractor implements SampleExtractor{

    @Override
    public Optional<SampleData> extractSample(JsonObject result) {
            if (!result.isEmpty()) {
                double[] values = new double[0];
                String publisher = result.getString(PUBLISHER);
                String time = result.getString(TIME);
                String samples = result.getString(READINGS);
                if (samples != null && samples.length()>0) {
                    values = extractReadings(samples);
                }
                if (publisher != null && time != null && values.length > 0) {

                    DescriptiveStatistics stats = new DescriptiveStatistics(values);
                    double quantile = stats.getPercentile(50);
                    //persist sample data
                    return Optional.of(new SampleData(publisher,time,quantile));
                }
            }
        return Optional.empty();
    }

    public double[] extractReadings(String samples) {
        return Arrays.asList(samples.replace("{", "").replace("}", "").split("\\,")).stream().mapToDouble(Double::valueOf).toArray();
    }
}
