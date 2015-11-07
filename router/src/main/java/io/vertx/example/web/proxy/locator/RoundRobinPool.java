package io.vertx.example.web.proxy.locator;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RoundRobinPool {
    private Map<String, ServiceIndexMapTuple> pools;

    public RoundRobinPool() {
        this.pools = new ConcurrentHashMap<>();
    }

    public void addService(String name, String address) {
        if (!pools.containsKey(name)) {
            pools.put(name, new ServiceIndexMapTuple());
        }
        pools.get(name).putAddress(address);
    }

    public void removeService(String name, String address) {
        if (!pools.containsKey(name)) {
            return;
        }
        pools.get(name).removeAddress(address);
    }

    public int size() {
        return pools.size();
    }

    public int size(String service) {
        if(pools.containsKey(service)) {
            return pools.get(service).size();
        }
        return 0;
    }

    public Optional<String> get(String service) {
        if (!pools.containsKey(service)) {
            return Optional.empty();
        }
        return Optional.of(pools.get(service).getNext());
    }

    public void addServices(String service, Set<String> keys) {
        for (String key : keys) {
            addService(service,key);
        }
    }

    private static class ServiceIndexMapTuple {
        int index;
        ConcurrentLinkedHashMap<String, String> pool;

        public ServiceIndexMapTuple() {
            this.pool = new Builder<String, String>()
                    .initialCapacity(16)
                    .maximumWeightedCapacity(16)
                    .build();
        }

        public int size() {
            return pool.size();
        }

        void putAddress(String address) {
            pool.putIfAbsent(address, address);
        }

        void removeAddress(String address) {
            pool.remove(address);
        }

        String getNext() {
            index = (index + 1) % pool.keySet().size();
            return pool.keySet().toArray(new String[pool.keySet().size()])[index];
        }
    }
}
