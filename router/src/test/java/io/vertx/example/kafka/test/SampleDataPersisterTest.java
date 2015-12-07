package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.BasicSampleExtractor;
import io.vertx.example.util.kafka.InMemSamplePersister;
import io.vertx.example.util.kafka.SampleExtractor;
import io.vertx.example.util.kafka.SamplePersister;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class SampleDataPersisterTest {
    public static final String NORBERT = "norbert";
    public static final String GINSBURG = "ginsburg";

    private SamplePersister persister;
    private SampleExtractor extractor;

    @Test
    public void testDataPersist() throws Exception {
        extractor = new BasicSampleExtractor();
        persister = new InMemSamplePersister();
        for (int i = 0; i < 10 ; i++) {
            persister.persist(extractor.extractSample(new JsonObject(buildMessage1())).get());
            persister.persist(extractor.extractSample(new JsonObject(buildMessage2())).get());
        }
        assertEquals(persister.fetchAll(NORBERT).size(),10);
        assertEquals(persister.fetch(NORBERT, 5).size(),5);
        assertEquals(persister.fetch(NORBERT, 0).size(),0);

        assertEquals(persister.fetchAll(GINSBURG).size(),10);
        assertEquals(persister.fetch(GINSBURG, 7).size(),7);
        assertEquals(persister.fetch(GINSBURG, 0).size(),0);
    }


    public Map<String, Object> buildMessage1() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", NORBERT);
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", "{1,13,192,7,8,99,1014,4}");
        return map;
    }

    public Map<String, Object> buildMessage2() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", GINSBURG);
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", "{12,123,12,17,98,29,114,354}");
        return map;
    }
}
