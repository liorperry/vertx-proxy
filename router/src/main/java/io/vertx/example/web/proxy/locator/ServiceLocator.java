package io.vertx.example.web.proxy.locator;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.Closeable;

import java.util.Collection;
import java.util.Optional;

public interface ServiceLocator extends Closeable {
    String DEFAULT_VERSION = "1.0";

    Optional<ServiceDescriptor> getService(String uri, String version);

    String getDomain();

    static int getPort(String address) {
        return Integer.parseInt(address.substring(address.indexOf(":") + 1, address.length()));
    }

    static String getHost(String address) {
        return address.substring(0, address.indexOf(":"));
    }

    default void close(Handler<AsyncResult<Void>> completionHandler) {
    }

    Collection<ServiceDescriptor> getAllProviders(ServiceVersion serviceVersion);

    Collection<ServiceDescriptor> getAllProviders();

    void updateFromRegistry();
}