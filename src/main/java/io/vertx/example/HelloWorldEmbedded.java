package io.vertx.example;

import io.vertx.core.Vertx;

public class HelloWorldEmbedded {

  public static void main(String[] args) {
    int port;
    String hostname;
    try {
      port = Integer.parseInt(System.getenv("OPENSHIFT_VERTX_PORT"));
    } catch (Exception ex) {
      port = 8080;
    }
    
    try {
      hostname = System.getenv("OPENSHIFT_VERTX_IP");
    } catch (Exception ex) {
      hostname = "localhost";
    }


    // Create an HTTP server which simply returns "Hello World!" to each request.
    Vertx.vertx().createHttpServer().requestHandler(req -> req.response().end("Hello Brian!")).listen(port, hostname);
  }

}
