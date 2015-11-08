package io.vertx.example.web.proxy.locator;

import io.vertx.core.impl.Closeable;

import java.util.Optional;

public interface ServiceLocator extends Closeable {

    Optional<String> getService(String uri);

    String getDomain();

    static int getPort(String address) {
        return Integer.parseInt(address.substring(address.indexOf(":") + 1, address.length()));
    }

    static String getHost(String address) {
        return address.substring(0, address.indexOf(":"));
    }


}