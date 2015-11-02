package io.vertx.example.web.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.example.util.Runner;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

/**
 * Created by lior on 28/10/2015.
 */
public class LauncherProxy extends AbstractVerticle {

    public static void main(String[] args) {
        System.out.println("Proxy accepting requests: " + ProxyServer.PORT);

        VertxOptions options = new VertxOptions()
                .setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true))
                .setClustered(false);
        //run
        Runner.runExample(ProxyServer.class, options);
    }

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(
                "io.vertx.example.web.proxy.ProxyServer",
                new DeploymentOptions().setInstances(2));

    }

}