package io.vertx.example.web.proxy;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.events.MongoMessageProcessor;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.events.Subscriber;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.healthcheck.InMemHealthReporter;
import io.vertx.example.web.proxy.launchers.AbstractVerticalServer;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import redis.embedded.exceptions.EmbeddedRedisException;

import java.io.IOException;

public class MongoStarted extends AbstractVerticalServer {
    private static final String DWH_LOG = "DWH";
    private static final String MONGO_URI = "mongodb://localhost:27017";
    private static final String MONGO_DB = "mongo_db";
    public static final String LOG = "log.";
    public static final int MONGO_PORT = 53706;

    static DeploymentOptions options = VertxInitUtils.initDeploymentOptions();

    private Subscriber subscriber;

    private static MongodExecutable mongodExecutable = null;
    private static MongodProcess mongod;
    private static JsonObject mongoconfig;

    private MongoClient mongoClient;

    public static void main(String[] args) throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        initMongo(starter);
        Vertx vertx = Vertx.vertx(VertxInitUtils.initOptions());
        VerticalServiceRegistry registry = new VerticalServiceRegistry();
        vertx.deployVerticle(new MongoStarted(new InMemHealthReporter(registry), Publisher.EMPTY, Subscriber.EMPTY ,registry),options);
    }

    public MongoStarted(HealthReporter healthReporter, Publisher publisher,Subscriber subscriber,VerticalServiceRegistry registry) {
        super(DWH_LOG, healthReporter, publisher, registry);
        this.subscriber = subscriber;
    }

    public void doInStart(Future<Void> fut)  {
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

        getHttpServer().requestHandler(router::accept).listen(getPort(), result -> {
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
