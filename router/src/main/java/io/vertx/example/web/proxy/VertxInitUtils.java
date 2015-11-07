package io.vertx.example.web.proxy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.io.IOException;
import java.net.ServerSocket;

public interface VertxInitUtils {
    String HTTP_PORT = "http.port";
    String ENABLE_METRICS_PUBLISH = "enable.metrics.publish";

    static VertxOptions initOptions() {
        return new VertxOptions()
                .setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true))
                .setClustered(false);
    }

    static DeploymentOptions initDeploymentOptions() throws IOException {
        //locate open port
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();

        return new DeploymentOptions().setConfig(new JsonObject()
                .put(ENABLE_METRICS_PUBLISH, true)
                .put(HTTP_PORT, port));
    }

}
