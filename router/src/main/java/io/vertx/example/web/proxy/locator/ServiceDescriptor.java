package io.vertx.example.web.proxy.locator;

import com.google.common.collect.Sets;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static io.vertx.example.web.proxy.locator.ServiceLocator.DEFAULT_VERSION;

public class ServiceDescriptor implements Comparable<ServiceDescriptor>{
    public static final String NAME = "name";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String VERSION = "version";
    public static final String SERVICE_VERSION = "serviceVersion";

    private ServiceVersion serviceVersion;
    private String host;
    private int port;

    ServiceDescriptor(ServiceVersion serviceVersion, String host, int port) {
        this.serviceVersion = serviceVersion;
        this.host = host;
        this.port = port;
    }



    public ServiceVersion getServiceVersion() {
        return serviceVersion;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static ServiceDescriptor create(ServiceVersion serviceVersion, int port)  {
        return new ServiceDescriptor(serviceVersion,"127.0.0.1",port);
    }

    public static ServiceDescriptor create(ServiceVersion serviceVersion, String hostAddress, int port)  {
        return new ServiceDescriptor(serviceVersion, hostAddress,port);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceDescriptor that = (ServiceDescriptor) o;

        if (port != that.port) return false;
        if (!serviceVersion.equals(that.serviceVersion)) return false;
        return host.equals(that.host);

    }

    @Override
    public int hashCode() {
        int result = serviceVersion.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + port;
        return result;
    }

    public static ServiceDescriptor create(String serviceName)  {
        return create(serviceName,8080);
    }

    public static ServiceDescriptor create(String serviceName, int port)  {
        String hostAddress = "127.0.0.1";
        try {
            hostAddress = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return create(new ServiceVersion(serviceName, DEFAULT_VERSION),hostAddress, port);
    }

    public static ServiceDescriptor create(JsonObject entry)  {
        String version = entry.getString(VERSION);
        JsonObject jsoServiceVersion = entry.getJsonObject(SERVICE_VERSION);
        JsonArray jsonArray = jsoServiceVersion.getJsonArray(VERSION);
        String[] versions = jsonArray.stream().map(s -> ((JsonObject) s).getString(VERSION)).toArray(String[]::new);

        return create(new ServiceVersion(jsoServiceVersion.getString(NAME),
                        version,
                        Sets.newHashSet(versions)),
                entry.getString(HOST),entry.getInteger(PORT));
    }

    @Override
    public int compareTo(ServiceDescriptor o) {
        return (this.hashCode()-o.hashCode());
    }
}
