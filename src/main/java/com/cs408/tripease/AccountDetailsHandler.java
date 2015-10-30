package com.cs408.tripease;


import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.String;
import java.util.Random;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class AccountDetailsHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(AccountDetailsHandler.class);


    private String NameParam = "FullName";
    private String AgeParam = "Age";
    private String Genderparam = "Gender";
    private String redirectURL = "/login";

    private JDBCClient jdbcClient;

    public static AccountDetailsHandler create(JDBCClient jc) {
        AccountDetailsHandler ach = new AccountDetailsHandler();
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
            String username = context.user().principal().getString("username");
	    String email = "Email@email.com";
            //String password = params.get(passwordParam);
	    String FullName = params.get(NameParam);
	    String Age = params.get(AgeParam);
	    int Age1 = Integer.parseInt(Age);
	    String Gender = params.get(Genderparam);
            if (FullName == null|| Age == null || Gender == null) {
                log.warn("Improper parameters inputted here.");
                context.fail(404);
            } else {

                //Add error checking for params here
                
                  
                jdbcClient.getConnection(res -> {
                    if (res.succeeded()) {
                        SQLConnection connection = res.result();
                        
                        connection.execute("INSERT INTO userDetails VALUES ('" + username + "', '" + email + "', '" + FullName + "', 21 , '" + Gender + "')", res2 -> {
                            if (res2.succeeded()) {
                                doRedirect(req.response(), redirectURL);
                            } else {
                                log.error("Could not edit account details in the database.");
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
