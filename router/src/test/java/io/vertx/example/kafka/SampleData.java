package io.vertx.example.kafka;

public final class SampleData {
    public static final String PUBLISHER = "publisher";
    public static final String TIME = "time";
    public static final String READINGS = "readings";


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
