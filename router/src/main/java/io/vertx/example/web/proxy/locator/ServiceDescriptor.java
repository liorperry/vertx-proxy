package io.vertx.example.web.proxy.locator;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

public class ServiceDescriptor {
    private String serviceName;
    private UUID uuid;
    private String host;
    private int port;

    ServiceDescriptor(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.uuid = UUID.randomUUID();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static ServiceDescriptor create(String serviceName,String hostAddress, int port)  {
        return new ServiceDescriptor(serviceName, hostAddress,port);
    }

    public static ServiceDescriptor create(String serviceName, int port)  {
        String hostAddress = "127.0.0.1";
        try {
            hostAddress = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return create(serviceName,hostAddress,port);
    }
}
