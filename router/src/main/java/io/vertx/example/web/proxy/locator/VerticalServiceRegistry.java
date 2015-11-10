package io.vertx.example.web.proxy.locator;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class VerticalServiceRegistry implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerticalServiceRegistry.class);
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

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        this.services.clear();
    }
}
