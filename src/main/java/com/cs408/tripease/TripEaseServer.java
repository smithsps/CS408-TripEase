package com.cs408.tripease;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.AbstractVerticle;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;




public class TripEaseServer extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(TripEaseServer.class);

    private static int port;
    private static String hostname;

    private static JsonObject jdbcConfig = new JsonObject();

    @Override
    public void start() {
        Vertx vertx = Vertx.vertx();

        HttpServer server = Vertx.vertx().createHttpServer();
        Router router = Router.router(vertx);

        //Assets routing (Redundant atm with the above route)
        router.routeWithRegex(".+(fonts|css|images|js).*")
              .handler(StaticHandler.create());

       //User Authorizations
        JDBCClient jdbcClient = JDBCClient.createShared(vertx, jdbcConfig);
        JDBCAuth authProvider = JDBCAuth.create(jdbcClient);


        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(UserSessionHandler.create(authProvider));

        router.route("/planner/*").handler(RedirectAuthHandler.create(authProvider, "/login"));
        router.post("/login").handler(FormLoginHandler.create(authProvider));
        router.route("/login").handler(routingContext -> {
            routingContext.response().sendFile("webroot/login.html");
        });

        //Main Page
        router.route().handler(StaticHandler.create().setIncludeHidden(false).setCachingEnabled(false));

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

        int dbPort;
        String dbHostname, dbUsername, dbPassword;
        try {
            dbHostname = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
            dbPort = Integer.parseInt(System.getenv("OPENSHIFT_MYSQL_DB_PORT"));
            dbUsername = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
            dbPassword = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
        } catch (NullPointerException | NumberFormatException ex) {
            TripEaseServer.log.fatal("Could not get database information! Trying local.. ");

            dbPort = 3306;
            dbHostname = "localhost";
            dbUsername = "u";
            dbPassword = "p";
        }
        jdbcConfig.put("url", dbHostname);
        jdbcConfig.put("port", dbPort);
        jdbcConfig.put("username", dbUsername);
        jdbcConfig.put("password", dbPassword);


        //Not sure if more preferred way to start.
        TripEaseServer server = new TripEaseServer();
        server.start();
    }
}
