package com.cs408.tripease;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.auth.AuthProvider;


public class TripEaseServer extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(TripEaseServer.class);

    private static int port;
    private static String hostname;

    private static int dbPort;
    private static String dbHostname;
    private static String dbUsername;
    private static String dbPassword;

    @Override
    public void start() {
        Vertx vertx = Vertx.vertx();

        HttpServer server = Vertx.vertx().createHttpServer();
        Router router = Router.router(vertx);

        //Main Page
        router.route().handler(StaticHandler.create());

        //Assets routing
        router.routeWithRegex(".+(fonts|css|images|js).*")
              .handler(StaticHandler.create());


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
        } catch (NullPointerException nullex) {
            TripEaseServer.log.info("Could not find Openshift hostname, using localhost.");
            hostname = "localhost";
        }

        try {
            dbHostname = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
            dbPort = Integer.parseInt(System.getenv("OPENSHIFT_MYSQL_DB_PORT"));
            dbUsername = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
            dbPassword = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
        } catch (NullPointerException | NumberFormatException ex) {
            TripEaseServer.log.fatal("Could not get database information! Exiting.. ");
            //System.exit(0);
        }

        //Not sure if more preferred way to start.
        TripEaseServer server = new TripEaseServer();
        server.start();
    }
}
