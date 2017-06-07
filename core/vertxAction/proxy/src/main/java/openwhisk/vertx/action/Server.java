package openwhisk.vertx.action;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;

import java.util.Collections;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Server {

    private Vertx vertx;

    public static void main(String[] args) throws InterruptedException {
        new Server().start();
    }

    private void start() throws InterruptedException {
        vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create(true));
        router.post("/init").handler(this::init);
        router.post("/run").handler(this::run);

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8080);

    }

    private void run(RoutingContext rc) {
        vertx.eventBus().<JsonObject>send("invocation", rc.getBodyAsJson().getJsonObject("value", new JsonObject()), reply -> {
            if (reply.failed()) {
                rc.response().setStatusCode(500).end(reply.cause().getMessage());
            } else {
                rc.response().end(reply.result().body().encode());
            }
        });
    }

    private void init(RoutingContext rc) {
        JsonObject json = rc.getBodyAsJson();
        JsonObject message = json.getJsonObject("value");

        String verticle = message.getString("main");
        byte[] binary = message.getBinary("code");

        Loader.writeCodeOnFileSystem(vertx, binary, ar -> {
            if (ar.failed()) {
                rc.fail(ar.cause());
            } else {
                if (!ar.result().toFile().isFile()) {
                    rc.response().setStatusCode(500).end("Cannot find " + ar.result().toAbsolutePath());
                }
                // Jar file saved, try to deploy the verticle
                DeploymentOptions options = new DeploymentOptions()
                    .setIsolationGroup("verticle_group")
                    .setExtraClasspath(Collections.singletonList(ar.result().toFile().getAbsolutePath()));
                vertx.deployVerticle(verticle, options, deployed -> {
                    if (deployed.failed()) {
                        deployed.cause().printStackTrace();
                        rc.response().setStatusCode(500).end("Unable to load" + verticle);
                    } else {
                        rc.response().end("OK");
                    }
                });
            }
        });
    }


}
