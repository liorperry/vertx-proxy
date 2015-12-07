package io.vertx.example.kafka.integration;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.SampleData;
import io.vertx.example.util.kafka.SampleExtractor;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public abstract class KafkaTestUtils {

    private static Random random = new Random();
    private static SampleExtractor extractor = new BasicSampleExtractor();

    public static List<SampleData> create(String publisherId,int size,int readingSampleSize) {
        List<SampleData> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(extractor.extractSample(new JsonObject(buildMessage(publisherId,readingSampleSize))).get());
        }
        return result;
    }

    private static Map<String, Object> buildMessage(String publisherId, int readingSampleSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", publisherId);
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", getReadings(readingSampleSize));
        return map;
    }

    private static String getReadings(int readingSampleSize) {
        DoubleStream doubles = random.doubles(readingSampleSize);
        Stream<String> values = doubles.mapToObj(Double::toString);
        StringBuilder builder = new StringBuilder("{");
        values.forEach(s -> {builder.append(s +",");});
        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("}");
        return builder.toString();
    }
}
