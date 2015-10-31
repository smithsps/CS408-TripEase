package com.cs408.tripease;


import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
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
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.String;
import java.util.Random;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;



public class AccountPreferencesHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(AccountPreferencesHandler.class);


    private String LocationParam = "Location";
    private String FoodTypeParam = "FoodType";
    private String BudgetParam = "Budget";
    private String LengthParam = "LengthofStay";
    private String redirectURL = "/login";

    private JDBCClient jdbcClient;

    public static AccountPreferencesHandler create(JDBCClient jc) {
        AccountPreferencesHandler ach = new AccountPreferencesHandler();
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
	  
	  
	    String Location = params.get(LocationParam);
	    String FoodType = params.get(FoodTypeParam);
	    String Budget = params.get(BudgetParam);
	    String Length = params.get(LengthParam);



            if (Location == null|| FoodType == null || Budget == null || Length == null) {
                log.warn("Improper parameters inputted in preferences.");
                context.fail(404);
            } else {
		    if(Budget.length()>11 || Length.length() >11){
			    context.fail(400);
		    }
   
                  
                jdbcClient.getConnection(res -> {
                    if (res.succeeded()) {
                        SQLConnection connection = res.result();
                        
                        connection.execute("INSERT INTO preferences VALUES ('" + username + "', '" + FoodType + "', '" + Budget + "' , '" + Location + "','" + Length + "')", res2 -> {
                            if (res2.succeeded()) {
                                doRedirect(req.response(), redirectURL);
                            } else {
				    context.fail(400);
                                log.error("Could not edit account prefrencres in the database.");
                            }

                        });
                    } else {
                        log.error("Could not connect to database.");
                        context.fail(402);
                    }
                });

		/////////////////////////////////////////////////////
		jdbcClient.getConnection(res -> {

		if(res.succeeded()) {
			SQLConnection connection = res.result();

			connection.query("SELECT username FROM user", res2 -> {
				if(res2.succeeded()) {
					for (JsonArray line : res2.result().getResults()) {
              					System.out.println(line.encode());
					}

				}else{
					log.error("Could not select from the user table");
				}
			});
			}else{
				log.error("coould not connect to database below");
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
