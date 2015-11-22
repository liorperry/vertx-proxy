package io.vertx.example.web.proxy;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.events.MongoMessageProcessor;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.events.Subscriber;
import io.vertx.example.web.proxy.healthcheck.InMemHealthReporter;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.IOException;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;

public class MongoStarted extends AbstractVerticle {
    private static final String DWH_LOG = "DWH";
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String MONGO_DB = "mongo_db";
    public static final String LOG = "log.";
    public static final int MONGO_PORT = 53706;

    static DeploymentOptions options = VertxInitUtils.initDeploymentOptions();

    private int port;
    private HealthReporter healthReporter;
    private Publisher publisher;
    private Subscriber subscriber;

    private static MongodExecutable mongodExecutable = null;
    private static MongodProcess mongod;
    private static VerticalServiceRegistry registry;
    private static JsonObject mongoconfig;

    private MongoClient mongoClient;

    public static void main(String[] args) throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        initMongo(starter);
        Vertx vertx = Vertx.vertx(VertxInitUtils.initOptions());
//        vertx.deployVerticle(new MongoStarted(new InMemHealthReporter(new VerticalServiceRegistry()), EventBus.EMPTY, Subscriber.EMPTY ),options);
        vertx.deployVerticle(new MongoStarted(new InMemHealthReporter(new VerticalServiceRegistry()), Publisher.EMPTY, Subscriber.EMPTY ),options);
    }

    public MongoStarted(HealthReporter healthReporter, Publisher publisher,Subscriber subscriber) {
        this.healthReporter = healthReporter;
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    public void start(Future<Void> fut) throws IOException {
        registry = new VerticalServiceRegistry();
        port = vertx.getOrCreateContext().config().getInteger(HTTP_PORT);

        //http server
        HttpServer httpServer = vertx.createHttpServer();

        //set services health checks
        HealthReporter.setUpHealthCheck(getVertx(), DWH_LOG, registry, healthReporter, 2000);

        // Send a metrics events every second
        if (getVertx().getOrCreateContext().config().containsKey(ENABLE_METRICS_PUBLISH) &&
                getVertx().getOrCreateContext().config().getBoolean(ENABLE_METRICS_PUBLISH)) {
            HealthReporter.setUpStatisticsReporter(ServiceDescriptor.create(DWH_LOG, port), vertx, publisher, httpServer, 3000);
        }

        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", MONGO_URI)
                .put("db_name", MONGO_DB);

        //init mongo cliennt
        mongoClient = MongoClient.createShared(vertx, mongoconfig);

        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/log/:logId").handler(this::getLog);
        router.post("/log/:logId").handler(this::addLog);

        subscriber.subscribe(DWH_LOG, new MongoMessageProcessor(mongoClient));

        httpServer.requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });

    }

    private void addLog(RoutingContext routingContext) {
        System.out.println("DWH:" + routingContext.request().uri());
        String productID = routingContext.request().getParam("logId");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject log = routingContext.getBodyAsJson();
            if (log == null) {
                sendError(400, response);
            } else {
                addToStorage(log);
                response.end();
            }
        }

    }

    private void addToStorage(JsonObject log) {
        mongoClient.save(LOG + log.getString("user"), log, id -> {
            System.out.println("Inserted id: " + id.result());
        });
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void getLog(RoutingContext routingContext) {
        System.out.println("DWH:" + routingContext.request().uri());
        String user = routingContext.request().getParam("user");
        mongoClient.find(LOG + user, new JsonObject(), res -> {
            res.result().stream().forEach(JsonObject::encodePrettily);
        });

    }

    public static void initMongo(MongodStarter starter) throws IOException {
//        int port = VertxInitUtils.getNextAvailablePort();
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MONGO_PORT, false))
                .build();
        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
        try {
            mongoClient.close();
            if (mongodExecutable != null) {
                mongod.stop();
                mongodExecutable.stop();
            }
            stopFuture.complete();
        } catch (EmbeddedRedisException e) {
            stopFuture.fail(e);
        }
    }


}
