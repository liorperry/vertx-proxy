package io.vertx.example.util.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lior on 06/12/2015.
 */
public class InMemSamplePersister implements SamplePersister {
    private List<SampleData> samples = new ArrayList<>();

    @Override
    public void persist(SampleData data) {
        samples.add(data);
    }

    @Override
    public List<SampleData> fetch(int latestNSamples) {
        assert (samples.size() - latestNSamples) > 0;
        return Collections.unmodifiableList(samples.subList(samples.size() - latestNSamples, samples.size() ));
    }

    @Override
    public List<SampleData> fetchAll() {
        return Collections.unmodifiableList(samples);
    }
}
