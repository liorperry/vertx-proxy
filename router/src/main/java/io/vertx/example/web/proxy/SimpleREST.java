package io.vertx.example.web.proxy;

import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.launchers.AbstractVerticalServer;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

public class SimpleREST extends AbstractVerticalServer {


    public static final String REST = "REST";
    private final Publisher logPublisher;


    public SimpleREST(HealthReporter healthReporter, Publisher healthPublisher, Publisher logPublisher,VerticalServiceRegistry registry) {
        super(REST,healthReporter,healthPublisher,registry);
        this.logPublisher = logPublisher;
    }

    private Map<String, JsonObject> products = new HashMap<>();


    public void doInStart(Future<Void> fut) {
        //register services
        registerService(ServiceDescriptor.create("serviceA", getPort()));
        registerService(ServiceDescriptor.create("serviceB", getPort()));
        registerService(ServiceDescriptor.create("whoAmI", getPort()));

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

        getHttpServer().requestHandler(router::accept).listen(getPort(), result -> {
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
        entries.put(routingContext.request().uri(), getPort());
        response.putHeader("content-type", "application/json").end(entries.encodePrettily());
    }

    private void logRequest(RoutingContext routingContext) {
        String msg = "Rest:" + routingContext.request().uri() + " : " + getPort();
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