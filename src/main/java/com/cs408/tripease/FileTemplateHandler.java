package com.cs408.tripease;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.impl.Utils;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.Handler;
import io.vertx.ext.web.impl.Utils;

public class FileTemplateHandler implements Handler<RoutingContext>{

    public TemplateEngine engine;
    public String filename;
    public String contentType;

    public static FileTemplateHandler create(TemplateEngine engine, String filename) {
        FileTemplateHandler fth = new FileTemplateHandler();
        fth.engine = engine;
        fth.filename = filename;
        fth.contentType = "text/html";
        return fth;
    }

    @Override
    public void handle(RoutingContext context) {
        String file = filename;
        engine.render(context, file, res -> {
            if (res.succeeded()) {
                context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType).end(res.result());
            } else {
                context.fail(res.cause());
            }
        });
    }
}