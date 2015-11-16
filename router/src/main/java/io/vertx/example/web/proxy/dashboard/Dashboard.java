package io.vertx.example.web.proxy.dashboard;

import com.google.common.collect.Sets;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.events.EventBus;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceVersion;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.Collection;
import java.util.Optional;

import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.events.EmptyEventBus.EMPTY;

public class Dashboard extends AbstractVerticle {

    public static final String REST = "REST";
    private static final String SERVICE = "serviceA";

    private final InMemServiceLocator serviceLocator;
    private EventBus eventBus;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = VertxInitUtils.initDeploymentOptions();
        ServiceDescriptor descriptor1 = ServiceDescriptor.create(new ServiceVersion(SERVICE, "1"), "localhost", 8080);
        ServiceDescriptor descriptor2 = ServiceDescriptor.create(new ServiceVersion(SERVICE, "1"), "localhost", 8081);
        VerticalServiceRegistry registry = new VerticalServiceRegistry(Sets.newHashSet(descriptor1, descriptor2));

        vertx.deployVerticle(new Dashboard(new InMemServiceLocator(REST, registry), EMPTY), options);
        System.out.println("Dashboard accepting requests: " + VertxInitUtils.getPort(options));
    }

    public Dashboard(InMemServiceLocator serviceLocator, EventBus eventBus) {
        this.serviceLocator = serviceLocator;
        this.eventBus = eventBus;
    }

    @Override
    public void start() {
        int port = vertx.getOrCreateContext().config().getInteger(HTTP_PORT);

        Router router = Router.router(vertx);

        // Allow outbound traffic to the news-feed address

        BridgeOptions options = new BridgeOptions().
                addOutboundPermitted(
                        new PermittedOptions().
                                setAddress("metrics")
                );

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options));

        // Serve the static resources
        router.post("/manage/service/block/:id").handler(this::blockService);
        router.post("/manage/service/unblock/:id").handler(this::unblockService);
        router.get("/manage/service/:uri").handler(this::getNextServices);
        router.get("/manage/services").handler(this::getSupportedServices);
        router.route().handler(StaticHandler.create());

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(port);

        // Send a metrics events every 5 second
        vertx.setPeriodic(5000, t -> {
            Optional metrics = eventBus.subscribe("metrics");
            if (metrics.isPresent()) {
                vertx.eventBus().publish("metrics", metrics.get());
            }
        });
    }

    private void getNextServices(RoutingContext routingContext) {
        String uri = routingContext.request().getParam("uri");
        String version = routingContext.request().getParam("version");
        if (uri == null) {
            routingContext.response().setStatusCode(400).end();
        } else {
            Optional<ServiceDescriptor> service = serviceLocator.getService(uri, version);
            if (!service.isPresent()) {
                routingContext.response().setStatusCode(400).end();
            } else {
                routingContext.response().putHeader("content-type", "application/json").end(Json.encodePrettily(service.get()));
            }
        }
    }

    private void unblockService(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        Optional<ServiceDescriptor> serviceDescriptor = serviceLocator.unblockServiceProvider(id);
        if (!serviceDescriptor.isPresent()) {
            routingContext.response().setStatusCode(400).end();
        } else {
            routingContext.response().putHeader("content-type", "application/json").end(Json.encodePrettily(serviceDescriptor));
        }
    }

    private void blockService(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        Optional<ServiceDescriptor> descriptor = serviceLocator.blockServiceProvider(id);
        if (!descriptor.isPresent()) {
            routingContext.response().setStatusCode(400).end();
        } else {
            routingContext.response().putHeader("content-type", "application/json").end(Json.encodePrettily(descriptor));
        }
    }

    private void getSupportedServices(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        Collection<ServiceDescriptor> providers = serviceLocator.getAllProviders();
        providers.forEach(descriptor -> {
            arr.add(Json.encode(descriptor));
        });
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());

    }
}
