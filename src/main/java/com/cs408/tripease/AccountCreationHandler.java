package com.cs408.tripease;


import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.*;

public class AccountCreationHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(AccountCreationHandler.class);


    private String usernameParam = "username";
    private String passwordParam = "password";
    private String passwordConfirmParam = "passwordConfirm";
    private String emailParam = "email";
    private String emailConfirmParam = "emailConfirm";
    private String redirectURL = "/login";

    private JDBCClient jdbcClient;

    public static AccountCreationHandler create(JDBCClient jc) {
        AccountCreationHandler ach = new AccountCreationHandler();
        ach.jdbcClient = jc;
        return ach;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
        } else {
            if (!req.isExpectMultipart()) {
                throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
            }
            MultiMap params = req.formAttributes();
            String username = params.get(usernameParam);
            String password = params.get(passwordParam);
            final String passwordConfirm = params.get(passwordConfirmParam);
            String email = params.get(emailParam);
            String emailConfirm = params.get(emailConfirmParam);
            if (username == null || password == null || passwordConfirm == null ||
                    email == null || emailConfirm == null) {
                log.warn("Improper parameters inputted.");
                context.fail(400);
            } else {
                //Add error checking for params here

                jdbcClient.getConnection(res -> {
                    if (res.succeeded()) {
                        SQLConnection connection = res.result();
                        
                        connection.execute("INSERT INTO user VALUES ('" + username + "', '" + password + "', '" + passwordConfirm + "', '" + email + "')", res2 -> {
                            if (res2.succeeded()) {
                                doRedirect(req.response(), redirectURL);
                            } else {
                                log.error("Could not create the user account in the database.");
                            }

                        });
                    } else {
                        log.error("Could not connect to database.");
                        context.fail(402);
                    }
                });
            }
        }
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader("location", url).setStatusCode(302).end();
    }
}