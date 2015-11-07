package io.vertx.example.web.proxy.healthcheck;

import io.vertx.example.web.proxy.ProxyServer;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ServiceDescriptor {
    private String serviceName;
    private String host;
    private int port;

    ServiceDescriptor(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
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

    public static ServiceDescriptor create(Class<ProxyServer> clazz, int port)  {
        String hostAddress = "127.0.0.1";
        try {
            hostAddress = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return new ServiceDescriptor(clazz.getSimpleName(), hostAddress,port);
    }
}
