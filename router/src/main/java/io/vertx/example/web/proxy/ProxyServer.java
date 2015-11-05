package io.vertx.example.web.proxy;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;

import io.vertx.example.util.Runner;
import io.vertx.example.web.proxy.events.EventBus;
import io.vertx.example.web.proxy.events.RedisEventBus;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.filter.Filter.FilterBuilder;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.RestServiceHealthCheck;
import io.vertx.example.web.proxy.repository.RedisRepository;
import io.vertx.example.web.proxy.repository.Repository;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;
import io.vertx.ext.dropwizard.MetricsService;
import redis.clients.jedis.Jedis;


public class ProxyServer extends AbstractVerticle {

    public static final int PORT = 8080;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions()
                .setMetricsOptions(new DropwizardMetricsOptions().setEnabled(true))
                .setClustered(false);
        //run
        Runner.runExample(ProxyServer.class,options);
    }

    private Jedis client;
    private Filter filter;
    private Repository repository;
    private EventBus bus;

    @Override
    public void init(io.vertx.core.Vertx vertx, Context context) {
        super.init(vertx, context);
        client = new Jedis();
        repository = new RedisRepository(client);
        bus = new RedisEventBus();

        //build chain of filters
        filter = FilterBuilder.filterBuilder(repository)
                .add(new ServiceFilter())
                .add(new ProductFilter())
                .build();
    }

    private void setUpHealthCheck() {
        final HealthCheckRegistry healthChecks = new HealthCheckRegistry();
        healthChecks.register("servicesRestCheck", new RestServiceHealthCheck("proxy",client));
        //run periodic health checks
        vertx.setPeriodic(2000, t -> healthChecks.runHealthChecks());
    }

    @Override
    public void start(Future<Void> fut) throws Exception {
        setUpHealthCheck();

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
        vertx.setPeriodic(1000, t -> {
            String value = metricsService.getMetricsSnapshot(httpServer).encodePrettily();
            bus.publish("metrics", value);
        });

        //request handling
        httpServer.requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
            if (!filter.filter(req)) {
                req.response().setChunked(true);
                req.response().setStatusCode(HttpResponseStatus.FORBIDDEN.code());
                req.handler(data -> {
                    String msg = "Proxying request body " + data.toString("ISO-8859-1");
                    System.out.println(msg);
                    req.response().write(msg);
                });
                req.endHandler((v) -> req.response().end());

            } else {
                //send message stratistics
                vertx.eventBus().send("whatever", req.uri());

                HttpClientRequest c_req = client.request(req.method(), SimpleREST.PORT, "localhost", req.uri(), c_res -> {
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
        }).listen(PORT, result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

}