package com.cs408.tripease;


import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonArray;
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
    private String redirectURL = "/userpreferences";
    boolean there = false;
    String Age="";
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
	    //String email = "Email@email.com";
            //String password = params.get(passwordParam);
	    String FullName = params.get(NameParam);
	    Age = params.get(AgeParam);
	    Age = Age.replaceAll("[^0-9]","");
	    //int Age1 = Integer.parseInt(Age);
	    String Gender = params.get(Genderparam);
            if (FullName == null|| Age == null || Gender == null) {
                log.warn("Improper parameters inputted here.");
                context.fail(404);
            } else {

                /*if(FullName.equals("")) {
                    log.warn("Fullname is not valid");
                    context.session().put("errorUserDetails", "Fullname is not valid.");
                    doRedirect(req.response(), "userdetails");
                    return;
                }

                if(FullName.length() > 200) {
                    log.warn("Fullname is too long.");
                    context.session().put("errorUserDetails", "Fullname is invalid (Too Long).");
                    doRedirect(req.response(), "userdetails");
                    return;
                }*/


                /*if(Age.equals("")) {
                    log.warn("Age is not valid");
                    context.session().put("errorUserDetails", "Age was left empty.");
                    doRedirect(req.response(), "userdetails");
                    return;
                }

                if(Gender.equals("")) {
                    log.warn("Gender field is invalid");
                    context.session().put("errorUserDetails", "Gender field was left blank.");
                    doRedirect(req.response(), "userdetails");
                    return;
                }

                if(Gender.length() > 50) {
                    log.warn("Gender is invalid (Too Long).");
                    context.session().put("errorUserDetails", "Gender is invalid (Too Long).");
                    doRedirect(req.response(), "userdetails");
                    return;
                }*/

                /*try {
                    Integer age = Integer.parseInt(Age);
                    if (age > 150) {
                        log.warn("You're not that old!");
                        context.session().put("errorUserDetails", "You're not that old!");
                        doRedirect(req.response(), "userdetails");
                        return;
                    }
                    if (age <= 5) {
                        log.warn("You're not that young!");
                        context.session().put("errorUserDetails", "You're not that young!");
                        doRedirect(req.response(), "userdetails");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    log.warn("Age is not valid number.");
                    context.session().put("errorUserDetails", "Age is not a valid number.");
                    doRedirect(req.response(), "userdetails");
                    return;
                }*/

                //Add gender field value testing.
                  
                jdbcClient.getConnection(res -> {
                    if (res.succeeded()) {
                        SQLConnection connection = res.result();
			connection.query("SELECT * FROM userDetails WHERE username = '"+username+"'", res3 -> {
			if(res3.succeeded()){
				for(JsonArray line : res3.result().getResults()){
					there = true; //account already exists need update not insert
				}
				if(!there){
					connection.execute("INSERT INTO userDetails VALUES ('"+username+"','"+FullName+"','"+Age+"','"+Gender+"')", res2 -> {
						if(res2.succeeded()){
							doRedirect(req.response(),redirectURL);
						}else{
							context.fail(400);
							log.error("could not insert into the detials table");
						}
					});
				}else{
					//update
					String update = "UPDATE userDetails SET fullname = '"+FullName+"', age = '"+Age+"', gender = '"+Gender+"' WHERE username = '"+username+"'";
					connection.update(update,res4 ->{
						if(res4.succeeded()){
							doRedirect(req.response(), redirectURL);
						}else{
							log.error("could not update user detials");
							context.fail(400);
						}
					});
				}
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
