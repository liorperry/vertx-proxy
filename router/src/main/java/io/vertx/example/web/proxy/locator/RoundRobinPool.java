package io.vertx.example.web.proxy.locator;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap.Builder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class RoundRobinPool {
    private Map<ServiceVersion, ServiceIndexMapTuple> pools;

    public RoundRobinPool() {
        this.pools = new ConcurrentHashMap<>();
    }

    public void addService(ServiceDescriptor serviceDescriptor) {
        if (!pools.containsKey(serviceDescriptor.getServiceVersion())) {
            pools.put(serviceDescriptor.getServiceVersion(), new ServiceIndexMapTuple());
        }
        pools.get(serviceDescriptor.getServiceVersion()).putDescriptor(serviceDescriptor);
    }

    public void addServices(Set<ServiceDescriptor> serviceDescriptor) {
        for (ServiceDescriptor descriptor : serviceDescriptor) {
            addService(descriptor);
        }
    }

    public void removeService(ServiceDescriptor descriptor) {
        if (!pools.containsKey(descriptor.getServiceVersion())) {
            return;
        }
        pools.get(descriptor.getServiceVersion()).removeDescriptor(descriptor);
    }

    public int size() {
        return pools.size();
    }

    public int size(ServiceVersion service) {
        if(pools.containsKey(service)) {
            return pools.get(service).size();
        }
        return 0;
    }

    public Optional<ServiceDescriptor> get(ServiceVersion service) {
        if (!pools.containsKey(service)) {
            return Optional.empty();
        }
        return Optional.of(pools.get(service).getNext());
    }



    public Collection<ServiceDescriptor> getAll(ServiceVersion service) {
        if(!pools.containsKey(service))
            return Collections.emptySet();

        return Collections.unmodifiableCollection(pools.get(service).getAll());
    }

    public void updateService(Set<ServiceDescriptor> descriptors) {
        for (ServiceDescriptor descriptor : descriptors) {
            removeService(descriptor);
            addService(descriptor);
        }
    }

    public Optional<ServiceDescriptor> get(ServiceVersion serviceVersion, Set<ServiceDescriptor> excludeList) {
        if (!pools.containsKey(serviceVersion)) {
            return Optional.empty();
        }
        return pools.get(serviceVersion).getNext(excludeList);
    }

    private static class ServiceIndexMapTuple {
        int index;
        ConcurrentSkipListSet<ServiceDescriptor> pool;

        public ServiceIndexMapTuple() {
            pool = new ConcurrentSkipListSet<>();
        }

        public int size() {
            return pool.size();
        }

        void putDescriptor(ServiceDescriptor descriptor) {
            pool.add(descriptor);
        }

        void removeDescriptor(ServiceDescriptor descriptor) {
            pool.remove(descriptor);
        }

        ServiceDescriptor getNext() {
            index = (index + 1) % pool.size();
            return pool.toArray(new ServiceDescriptor[pool.size()])[index];
        }


        public Collection<ServiceDescriptor> getAll() {
            return Collections.unmodifiableCollection(pool);
        }

        /**
         * go over the entire list of service providers to match address not in the exclusion list
         * @param excludeList
         * @return
         */
        public Optional<ServiceDescriptor> getNext(Set<ServiceDescriptor> excludeList) {
            int size = pool.size();
            for (int i = 0; i < size; i++) {
                ServiceDescriptor next = getNext();
                if(!excludeList.contains(next)) {
                    return Optional.of(next);
                }
            }
            //no match was found
            return Optional.empty();
        }
    }
}
