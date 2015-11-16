package io.vertx.example.web.proxy.dashboard;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.example.web.proxy.VertxInitUtils;
import io.vertx.example.web.proxy.events.EventBus;
import io.vertx.example.web.proxy.events.RedisEventBus;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceVersion;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.Collection;
import java.util.Optional;

public class Dashboard extends AbstractVerticle {

    public static final String REST = "REST";
    public static final int PORT = 8181;

    public static final VertxOptions DROPWIZARD_OPTIONS = new VertxOptions().
            setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true));

    private final InMemServiceLocator serviceLocator;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        System.out.println("Dashboard accepting requests: "+ Dashboard.PORT);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Dashboard(new InMemServiceLocator(REST, new VerticalServiceRegistry())),
                VertxInitUtils.initDeploymentOptions());
    }

    public Dashboard(InMemServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    @Override
    public void start() {
        EventBus bus = new RedisEventBus();
        Router router = Router.router(vertx);

        // Allow outbound traffic to the news-feed address

        BridgeOptions options = new BridgeOptions().
                addOutboundPermitted(
                        new PermittedOptions().
                                setAddress("metrics")
                );

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(options));

        // Serve the static resources
        router.route().handler(StaticHandler.create());
        router.get("/manage/services").handler(this::getSupportedServices);

        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router::accept).listen(PORT);

        // Send a metrics events every 5 second
        vertx.setPeriodic(5000, t -> {
            Optional metrics = bus.subscribe("metrics");
            if(metrics.isPresent()) {
                vertx.eventBus().publish("metrics", metrics.get());
            }
        });


    }

    private void getSupportedServices(RoutingContext routingContext) {
        JsonArray arr = new JsonArray();
        Collection<ServiceDescriptor> providers = serviceLocator.getAllProviders();
        providers.forEach(arr::add);
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());

    }

}
