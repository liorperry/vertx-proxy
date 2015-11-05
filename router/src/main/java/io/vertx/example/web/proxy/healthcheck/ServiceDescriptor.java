package io.vertx.example.web.proxy.healthcheck;

public class ServiceDescriptor {
    public String serviceName;
    public String host;
    public String port;

    public ServiceDescriptor(String serviceName, String host, String port) {
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

    public String getPort() {
        return port;
    }

}
