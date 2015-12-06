package io.vertx.example.kafka.test;

import io.vertx.core.json.JsonObject;
import io.vertx.example.util.kafka.*;
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
public class SampleDataPersisterTest {
    private SamplePersister persister;
    private SampleExtractor extractor;

    @Test
    public void testDataPersist() throws Exception {
        extractor = new BasicSampleExtractor();
        persister = new InMemSamplePersister();
        for (int i = 0; i < 10 ; i++) {
            Optional<SampleData> sampleData = extractor.extractSample(new JsonObject(buildMessage()));
            persister.persist(sampleData.get());
        }
        assertEquals(persister.fetchAll().size(),10);
        assertEquals(persister.fetch(5).size(),5);
        assertEquals(persister.fetch(0).size(),0);
    }


    public Map<String, Object> buildMessage() {
        Map<String, Object> map = new HashMap<>();
        map.put("publisher", "norbert");
        map.put("time", GregorianCalendar.getInstance().getTime().toLocaleString());
        map.put("readings", "{1,13,192,7,8,99,1014,4}");
        return map;
    }
}
