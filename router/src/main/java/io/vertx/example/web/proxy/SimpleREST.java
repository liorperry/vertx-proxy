package io.vertx.example.web.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;

public class SimpleREST extends AbstractVerticle {


    public static final String REST = "REST";
    private VerticalServiceRegistry verticalServiceRegistry;
    private HealthReporter healthReporter;
    private Publisher healthPublisher;
    private int port;
    private long timer;
    private Publisher logPublisher;

    public SimpleREST(HealthReporter healthReporter) {
        this(healthReporter, Publisher.EMPTY , Publisher.EMPTY );
    }

    public SimpleREST(HealthReporter healthReporter, Publisher healthPublisher, Publisher logPublisher) {
        this.healthReporter = healthReporter;
        this.healthPublisher = healthPublisher;
        this.logPublisher = logPublisher;
    }

    private Map<String, JsonObject> products = new HashMap<>();

/*
// removed - causing error on stoping the testing junit vert.x threads
    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        verticalServiceRegistry.close(event -> {stopFuture.complete();});
        healthReporter.close(event -> {stopFuture.complete();});
    }
*/

    @Override
    public void start(Future<Void> fut) {
        verticalServiceRegistry = new VerticalServiceRegistry();
        port = vertx.getOrCreateContext().config().getInteger(HTTP_PORT);
        //register services
        verticalServiceRegistry.register(ServiceDescriptor.create("serviceA", port));
        verticalServiceRegistry.register(ServiceDescriptor.create("serviceB", port));
        verticalServiceRegistry.register(ServiceDescriptor.create("whoAmI", port));

        //http server
        HttpServer httpServer = vertx.createHttpServer();

        //set services health checks
        timer = HealthReporter.setUpHealthCheck(getVertx(), REST, verticalServiceRegistry, healthReporter, 2000);

        // Send a metrics events every second
        if (getVertx().getOrCreateContext().config().containsKey(ENABLE_METRICS_PUBLISH) &&
                getVertx().getOrCreateContext().config().getBoolean(ENABLE_METRICS_PUBLISH)) {
            HealthReporter.setUpStatisticsReporter(ServiceDescriptor.create(REST, port), vertx, healthPublisher, httpServer, 3000);
        }

        setUpInitialData();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/whoAmI").handler(this::whoAmIHandler);
        router.get("/serviceA/:productID").handler(this::handleGetProduct);
        router.put("/serviceA/:productID").handler(this::handleAddProduct);
        router.get("/serviceA").handler(this::handleListProducts);

        router.get("/serviceB/:productID").handler(this::handleGetProduct);
        router.put("/serviceB/:productID").handler(this::handleAddProduct);
        router.get("/serviceB").handler(this::handleListProducts);

        httpServer.requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private void whoAmIHandler(RoutingContext routingContext) {
        logRequest(routingContext);
        HttpServerResponse response = routingContext.response();
        JsonObject entries = new JsonObject();
        entries.put(routingContext.request().uri(), port);
        response.putHeader("content-type", "application/json").end(entries.encodePrettily());
    }

    private void logRequest(RoutingContext routingContext) {
        String msg = "Rest:" + routingContext.request().uri() + " : " + port;
        System.out.println(msg);
        vertx.runOnContext(event -> logPublisher.publish("DWH", msg));
    }

    private void handleGetProduct(RoutingContext routingContext) {
        logRequest(routingContext);
        String productID = routingContext.request().getParam("productID");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = products.get(productID);
            if (product == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(product.encodePrettily());
            }
        }
    }

    private void handleAddProduct(RoutingContext routingContext) {
        logRequest(routingContext);
        String productID = routingContext.request().getParam("productID");
        HttpServerResponse response = routingContext.response();
        if (productID == null) {
            sendError(400, response);
        } else {
            JsonObject product = routingContext.getBodyAsJson();
            if (product == null) {
                sendError(400, response);
            } else {
                products.put(productID, product);
                response.end();
            }
        }
    }

    private void handleListProducts(RoutingContext routingContext) {
        logRequest(routingContext);
        JsonArray arr = new JsonArray();
        products.forEach((k, v) -> arr.add(v));
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    private void setUpInitialData() {
        addProduct(new JsonObject().put("id", "prod3568").put("name", "Egg Whisk").put("price", 3.99).put("weight", 150));
        addProduct(new JsonObject().put("id", "prod7340").put("name", "Tea Cosy").put("price", 5.99).put("weight", 100));
        addProduct(new JsonObject().put("id", "prod8643").put("name", "Spatula").put("price", 1.00).put("weight", 80));
    }

    private void addProduct(JsonObject product) {
        products.put(product.getString("id"), product);
    }
}