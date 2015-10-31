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
	String Location="";
	String FoodType="";
	String Budget="";
	String Length="";
	String[] Hotel = new String[10];
	int counter = 0;
	public void test(RoutingContext context){
		jdbcClient.getConnection(res -> {
				if(res.succeeded()) {
				SQLConnection connection = res.result();
				System.out.println("Current Location of Location before remove: "+Location);
				Location = Location.replaceAll("[^a-zA-Z]","");

				if(Location.equals("Miami")){Location = "Miami, FL";}
				if(Location.equals("Chicago")){Location = "Chicago, IL";}
				if(Location.equals("New York")){Location = "New York City, NY";}
				System.out.println("CUrrent value of locatoin: "+Location);

				connection.query("SELECT name FROM hotel WHERE location = '"+Location+"'", res2 -> {
					if(res2.succeeded()) {
					for (JsonArray line : res2.result().getResults()) {
					Hotel[counter] = line.encode();
					Hotel[counter] = Hotel[counter].replaceAll("[^a-zA-Z]","");
					counter++;
					System.out.println("Hotel: "+Hotel[counter-1]);
					
					}
					context.put("hotels",Hotel);
					context.next();

					}else{
					log.error("Could not select from the user table");
					}
					});
				}else{
					log.error("coould not connect to database below");
					//context.fail(402);
				}
		});


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

			///////////////////////////////////////////////////////////
			//get user location
			/////////////////////////////////////////////////////////
			jdbcClient.getConnection(res -> {
					if(res.succeeded()) {
					SQLConnection connection = res.result();
					connection.query("SELECT Location FROM preferences WHERE username = '"+username+"'", res2 -> {
						if(res2.succeeded()) {
						ResultSet resultSet = res2.result();
						for(JsonArray line : res2.result().getResults()){
						Location  = line.encode();
						Location = Location.replaceAll("[^a-zA-Z]","");
						System.out.println("userLocation:"+Location);
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
			//////////////////////////////////////////
			//get user budget
			//////////////////////////////////////////
			jdbcClient.getConnection(res -> {
					if(res.succeeded()) {
					SQLConnection connection = res.result();
					connection.query("SELECT budget FROM preferences WHERE username = '"+username+"'", res2 -> {
						if(res2.succeeded()) {
						ResultSet resultSet = res2.result();
						for(JsonArray line : res2.result().getResults()){
						Budget= line.encode();
						Budget = Budget.replaceAll("[^a-zA-Z]","");
						System.out.println("Budget: "+Budget);
						}

						}else{
						log.error("Could not select budget from pref table table");
						}
						});
					}else{
					log.error("coould not connect to database below");
					context.fail(402);
					}
					});
			///////////////////////////////////////////
			//get user foodType
			///////////////////////////////////////////
			jdbcClient.getConnection(res -> {
					if(res.succeeded()) {
					SQLConnection connection = res.result();
					connection.query("SELECT foodtype FROM preferences WHERE username = '"+username+"'", res2 -> {
						if(res2.succeeded()) {
						ResultSet resultSet = res2.result();
						for(JsonArray line : res2.result().getResults()){
						FoodType  = line.encode();
						FoodType = FoodType.replaceAll("[^a-zA-Z,]","");
						System.out.println("Food Type: "+FoodType);
						}

						}else{
						log.error("Could not select budget from pref table table");
						}
						});
					}else{
					log.error("coould not connect to database below");
					context.fail(402);
					}
					});
			/////////////////////////////////////////////
			//get length of stay
			/////////////////////////////////////////////
			jdbcClient.getConnection(res -> {
					if(res.succeeded()) {
					SQLConnection connection = res.result();
					connection.query("SELECT Length FROM preferences WHERE username = '"+username+"'", res2 -> {
						if(res2.succeeded()) {
						ResultSet resultSet = res2.result();
						for(JsonArray line : res2.result().getResults()){
						Length= line.encode();
						Length = Length.replaceAll("[^a-zA-Z]","");
						System.out.println("Length of stay: "+Length);
						}

						}else{
						log.error("Could not select budget from pref table table");
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
			test(context);
			System.out.println("WOW ITS A PRINT STATMENT");
		}
		private void doRedirect(HttpServerResponse response, String url) {
			response.putHeader("location", url).setStatusCode(302).end();
		}

		}
