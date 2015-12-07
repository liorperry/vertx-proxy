package io.vertx.example.util.kafka;

import io.vertx.core.json.JsonObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RedisSamplePersister implements SamplePersister {
    public static final String PUBLISHERS = "publishers";
    private JedisPool pool;
    private SampleExtractor extractor;

    public RedisSamplePersister(JedisPool pool, SampleExtractor extractor) {
        this.pool = pool;
        this.extractor = extractor;
    }

    @Override
    public void persist(List<SampleData> data) {
        data.stream().forEach(this::persist);
    }

    @Override
    public void persist(SampleData data) {
        Jedis resource = pool.getResource();
        try {
            resource.sadd(PUBLISHERS,data.getPublishId());
            resource.lpush(data.getPublishId(), data.toJson().encodePrettily());
        } finally {
            resource.close();
        }
    }

    @Override
    public List<SampleData> fetch(String publisherId, int latestNSamples) {
        Jedis resource = pool.getResource();
        try {
            long size = resource.llen(publisherId);
            if (size == 0) {
                return Collections.emptyList();
            }
            if (size - latestNSamples > 0) {
                List<String> samples = resource.lrange(publisherId, size - latestNSamples, size);
                List<SampleData> collect = samples.stream().map(s -> extractor.extractSample(new JsonObject(s)).get()).collect(Collectors.toList());
                return Collections.unmodifiableList(collect);
            }
            return Collections.emptyList();
        } finally {
            resource.close();
        }

    }

    @Override
    public List<SampleData> fetchAll(String publisherId) {
        Jedis resource = pool.getResource();
        try {
            long size = resource.llen(publisherId);
            if (size == 0) {
                return Collections.emptyList();
            }
            List<String> samples = resource.lrange(publisherId, 0, size);
            List<SampleData> collect = samples.stream().map(s -> extractor.extractSample(new JsonObject(s)).get()).collect(Collectors.toList());
            return Collections.unmodifiableList(collect);
        } finally {
            resource.close();
        }
    }

    @Override
    public Set<String> getPublishers() {
        Jedis resource = pool.getResource();
        try {
            return resource.smembers(PUBLISHERS);
        } finally {
            resource.close();
        }
    }
}
