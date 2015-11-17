package io.vertx.example.web.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.healthcheck.Reporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;

import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;

public class SimpleREST extends AbstractVerticle {


    public static final String REST = "REST";
    private VerticalServiceRegistry verticalServiceRegistry;
    private Reporter reporter;
    private int port;
    private long timer;

    public SimpleREST(Reporter reporter) {
        this.reporter = reporter;
    }

    private Map<String, JsonObject> products = new HashMap<>();

/*
// removed - causing error on stoping the testing junit vert.x threads
    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        verticalServiceRegistry.close(event -> {stopFuture.complete();});
        reporter.close(event -> {stopFuture.complete();});
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

        //set services health checks
        timer = Reporter.setUpHealthCheck(getVertx(), REST, verticalServiceRegistry, reporter, 2000);

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

        vertx.createHttpServer().requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private void whoAmIHandler(RoutingContext routingContext) {
        System.out.println("Rest:"+routingContext.request().uri() + " : " + port  );
        HttpServerResponse response = routingContext.response();
        JsonObject entries = new JsonObject();
        entries.put(routingContext.request().uri(),port);
        response.putHeader("content-type", "application/json").end(entries.encodePrettily());
    }

    private void handleGetProduct(RoutingContext routingContext) {
        System.out.println("Rest:"+routingContext.request().uri());
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
        System.out.println("Rest:"+routingContext.request().uri());
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
        System.out.println("Rest:"+routingContext.request().uri());
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