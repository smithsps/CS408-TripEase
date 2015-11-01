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
	String[] Rest = new String[10];
	String[] Act = new String[10];
	int Hotelcounter = 0;
	int Restcounter = 0;
	int Actcounter = 0;
	public void getHotels(RoutingContext context){
		jdbcClient.getConnection(res -> {
				if(res.succeeded()) {
					System.out.println("get hotels");
					SQLConnection connection = res.result();
					connection.query("SELECT name FROM hotel WHERE location = '" + Location + "'", res2 -> {
						if(res2.succeeded()) {
							for (JsonArray line : res2.result().getResults()) {
							Hotel[Hotelcounter] = line.encode();
							Hotel[Hotelcounter] = Hotel[Hotelcounter].replaceAll("[^a-zA-Z' ']","");
							Hotelcounter++;
							}
							Hotelcounter=0;
						context.put("hotels", Hotel);
						}else{
							log.error("Could not select from the user table");
						}
					});
					connection.query("SELECT price FROM hotel WHERE location = '" + Location + "'",res3 ->{
						if(res3.succeeded()){
							Hotelcounter=0;
							for(JsonArray line1 : res3.result().getResults()){
								String temp = Hotel[Hotelcounter];
								temp = temp.concat("  "+line1.encode());
								temp = temp.replaceAll("[^a-zA-Z,' '0-9]","");
								Hotel[Hotelcounter]=temp;
								System.out.println("hotel with price: " +Hotel[Hotelcounter]);
								Hotelcounter++;
								context.put("hotels",Hotel);

							}
							Hotelcounter=0;
						}else{
							log.error("could not select from user table above");
						}
					});
					connection.query("SELECT name FROM resturant WHERE location = '" + Location + "'", res4 ->{
						if(res4.succeeded()){
							for(JsonArray line2 : res4.result().getResults()){
								System.out.println("resturant: "+line2.encode());
								String Resttemp = line2.encode();
								Resttemp = Resttemp.replaceAll("[^a-zA-Z,' '0-9]","");
								Rest[Restcounter] = Resttemp;
								Restcounter++;
								}
								Restcounter=0;
						context.put("resturants",Rest);
						}else{
							log.error("could not select form resturant table");
						}
					});
					connection.query("SELECT name FROM activities WHERE location ='"+Location+"'", res5 ->{
						if(res5.succeeded()){
							for(JsonArray line3 : res5.result().getResults()){
								System.out.println("Activities: "+line3.encode());
								String ActTemp = line3.encode();
								ActTemp = ActTemp.replaceAll("[^a-zA-Z,' '0-9]","");
								Act[Actcounter]=ActTemp;
								Actcounter++;
							}
							Actcounter=0;
						context.put("activities",Act);
						context.next();
						}else{
							log.error("could not select form the activites table");
						}
					});

				} else {
					log.error("coould not connect to database below111");
					//context.fail(402);u
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
						Location = Location.replaceAll("[^a-zA-Z,' ']","");
						System.out.println("userLocation:"+Location);
						}
						context.put("location", Location);

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
			/*jdbcClient.getConnection(res -> {
					if(res.succeeded()) {
					SQLConnection connection = res.result();
					connection.query("SELECT budget FROM preferences WHERE username = '"+username+"'", res2 -> {
						if(res2.succeeded()) {
						ResultSet resultSet = res2.result();
						for(JsonArray line : res2.result().getResults()){
						Budget= line.encode();
						Budget = Budget.replaceAll("[^a-zA-Z,' ']","");
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
						FoodType = FoodType.replaceAll("[^a-zA-Z,' ']","");
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
						Length = Length.replaceAll("[^a-zA-Z,' ']","");
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
			//get trip details*/
			/////////////////////////////////////////////////////////////
			getHotels(context);
			System.out.println("WOW ITS A PRINT STATMENT");
		}
		private void doRedirect(HttpServerResponse response, String url) {
			response.putHeader("location", url).setStatusCode(302).end();
		}

		}
