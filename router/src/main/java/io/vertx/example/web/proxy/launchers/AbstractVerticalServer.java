package io.vertx.example.web.proxy.launchers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.example.web.proxy.events.Publisher;
import io.vertx.example.web.proxy.healthcheck.HealthReporter;
import io.vertx.example.web.proxy.locator.ServiceDescriptor;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;

public abstract class AbstractVerticalServer extends AbstractVerticle {
    private String domain;
    private HealthReporter healthReporter;
    private Publisher publisher;
    private VerticalServiceRegistry registry;
    private HttpServer httpServer;

    public AbstractVerticalServer(String domain, HealthReporter healthReporter, Publisher publisher, VerticalServiceRegistry registry) {
        this.domain = domain;
        this.healthReporter = healthReporter;
        this.publisher = publisher;
        this.registry = registry;
    }

    public final void start(Future<Void> fut) throws Exception {
        ServiceDescriptor descriptor = ServiceDescriptor.create(domain, getPort());
        registry.register(descriptor);

        //http server
        httpServer = vertx.createHttpServer();

        //set services health checks
        HealthReporter.setUpHealthCheck(getVertx(), domain, registry, healthReporter, 2000);
        //setup metrics statistics reporting
        setUpMetricsReporter(httpServer);
        //do additional
        doInStart(fut);
    }

    /**
     * future needs to be marked as completed
     * @param fut
     */
    public abstract void doInStart(Future<Void> fut) ;

    public final void registerService(ServiceDescriptor descriptor) {
        registry.register(descriptor);
    }

    private void setUpMetricsReporter(HttpServer httpServer) {
        // Send a metrics events every second
        if (getVertx().getOrCreateContext().config().containsKey(ENABLE_METRICS_PUBLISH) &&
                getVertx().getOrCreateContext().config().getBoolean(ENABLE_METRICS_PUBLISH)) {
            HealthReporter.setUpStatisticsReporter(ServiceDescriptor.create(domain, getPort()), vertx, publisher, httpServer, 3000);
        }
    }

    public final int getPort() {
        return vertx.getOrCreateContext().config().getInteger(HTTP_PORT);
    }

    public final HttpServer getHttpServer() {
        return httpServer;
    }
}
