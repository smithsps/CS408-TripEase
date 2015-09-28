package com.cs408.tripease;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class TripEaseServer extends AbstractVerticle{

  public static void main(String[] args) {
    int port;
    String hostname;

    try {
      port = Integer.parseInt(System.getenv("OPENSHIFT_VERTX_PORT"));
    } catch (NullPointerException ex) {
      port = 8080;
    }
    
    try {
      hostname = System.getenv("OPENSHIFT_VERTX_IP");
    } catch (NullPointerException nullex) {
      hostname = "localhost";
    }

    // Create an HTTP server which simply returns "Hello World!" to each request.
    Vertx.vertx().createHttpServer()
            .requestHandler(req -> req.response().end("CS408 : TripEase")).listen(port, hostname);
  }
}
