package io.vertx.example.web.proxy.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.*;
import java.util.stream.Collectors;

import static io.vertx.example.web.proxy.filter.FilterUtils.extractProduct;
import static io.vertx.example.web.proxy.filter.FilterUtils.extractService;

public class CouchbaseKeysRepository implements KeysRepository {
    public static final String SERVICES = "services";
    public static final String PRODUCTS = "products";
    public static final String MOBILE_CHANNEL = "channel.mobile";
    public static final String INTERNET_CHANNEL = "channel.internet";

    private CouchbaseCluster cluster;

    public CouchbaseKeysRepository(CouchbaseCluster cluster) {
        // Create a cluster reference
        this.cluster = cluster;
    }

    @Override
    public Map<String, String> getServices() {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(SERVICES)) {
                return Collections.emptyMap();
            }
            //todo this in collectors.toMap
            JsonObject content = bucket.get(SERVICES).content();
            HashMap<String, String> map = new HashMap<>();
            content.getNames().stream().forEach(s -> map.put(s, content.getString(s)));
            return map;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public Map<String, String> getProducts() {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(PRODUCTS)) {
                return Collections.emptyMap();
            }
            //todo this in collectors.toMap
            JsonObject content = bucket.get(PRODUCTS).content();
            HashMap<String, String> map = new HashMap<>();
            content.getNames().stream().forEach(s -> map.put(s, content.getString(s)));
            return map;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public Set<String> getChannelServices(String channelName) {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(channelName)) {
                return Collections.emptySet();
            }
            JsonObject content = bucket.get(channelName).content();
            JsonArray array = content.getArray(SERVICES);
            return array.toList().stream().map(Object::toString).collect(Collectors.<String>toSet());
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public Optional<Boolean> getService(String uri) {
        Optional<String> service = extractService(uri);
        if (!service.isPresent()) {
            return Optional.empty();
        }

        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(SERVICES)) {
                return Optional.empty();
            }
            JsonObject content = bucket.get(SERVICES).content();
            return Optional.of(content.getBoolean(service.get()));
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public Optional<Boolean> getChannelService(String uri, String channelName) {
        Optional<String> service = extractService(uri);
        if (!service.isPresent()) {
            return Optional.empty();
        }
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(channelName)) {
                return Optional.empty();
            }
            JsonObject content = bucket.get(channelName).content();
            JsonArray array = content.getArray(SERVICES);
            boolean present = array.toList().stream().filter(o -> o.toString().equals(service.get())).findFirst().isPresent();
            return Optional.of(present);
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public Optional<Boolean> getProduct(String uri) {
        Optional<String> products = extractProduct(uri);
        if (!products.isPresent()) {
            return Optional.empty();
        }

        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            if (!bucket.exists(PRODUCTS)) {
                return Optional.empty();
            }
            JsonObject content = bucket.get(PRODUCTS).content();
            return Optional.of(content.getBoolean(products.get()));
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public boolean blockService(String serviceName) {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            JsonDocument document = bucket.get(SERVICES);
            document.content().put(serviceName,Boolean.FALSE.toString());
            bucket.upsert(document);
            return true;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public boolean openService(String serviceName) {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            JsonDocument document = bucket.get(SERVICES);
            document.content().put(serviceName,Boolean.TRUE.toString());
            bucket.upsert(document);
            return true;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public boolean blockProduct(String productName) {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            JsonDocument document = bucket.get(PRODUCTS);
            document.content().put(productName,Boolean.FALSE.toString());
            bucket.upsert(document);
            return true;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public boolean openProduct(String productName) {
        // Connect to the bucket and open it
        Bucket bucket = null;
        try {
            bucket = cluster.openBucket("default");
            JsonDocument document = bucket.get(PRODUCTS);
            document.content().put(productName,Boolean.TRUE.toString());
            bucket.upsert(document);
            return true;
        } finally {
            assert bucket != null;
            bucket.close();
        }
    }

    @Override
    public void addService(String serviceName, boolean status, String... channels) {
/*
        addService(serviceName, status);
        Jedis jedis = pool.getResource();
        Arrays.asList(channels).stream().forEach(s -> {
            jedis.sadd(s, serviceName);
        });
        jedis.close();
*/
    }

    public void addService(String serviceName, boolean status) {
/*
        Jedis jedis = pool.getResource();
        jedis.hset(SERVICES, serviceName, Boolean.toString(status));
        jedis.close();
*/
    }

    @Override
    public void addProduct(String productName, boolean status) {
/*
        Jedis jedis = pool.getResource();
        jedis.hset(PRODUCTS, productName, Boolean.toString(status));
        jedis.close();
*/
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        cluster.disconnect();
    }

}
