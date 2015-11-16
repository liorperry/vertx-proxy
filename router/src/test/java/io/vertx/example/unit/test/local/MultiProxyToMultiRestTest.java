package io.vertx.example.unit.test.local;

import com.codahale.metrics.health.HealthCheck;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.example.web.proxy.ProxyServer;
import io.vertx.example.web.proxy.SimpleREST;
import io.vertx.example.web.proxy.filter.ProductFilter;
import io.vertx.example.web.proxy.filter.ServiceFilter;
import io.vertx.example.web.proxy.healthcheck.InMemReporter;
import io.vertx.example.web.proxy.locator.InMemServiceLocator;
import io.vertx.example.web.proxy.locator.VerticalServiceRegistry;
import io.vertx.example.web.proxy.repository.KeysRepository;
import io.vertx.example.web.proxy.repository.LocalCacheKeysRepository;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.example.web.proxy.VertxInitUtils.ENABLE_METRICS_PUBLISH;
import static io.vertx.example.web.proxy.VertxInitUtils.HTTP_PORT;
import static io.vertx.example.web.proxy.filter.Filter.FilterBuilder.filterBuilder;
import static junit.framework.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class MultiProxyToMultiRestTest {

    public static final int PROXY_PORT = 8080;
    private static final int REST1_PORT = 8082;
    private static final int REST2_PORT = 8083;

    public static final String LOCALHOST = "localhost";

    //SERVICES
    private static final String WHO_AM_I = "whoAmI";


    private static Set<String> serviceProvidersAddress;
    private static Vertx vertx;
    private static KeysRepository keysRepository;
    private static InMemServiceLocator locator;

    //realtime set of available services
    private static VerticalServiceRegistry registry = new VerticalServiceRegistry();


    @BeforeClass
    public static void setUp(TestContext context) throws IOException, InterruptedException {
        //start verticals
        vertx = Vertx.vertx();

        serviceProvidersAddress = new HashSet<>();
        //deploy rest server
        vertx.deployVerticle(new SimpleREST(new InMemReporter(registry)),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST1_PORT)),
                context.asyncAssertSuccess());

        vertx.deployVerticle(new SimpleREST(new InMemReporter(registry)),
                new DeploymentOptions().setConfig(new JsonObject().put(HTTP_PORT, REST2_PORT)),
                context.asyncAssertSuccess());


        //keys & routes repository
        keysRepository= new LocalCacheKeysRepository();
        keysRepository.addService(WHO_AM_I, true);

        //proxy vertical deployment
        locator = new InMemServiceLocator(ProxyServer.PROXY,registry);
        vertx.deployVerticle(new ProxyServer(
                        filterBuilder(keysRepository)
                                .add(new ServiceFilter())
                                .add(new ProductFilter())
                                .build(),
                        (result, domain, descriptor) -> HealthCheck.Result.healthy(),
                        locator),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put(HTTP_PORT, PROXY_PORT)
                        .put(ENABLE_METRICS_PUBLISH, false)),
                context.asyncAssertSuccess());
    }

    @Test
    public void testServiceWhoAmIFirst(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                serviceProvidersAddress.add(entries1.encodePrettily());
                async.complete();
            });
        });

    }

    @Test
    public void testServiceWhoAmISecond(TestContext context) throws InterruptedException {
        Async async = context.async();
        // Send a request and get a response
        final String requestURI = "/" + WHO_AM_I;
        HttpClient httpClient = vertx.createHttpClient();

        //first request
        httpClient.getNow(PROXY_PORT, LOCALHOST, requestURI, resp1 -> {
            resp1.bodyHandler(body1 -> {
                JsonObject entries1 = new JsonObject(body1.toString());
                System.out.println(requestURI + ":" + entries1.encodePrettily());
                serviceProvidersAddress.add(entries1.encodePrettily());
                async.complete();
            });
        });
    }

    @AfterClass
    public static void tearDown() throws Exception {
        System.out.println("******** serviceProvidersAddress **********");
        serviceProvidersAddress.stream().forEach(System.out::println);
        assertEquals(serviceProvidersAddress.size(), 2);
        vertx.close();
    }
}
