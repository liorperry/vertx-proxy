package io.vertx.example.kafka.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.example.util.kafka.*;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OutlierWebServer extends AbstractVerticle {
    private OutlierDetector detector;
    private SamplePersister persister;
    private int port;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        SamplePersister persister = new RedisSamplePersister(new JedisPool(),new BasicSampleExtractor());
        SimpleDistanceOutlierDetector detector = new SimpleDistanceOutlierDetector(persister);
        persister.persist(KafkaTestUtils.create("norbert", 100, 15));
        persister.persist(KafkaTestUtils.create("ginzburg", 300, 10));
        vertx.deployVerticle(new OutlierWebServer(detector,persister, 8081), options);
    }

    public OutlierWebServer(OutlierDetector detector,SamplePersister persister, int port) {
        this.detector = detector;
        this.persister = persister;
        this.port = port;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router router = Router.router(vertx);
        //http server
        HttpServer httpServer = vertx.createHttpServer();

        router.route().handler(BodyHandler.create());
        router.get("/outlier/:publisherId").handler(this::outlier);
        router.get("/outlier").handler(this::outlierList);
        System.out.println("Listening for http://{hostname}:"+port+"/outlier/:{publisherId}?windowSize=10;outlierFactor=2");

        httpServer.requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void outlierList(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        Set<String> publishers = persister.getPublishers();
        JsonArray array = new JsonArray(new ArrayList<>(publishers));
        response.putHeader("content-type", "application/json").end(array.encodePrettily());
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