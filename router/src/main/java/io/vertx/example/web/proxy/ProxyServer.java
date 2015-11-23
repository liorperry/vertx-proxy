package io.vertx.example.web.proxy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.*;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.launchers.AbstractVerticalServer;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.ServiceLocator;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import io.vertx.ext.dropwizard.MatchType;

import java.util.Optional;

import static io.vertx.example.web.proxy.locator.ServiceLocator.DEFAULT_VERSION;


public class ProxyServer extends AbstractVerticalServer {

    public static final String PROXY = "PROXY";
    public static final String VERSION = "version";

    private Filter filter;
    private ServiceLocator locator;

    public ProxyServer(Filter filter, HealthReporter healthReporter,Publisher publisher, VerticalServiceRegistry verticalServiceRegistry, ServiceLocator locator) {
        super(PROXY,healthReporter, publisher, verticalServiceRegistry);
        this.filter = filter;
        this.locator = locator;
    }

/*
// removed - causing error on stopping the testing junit vert.x threads
    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop();
        verticalServiceRegistry.close(event -> {
        });
        healthReporter.close(event -> {
        });
        locator.close(event -> {
        });
        filter.close(event -> {
        });
    }
*/

    @Override
    public void doInStart(Future<Void> fut)  {
        // If a config file is set, read the host and port.
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        HttpServer httpServer = vertx.createHttpServer();

        //request handling
        httpServer.requestHandler(req -> {
            System.out.println("Proxying request: " + req.uri());
            if (!filter.filter(req)) {
                returnForbiddenResponse(req);
            } else {
                String version = req.getHeader(VERSION) != null ? req.getHeader(VERSION) : DEFAULT_VERSION;
                Optional<ServiceDescriptor> service = locator.getService(req.uri(), version);
                if (!service.isPresent()) {
                    returnForbiddenResponse(req);
                } else {
                    proxyRequestOn(client, req, service.get());
                }
            }
        }).listen(getPort(), result -> {
            if (result.succeeded()) {
                fut.complete();
            } else {
                fut.fail(result.cause());
            }
        });
    }

    private void proxyRequestOn(HttpClient client, HttpServerRequest req, ServiceDescriptor descriptor) {
        System.out.println("ServiceLocator:" + req.uri() + "->" + descriptor.getHost() + ":" + descriptor.getPort());
        HttpClientRequest c_req = client.request(req.method(), descriptor.getPort(), descriptor.getHost(), req.uri(), c_res -> {
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