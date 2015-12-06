package io.vertx.example.web.proxy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.io.IOException;
import java.net.ServerSocket;

public interface VertxInitUtils {
    String HTTP_PORT = "http.port";
    String ENABLE_METRICS_PUBLISH = "enable.metrics.publish";

    static VertxOptions initOptions() {
       return new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/")).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/.*").setType(MatchType.REGEX))

        ).setClustered(false);
    }

    static DeploymentOptions initDeploymentOptions() {
        return initDeploymentOptions(true);
    }

    static DeploymentOptions initDeploymentOptions(boolean enableMetricsPublish) {
        //locate open port
        int port = 8282;
        try {
            port = getNextAvailablePort();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Init deployment options selected port: " + port);
        return new DeploymentOptions().setConfig(new JsonObject()
                .put(ENABLE_METRICS_PUBLISH, enableMetricsPublish)
                .put(HTTP_PORT, port));
    }

    static int getNextAvailablePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        socket.close();
        return socket.getLocalPort();
    }

    static int getPort(DeploymentOptions options) {
        return getPort(options,HTTP_PORT);
    }

    static int getPort(DeploymentOptions options,String constant) {
        return options.getConfig().getInteger(constant);
    }
}
