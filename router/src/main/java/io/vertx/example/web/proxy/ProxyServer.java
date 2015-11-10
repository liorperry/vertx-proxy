package io.vertx.example.web.proxy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.example.web.proxy.events.EventBus;
import io.vertx.example.web.proxy.events.RedisEventBus;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.healthcheck.Reporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceLocator;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;

import java.util.Optional;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.locator.ServiceLocator.getHost;
import static io.vertx.example.web.proxy.locator.ServiceLocator.getPort;


public class ProxyServer extends AbstractVerticle {

    public static final String PROXY = "PROXY";

    private Filter filter;
    private EventBus bus;
    private Reporter reporter;
    private ServiceLocator locator;
    private VerticalServiceRegistry verticalServiceRegistry;
    private int port;

    public ProxyServer(Filter filter, Reporter reporter, ServiceLocator locator) {
        this.filter = filter;
        this.reporter = reporter;
        this.locator = locator;
    }

    @Override
    public void init(io.vertx.core.Vertx vertx, Context context) {
        super.init(vertx, context);
        verticalServiceRegistry = new VerticalServiceRegistry();
        bus = new RedisEventBus();
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        verticalServiceRegistry.close(event -> {
        });
        reporter.close(event -> {
        });
        locator.close(event -> {
        });
        filter.close(event -> {
        });
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        port = vertx.getOrCreateContext().config().getInteger(HTTP_PORT);
        verticalServiceRegistry.register(ServiceDescriptor.create("manage", port));
        //set services health checks
        Reporter.setUpHealthCheck(getVertx(), PROXY, verticalServiceRegistry, reporter);

        // If a config file is set, read the host and port.
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        HttpServer httpServer = vertx.createHttpServer();

        // DropwizardMetricsOptions service matching
        Vertx vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                        setEnabled(true).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/")).
                        addMonitoredHttpServerUri(
                                new Match().setValue("/.*").setType(MatchType.REGEX))
        ));

        // set up server metrics
        MetricsService metricsService = MetricsService.create(vertx);

        // Send a metrics events every second
        if (getVertx().getOrCreateContext().config().getBoolean(ENABLE_METRICS_PUBLISH)) {
            vertx.setPeriodic(1000, t -> {
                String value = metricsService.getMetricsSnapshot(httpServer).encodePrettily();
                bus.publish("metrics", value);
            });
        }

        //request handling
        httpServer.requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
            if (!filter.filter(req)) {
                returnForbiddenResponse(req);
            } else {
                Optional<String> address = locator.getService(req.uri());
                if (!address.isPresent()) {
                    returnForbiddenResponse(req);
                } else {
                    proxyRequestOn(client, req, address.get());
                }
            }
        }).listen(this.port, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private void proxyRequestOn(HttpClient client, HttpServerRequest req, String address) {
        System.out.println("ServiceLocator:" + req.uri() + "->" + getHost(address) + ":" + getPort(address));
        HttpClientRequest c_req = client.request(req.method(), getPort(address), getHost(address), req.uri(), c_res -> {
            System.out.println("Proxying response: " + c_res.statusCode());
            req.response().setChunked(true);
            req.response().setStatusCode(c_res.statusCode());
            req.response().headers().setAll(c_res.headers());
            c_res.handler(data -> {
                System.out.println("Proxying response body: " + data.toString("ISO-8859-1"));
                req.response().write(data);
            });
            c_res.endHandler((v) -> req.response().end());
        });
        c_req.setChunked(true);
        c_req.headers().setAll(req.headers());
        req.handler(data -> {
            System.out.println("Proxying request body " + data.toString("ISO-8859-1"));
            c_req.write(data);
        });
        req.endHandler((v) -> c_req.end());
    }

    private void returnForbiddenResponse(HttpServerRequest req) {
        req.response().setChunked(true);
        req.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code());
        req.handler(data -> {
            String msg = "Proxying request body " + data.toString("ISO-8859-1");
            System.out.println(msg);
            req.response().write(msg);
        });
        req.endHandler((v) -> req.response().end());
    }

}