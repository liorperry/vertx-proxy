package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.repository.LocalCacheKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;

@RunWith(VertxUnitRunner.class)
public class ProxyToRestTest {

    public static final int PROXY_PORT = 8080;
    public static final int REST_PORT = 8082;
    public static final String LOCALHOST = "localhost";
    //SERVICES
    public static final String SERVICE_A_OPEN = "serviceA";
    public static final String SERVICE_B_BLOCKED = "serviceB";
    //PRODUCTS
    public static final String PROD3568_BLOCKED = "prod3568";
    public static final String PROD7340_OPED = "prod7340";
    public static final String PROD8643_OPEN = "prod8643";

    private static Vertx vertx;

    @BeforeClass
    public static void setUp(TestContext context) {
        vertx = Vertx.vertx();

        vertx.deployVerticle(new SimpleREST((result, domain, descriptor) -> HealthCheck.Result.healthy()),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST_PORT)),
                context.asyncAssertSuccess());


        Set<String> services = new LinkedHashSet<>();
        services.add("localhost:" + REST_PORT);

        //keys & routes repository
        LocalCacheKeysRepository repository = new LocalCacheKeysRepository();
        repository.getServices().put(SERVICE_A_OPEN, "true");
        repository.getServices().put(SERVICE_B_BLOCKED, "false");
        repository.getProducts().put(PROD3568_BLOCKED, "false");

        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(repository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        (result, domain, descriptor) -> HealthCheck.Result.healthy(),
                        InMemServiceLocator.create(
                                ProxyServer.PROXY,
                                Collections.singletonMap(SERVICE_A_OPEN, services)
                        )),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
    }

    @AfterClass
    public static void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testServiceA(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        // Send a request and get a response
        String requestURI = "/" + SERVICE_A_OPEN;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD3568_BLOCKED));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                context.assertTrue(body.toString().contains(PROD8643_OPEN));
                async.complete();
                client.close();
            });
        });
    }

    @Test
    public void testServiceAProd3568(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD3568_BLOCKED;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceB(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String requestURI = "/" + SERVICE_B_BLOCKED;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(resp.statusCode() == HttpResponseStatus.FORBIDDEN.code());
                client.close();
                async.complete();
            });
        });
    }

    @Test
    public void testServiceAProd7340(TestContext context) {
        HttpClient client = vertx.createHttpClient();
        Async async = context.async();
        String requestURI = "/" + SERVICE_A_OPEN + "/" + PROD7340_OPED;
        client.getNow(PROXY_PORT, LOCALHOST, requestURI, resp -> {
            resp.bodyHandler(body -> {
                System.out.println(requestURI + ":" + resp.statusCode() + " [" + body.toString() + "]");
                context.assertTrue(body.toString().contains(PROD7340_OPED));
                client.close();
                async.complete();
            });
        });
    }
}
