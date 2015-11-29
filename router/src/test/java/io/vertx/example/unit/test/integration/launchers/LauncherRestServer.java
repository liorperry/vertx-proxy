package io.vertx.example.unit.test.integration.launchers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.example.util.Runner;

import java.io.IOException;
import static io.vertx.example.web.proxy.VertxInitUtils.*;

public class LauncherRestServer extends AbstractVerticle {

    public static void main(String[] args) throws IOException {
        Runner.runExample(LauncherRestServer.class, initOptions(), initDeploymentOptions());
    }

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(
                "io.vertx.example.unit.test.integration.SimpleREST",
                new DeploymentOptions().setInstances(2));

    }

}