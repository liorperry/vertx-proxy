package io.vertx.example.web.proxy;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.example.util.Runner;
import io.vertx.example.web.proxy.filter.Filter;
import io.vertx.example.web.proxy.filter.Filter.FilterBuilder;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.repository.RedisRepository;
import io.vertx.example.web.proxy.repository.Repository;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;


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

    private Filter filter;
    private Repository repository;

    @Override
    public void init(io.vertx.core.Vertx vertx, Context context) {
        super.init(vertx, context);
        repository = new RedisRepository();
        //build chain of filters
        filter = FilterBuilder.filterBuilder(repository)
                .add(new ServiceFilter())
                .add(new ProductFilter())
                .build();
    }

    @Override
    public void start() throws Exception {
        // If a config file is set, read the host and port.
        HttpClient client = vertx.createHttpClient(new HttpClientOptions());
        vertx.createHttpServer().requestHandler(req -> {
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
        }).listen(PORT);
    }

}