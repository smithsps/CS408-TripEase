package com.cs408.tripease;
import java.sql.*;

public class jdbctest {

	public static void main(String[] args){
		
		//registered driver name (change accordingly)
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";
		//database URL (change accordingly)
		String DB_URL = "jdbc:mysql://localhost/EMP";
		
		String USER = "username";
		String PASSWORD = "password";
		
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(DB_URL, USER, PASSWORD);
			PreparedStatement ps; 
			//Sample sql query, can write as many as we want accrding to our needs
			ps = con.prepareStatement("INSERT INTO Locations(Name) values(?)");
			ps.setString(1, "Hawaii");
			ps.executeUpdate();
			
			ps.close();
			con.close();
		}catch (SQLException se){
			se.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		
			
	}
		
}
