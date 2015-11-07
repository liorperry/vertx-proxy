package io.vertx.example.web.proxy.launchers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.example.util.Runner;
import io.vertx.example.web.proxy.ProxyServer;

import java.io.IOException;

import static io.vertx.example.web.proxy.VertxInitUtils.*;

public class LauncherProxy extends AbstractVerticle {

    public static void main(String[] args) throws IOException {
        Runner.runExample(LauncherProxy.class, initOptions(), initDeploymentOptions());
    }

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(
                "io.vertx.example.web.proxy.ProxyServer",
                new DeploymentOptions().setInstances(2));

    }

}