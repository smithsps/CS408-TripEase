package com.cs408.tripease;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.StaticHandler;


public class TripEaseServer extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(TripEaseServer.class);

    private static int port;
    private static String hostname;

    @Override
    public void start() {
        Vertx vertx = Vertx.vertx();

        HttpServer server = Vertx.vertx().createHttpServer();
        Router router = Router.router(vertx);
        Route route = router.route("/*");

        StaticHandler staticHandler = StaticHandler.create();
        //As we are still developing we don't want to cache files.
        staticHandler.setCachingEnabled(false);

        route.handler(staticHandler);


        System.out.println("TripEase server started at port:" + port + " on " + hostname);
        server.requestHandler(router::accept).listen(port, hostname);
    }

    public static void main(String[] args) {
        try {
            port = Integer.parseInt(System.getenv("OPENSHIFT_VERTX_PORT"));
        } catch (NullPointerException | NumberFormatException ex) {
            TripEaseServer.log.info("Could not find Openshift port, using port 8080.");
            port = 8080;
        }

        try {
            hostname = System.getenv("OPENSHIFT_VERTX_IP");
            if (hostname == null) {
                throw new NullPointerException();
            }
        } catch (NullPointerException | NumberFormatException nullex) {
            TripEaseServer.log.info("Could not find Openshift hostname, using localhost.");
            hostname = "localhost";
        }

        //Not sure if more preferred way to start.
        TripEaseServer server = new TripEaseServer();
        server.start();
    }
}
