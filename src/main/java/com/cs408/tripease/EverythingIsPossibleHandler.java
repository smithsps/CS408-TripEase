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
import io.vertx.ext.sql.ResultSet.*;
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


public class EverythingIsPossibleHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(EverythingIsPossibleHandler.class);

    private JDBCClient jdbcClient;

    public static EverythingIsPossibleHandler create(JDBCClient jc) {
        EverythingIsPossibleHandler ach = new EverythingIsPossibleHandler();
        ach.jdbcClient = jc;
        return ach;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        //if (req.method() != HttpMethod.POST) {
            //context.fail(405); // Must be a POST
        //} else {
            //if (!req.isExpectMultipart()) {
            //   throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler11?");
            //}
            MultiMap params = req.formAttributes();
            String username = context.user().principal().getString("username");

	    String Location ="";
	    String FoodType ="";
	    String Budget="";
	    String Length="";

	    ///////////////////////////////////////////////////////////
	    //get user prefrences
	    /////////////////////////////////////////////////////////
		jdbcClient.getConnection(res -> {
		if(res.succeeded()) {
			SQLConnection connection = res.result();
			connection.query("SELECT * FROM hotel", res2 -> {
				if(res2.succeeded()) {
					ResultSet resultSet = res2.result();
					for(JsonArray line : res2.result().getResults()){
						String temp  = line.encode();
						System.out.println("temp: "+temp);
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


	///////////////////////////////////////////////////////////////
	//get trip details
	/////////////////////////////////////////////////////////////
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
	context.next();
	}
    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader("location", url).setStatusCode(302).end();
    }

}
