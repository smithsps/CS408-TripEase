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
import io.vertx.core.AsyncResult;

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
	String[] Rest = new String[10];
	String[] Act = new String[10];
	int Hotelcounter = 0;
	int Restcounter = 0;
	int Actcounter = 0;
	String username = "";


	@Override
	public void handle(RoutingContext context) {

		username = context.user().principal().getString("username");

		jdbcClient.getConnection(connectionRes -> {
			if (connectionRes.succeeded()) {
				System.out.println("Able to get JDBC Connection");
				queryLocation(context, connectionRes);

			} else {
				log.error("Could not connect to the database.");
				context.fail(402);
			}
		});
	}

	private void queryLocation(RoutingContext context, AsyncResult<SQLConnection> connectionRes) {
		// Get and set locations of user for future queries
		SQLConnection connection = connectionRes.result();
		System.out.println("SELECT Location FROM preferences WHERE username = '"+username+"'");
		connection.query("SELECT Location FROM preferences WHERE username = '"+username+"'", res2 -> {
			if(res2.succeeded()) {
				System.out.println("Able to get query location");
				ResultSet resultSet = res2.result();
				for(JsonArray line : res2.result().getResults()){
					Location  = line.encode();
					Location = Location.replaceAll("[^a-zA-Z,' ']","");
					System.out.println("userLocation:"+Location);
				}
				context.session().put("location", Location);
				queryBudget(context, connection);


			}else{
				log.error("Could not select from the user table");
			}
		});
	}
	private void queryBudget(RoutingContext context, SQLConnection connection) {
		connection.query("SELECT budget FROM preferences WHERE username = '"+username+"'", res2 -> {
			if(res2.succeeded()) {
				System.out.println("Able to get budget query");
				ResultSet resultSet = res2.result();
				for(JsonArray line : res2.result().getResults()){
					Budget= line.encode();
					Budget = Budget.replaceAll("[^a-zA-Z,' ']","");
					System.out.println("Budget: "+Budget);
				}
				queryHotels(context, connection);
			}else{
				log.error("Could not select budget from pref table table");
			}
		});
	}
	private void queryHotels(RoutingContext context, SQLConnection connection) {
		// Retrieve Hotels
		connection.query("SELECT name FROM hotel WHERE location = '" + Location + "'", res2 -> {
			if (res2.succeeded()) {
				System.out.println("Able to get hotel query");
				for (JsonArray line : res2.result().getResults()) {
					Hotel[Hotelcounter] = line.encode();
					Hotel[Hotelcounter] = Hotel[Hotelcounter].replaceAll("[^a-zA-Z' ']", "");
					Hotelcounter++;
				}
				Hotelcounter = 0;
				queryHotelPricing(context, connection);
			} else {
				log.error("Could not select from the user table");
			}
		});
	}
	private void queryHotelPricing(RoutingContext context, SQLConnection connection) {
		// Retrieve Hotel Pricing
		connection.query("SELECT price FROM hotel WHERE location = '" + Location + "'", res3 -> {
			if (res3.succeeded()) {
				System.out.println("Able to get hotel pricing");
				Hotelcounter = 0;
				for (JsonArray line1 : res3.result().getResults()) {
					String temp = Hotel[Hotelcounter];
					temp = temp.concat("   ($" + line1.encode()+")");
					temp = temp.replaceAll("[^a-zA-Z,' '0-9$()]", "");
					Hotel[Hotelcounter] = temp;
					System.out.println("hotel with price: " + Hotel[Hotelcounter]);
					Hotelcounter++;
				}
				context.session().put("hotels", Hotel);
				queryResturants(context, connection);
				Hotelcounter = 0;
			} else {
				log.error("could not select from user table above");
			}
		});
	}
	private void queryResturants(final RoutingContext context, final SQLConnection connection) {
		// Retrieve Resturants
		connection.query("SELECT name FROM resturant WHERE location = '" + Location + "'", res4 ->{
			if(res4.succeeded()){
				System.out.println("Able to get resturant query");
				for(JsonArray line2 : res4.result().getResults()){
					System.out.println("resturant: "+line2.encode());
					String Resttemp = line2.encode();
					Resttemp = Resttemp.replaceAll("[^a-zA-Z,' '0-9]", "");
					Rest[Restcounter] = Resttemp;
					Restcounter++;
				}
				Restcounter=0;
				context.session().put("resturants", Rest);

				queryActivites(context, connection);
			}else{
				log.error("could not select form resturant table");
			}
		});
	}
	private void queryActivites(RoutingContext context, SQLConnection connection) {
		// Retrieve Activies
		connection.query("SELECT name FROM activities WHERE location ='"+Location+"'", res5 ->{
			if(res5.succeeded()){
				System.out.println("Able to get activities query");
				for(JsonArray line3 : res5.result().getResults()){
					System.out.println("Activities: "+line3.encode());
					String ActTemp = line3.encode();
					ActTemp = ActTemp.replaceAll("[^a-zA-Z,' '0-9]", "");
					Act[Actcounter]=ActTemp;
					Actcounter++;
				}
				Actcounter=0;
				context.session().put("activities",Act);

				context.next();
			}else{
				log.error("could not select form the activites table");
			}
		});
	}
}
