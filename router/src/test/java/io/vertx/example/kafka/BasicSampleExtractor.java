package io.vertx.example.kafka;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import static io.vertx.example.kafka.SampleData.*;

public class BasicSampleExtractor implements SampleExtractor{

    @Override
    public Optional<SampleData> extractSample(JsonObject result) {
            if (!result.isEmpty()) {
                double[] values = new double[0];
                String publisher = result.getString(PUBLISHER);
                String time = result.getString(TIME);
                JsonArray readings = result.getJsonArray(READINGS);
                if (readings != null && !readings.isEmpty()) {
                    DoubleStream intStream = readings.getList().stream().flatMapToDouble((Function) o -> (Double) o);
                    values = intStream.toArray();
                }
                if (publisher != null && time != null && values.length > 0) {
                    double median = new Median().evaluate(values);
                    //persist sample data
                    return Optional.of(new SampleData(publisher,time,median));
                }
            }
        return Optional.empty();
    }
}
