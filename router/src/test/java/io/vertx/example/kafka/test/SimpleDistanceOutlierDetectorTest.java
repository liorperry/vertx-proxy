package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.*;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class SimpleDistanceOutlierDetectorTest {


    private SamplePersister persister;
    private SampleExtractor extractor;

    @Before
    public void setUp() throws Exception {
        extractor = new BasicSampleExtractor();
        persister = new InMemSamplePersister();
        for (int i = 0; i < 10  ; i++) {
            persister.persist(extractor.extractSample(new JsonObject(buildMessage1(i))).get());
            persister.persist(extractor.extractSample(new JsonObject(buildMessage2(i))).get());
        }
    }

    @Test
    public void testOutlierDistance() throws Exception {
        SimpleDistanceOutlierDetector detector = new SimpleDistanceOutlierDetector(persister);
        SampleData sampleData = extractor.extractSample(new JsonObject(buildMessage2(0))).get();
        double distance = detector.distance(sampleData, 5);
        assertEquals(distance, 0.0);

        distance = detector.distance(sampleData, 3);
        assertEquals(distance, 2.0);

        distance = detector.distance(sampleData, 7);
        assertEquals(distance, 2.0);
    }

    @Test
    public void testOutlierSamples() throws Exception {
        SimpleDistanceOutlierDetector detector = new SimpleDistanceOutlierDetector(persister);
        SampleData sampleData = extractor.extractSample(new JsonObject(buildMessage2(0))).get();

        double[] readings = extractor.extractReadings(getReadings2(0));
        DescriptiveStatistics stats = new DescriptiveStatistics(readings);
        double deviation = stats.getStandardDeviation();
        double mean = stats.getMean();
        boolean outlier = detector.isOutlier(deviation, mean, sampleData, 2);
        assertFalse(outlier);

        outlier = detector.isOutlier(deviation, mean, sampleData, 0.01);
        assertTrue(outlier);
    }

    @Test
    public void testOutlierDetector() throws Exception {
/*
        SimpleDistanceOutlierDetector detector = new SimpleDistanceOutlierDetector(persister);
        detector.distance()
*/
    }

    public Map<String, Object> buildMessage1(int index) {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "norbert");
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", getReadings1(index));
        return map;
    }

    public Map<String, Object> buildMessage2(int index) {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "ginsburg");
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", getReadings2(index));
        return map;
    }

    public String getReadings1(int index) {
        return "{" + (1 + index) + "," + (13 + index) + "," + (192 + index) + "," + (7 + index) + "," + (8 + index) + "," + (99 + index) + "," + (1014 + index) + "," + (4 + index) + "," + "}";
    }

    public String getReadings2(int index) {
        return "{" + (1 + index) + "," + (2 + index) + "," + (3 + index) + "," + (4 + index) + "," + (5 + index) + "," + (6 + index) + "," + (8 + index) + "," + (8 + index) + "," + (9 + index) + "," + "}";
    }

}
