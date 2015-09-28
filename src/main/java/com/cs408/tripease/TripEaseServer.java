package com.cs408.tripease;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class TripEaseServer {

    private static final Logger log = LoggerFactory.getLogger(TripEaseServer.class);

    public static void main(String[] args) {
        int port;
        String hostname;

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

        System.out.println("TripEase server started at port:" + port + " on " + hostname);
        // Create an HTTP server which simply returns "Hello World!" to each request.
        Vertx.vertx().createHttpServer()
                .requestHandler(req -> req.response().end("CS408 : TripEase")).listen(port, hostname);
    }
}
