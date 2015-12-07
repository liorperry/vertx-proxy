package io.vertx.example.kafka.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.example.util.kafka.InMemSamplePersister;
import io.vertx.example.util.kafka.OutlierDetector;
import io.vertx.example.util.kafka.SampleData;
import io.vertx.example.util.kafka.SimpleDistanceOutlierDetector;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.Optional;

public class OutlierWebServer extends AbstractVerticle {
    private OutlierDetector detector;
    private int port;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        InMemSamplePersister persister = new InMemSamplePersister();
        SimpleDistanceOutlierDetector detector = new SimpleDistanceOutlierDetector(persister);
        persister.persist(KafkaTestUtils.create("norbert", 100, 15));
        persister.persist(KafkaTestUtils.create("ginzburg", 300, 10));
        vertx.deployVerticle(new OutlierWebServer(detector, 8081), options);
    }

    public OutlierWebServer(OutlierDetector detector, int port) {
        this.detector = detector;
        this.port = port;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);
        //http server
        HttpServer httpServer = vertx.createHttpServer();

        router.route().handler(BodyHandler.create());
        router.get("/outlier/:publisherId").handler(this::outlier);

        httpServer.requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void outlier(RoutingContext routingContext) {
        String publisherId = routingContext.request().getParam("publisherId");
        HttpServerResponse response = routingContext.response();
        if (publisherId == null) {
            sendError(400, response);
        }
        Optional<String> windowSize = Optional.ofNullable(routingContext.request().getParam("windowSize"));
        Optional<String> outlierFactor = Optional.ofNullable(routingContext.request().getParam("outlierFactor"));

        List<SampleData> outliers = detector.getOutlier(publisherId, Integer.valueOf(windowSize.orElse("10")), Optional.of(Double.valueOf(outlierFactor.orElse("2"))));
        JsonArray array = new JsonArray(outliers);
        response.putHeader("content-type", "application/json").end(array.encodePrettily());
    }


    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

}