package edu.mit.db.rstore.impl;

import java.sql.*;

import com.hp.hpl.jena.vocabulary.DB;

/**
 * This class creates Database connection to PostgresSQL 
 * (Helper class for DBPopulator)
 * @author oshani
 *
 */
public class DBConnection {
	
	public void connect() throws ClassNotFoundException, SQLException{
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql:rstoreDB";
		
		//Make sure that you have a user named postgres with the same password
		//otherwise it will not work!
		String username = "postgres";
		String password = "postgres";
		
		Connection conn = DriverManager.getConnection(url, username, password);
		
		Statement st = conn.createStatement();
//		ResultSet rs = st.executeQuery("CREATE TABLE base (subject varchar(40)," +
//														 " predicate varchar(40)," +
//														 " object varchar(40))");
		
//		ResultSet rs = st.executeQuery("INSERT INTO TABLE base (subject varchar(40)," +
//				 " predicate varchar(40)," +
//				 " object varchar(40))");


	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		DBConnection dbc = new DBConnection();
		dbc.connect();
	}
}
