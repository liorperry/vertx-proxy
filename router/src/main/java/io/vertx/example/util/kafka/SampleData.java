package io.vertx.example.util.kafka;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class SampleData {
    public static final String PUBLISHER = "publisher";
    public static final String TIME = "time";
    public static final String READINGS = "readings";
    public static final String MEDIAN = "median";


    private final String publishId;
    private final String time;
    private final double median;

    public SampleData(String publishId, String time, double median) {
        this.publishId = publishId;
        this.time = time;
        this.median = median;
    }

    public String getPublishId() {
        return publishId;
    }

    public String getTime() {
        return time;
    }

    public double getMedian() {
        return median;
    }

    public JsonObject toJson() {
        Map<String,Object> map = new HashMap<>();
        map.put(PUBLISHER,publishId);
        map.put(TIME,time);
        map.put(MEDIAN,median);
        return new JsonObject(map);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleData that = (SampleData) o;

        if (Double.compare(that.median, median) != 0) return false;
        if (!publishId.equals(that.publishId)) return false;
        return time.equals(that.time);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = publishId.hashCode();
        result = 31 * result + time.hashCode();
        temp = Double.doubleToLongBits(median);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
