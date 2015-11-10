package io.vertx.example.web.proxy.locator;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

import java.util.*;
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

    public void addService(String name, Set<String> address) {
        for (String entry : address) {
            addService(name,entry);
        }
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

    public void addServices(Map<String, Set<String>> keys) {
        for (Map.Entry<String, Set<String>> entry : keys.entrySet()) {
            addService(entry.getKey(), entry.getValue());
        }
    }

    public void addServices(String service, Set<String> keys) {
        for (String key : keys) {
            addService(service, key);
        }
    }

    public Collection<String> getAll(String serviceName) {
        if(!pools.containsKey(serviceName))
            return Collections.emptySet();

        return Collections.unmodifiableCollection(pools.get(serviceName).getAll());
    }

    public void updateService(String serviceName, Set<String> keys) {
        pools.remove(serviceName);
        addService(serviceName,keys);
    }

    public Optional<String> get(String serviceName, HashSet<String> excludeList) {
        if (!pools.containsKey(serviceName)) {
            return Optional.empty();
        }
        return pools.get(serviceName).getNext(excludeList);
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


        public Collection<String> getAll() {
            return pool.values();
        }

        /**
         * go over the entire list of service providers to match address not in the exclusion list
         * @param excludeList
         * @return
         */
        public Optional<String> getNext(HashSet<String> excludeList) {
            int size = pool.size();
            for (int i = 0; i < size; i++) {
                String next = getNext();
                if(!excludeList.contains(next)) {
                    return Optional.of(next);
                }
            }
            //no match was found
            return Optional.empty();
        }
    }
}
