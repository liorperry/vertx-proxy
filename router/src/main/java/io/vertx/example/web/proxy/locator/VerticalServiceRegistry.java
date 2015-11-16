package io.vertx.example.web.proxy.locator;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.Closeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * service registry for single vertical
 */
public class VerticalServiceRegistry implements Closeable {
    private final Set<ServiceDescriptor> services = new HashSet<>();

    public VerticalServiceRegistry() {}

    public VerticalServiceRegistry(Set<ServiceDescriptor> services ) {
        services.stream().forEach(this::register);
    }

    public VerticalServiceRegistry register(ServiceDescriptor descriptor) {
        this.services.add(descriptor);
        return this;
    }

    public VerticalServiceRegistry unregister(ServiceDescriptor descriptor) {
        this.services.remove(descriptor);
        return this;
    }

    public int size() {
        return services.size();
    }

    public Collection<ServiceDescriptor> getServices() {
        return Collections.unmodifiableCollection(services);
    }

    @Override
    public void close(Handler<AsyncResult<Void>> completionHandler) {
        this.services.clear();
    }
}
