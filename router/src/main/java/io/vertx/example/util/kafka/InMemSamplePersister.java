package io.vertx.example.util.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lior on 06/12/2015.
 */
public class InMemSamplePersister implements SamplePersister {
    private Map<String,List<SampleData>> samples = new ConcurrentHashMap<>();

    @Override
    public void persist(List<SampleData> data) {
        data.stream().forEach(this::persist);
    }

    @Override
    public void persist(SampleData data) {
        if(!samples.containsKey(data.getPublishId())) {
            samples.put(data.getPublishId(),new ArrayList<>());
        }
        samples.get(data.getPublishId()).add(data);
    }

    @Override
    public List<SampleData> fetch(String publisherId, int latestNSamples) {
        if(!samples.containsKey(publisherId))
            return Collections.emptyList();
        int size = samples.get(publisherId).size();
        if(size - latestNSamples > 0) {
            return Collections.unmodifiableList(samples.get(publisherId).subList(size - latestNSamples, size));
        }
        return Collections.emptyList();
    }

    @Override
    public List<SampleData> fetchAll(String publisherId) {
        if(!samples.containsKey(publisherId))
            return Collections.emptyList();
        return Collections.unmodifiableList(samples.get(publisherId));
    }
}
