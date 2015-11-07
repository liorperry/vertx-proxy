package io.vertx.example.web.proxy.locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);
    private final ConcurrentMap<String, ServiceDescriptor> services = new ConcurrentHashMap();

    public void register(ServiceDescriptor descriptor) {
        this.services.putIfAbsent(descriptor.getServiceName(), descriptor);
    }

    public void unregister(String name) {
        this.services.remove(name);
    }

    public SortedSet<String> getNames() {
        return Collections.unmodifiableSortedSet(new TreeSet(this.services.keySet()));
    }

    public Collection<ServiceDescriptor> getServices() {
        return Collections.unmodifiableCollection(services.values());
    }
}
