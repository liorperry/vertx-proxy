package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.SampleData;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static junit.framework.Assert.*;

@RunWith(VertxUnitRunner.class)
public class SampleDataExtractorTest {


    public static final String NOW = GregorianCalendar.getInstance().getTime().toLocaleString();
    public static final String READINGS = "{1,13,192,7,8,99,1014,4}";

    @Test
    public void testDataReadingsExtractor() throws Exception {
        BasicSampleExtractor extractor = new BasicSampleExtractor();
        double[] readings = extractor.extractReadings(READINGS);
        assertEquals(readings.length,8);
    }

    public void testDataExtractor() throws Exception {
        BasicSampleExtractor extractor = new BasicSampleExtractor();
        Optional<SampleData> sampleData = extractor.extractSample(new JsonObject(buildMessage()));
        assertTrue(sampleData.isPresent());
        assertEquals(sampleData.get().getPublishId(), "norbert");
        assertEquals(sampleData.get().getTime(), NOW);
        assertEquals(sampleData.get().getMedian(), 10.5);
    }

    public Map<String, Object> buildMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "norbert");
        map.put("time", NOW);
        map.put("readings", READINGS);
        return map;
    }

}
