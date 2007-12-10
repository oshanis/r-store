package edu.mit.db.rstore.impl;

import java.sql.*;

/**
 * This class creates Database connection to PostgresSQL 
 * (Helper class for DBPopulator)
 * @author oshani
 *
 */
public class DBConnection {
	
	public Connection conn;
	public Statement st;
	
	public void connect() throws ClassNotFoundException, SQLException{
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql:r-storeDB";
		
		//Make sure that you have a user named postgres with the same password
		//otherwise it will not work!
		String username = "postgres";
		String password = "postgres";
		
		conn = DriverManager.getConnection(url, username, password);
		
		st = conn.createStatement();

	}
	
	public boolean tableExists(String tableName) throws SQLException{
		PreparedStatement prepSt =  null;
		ResultSet results = null;
		try {
	        prepSt = conn.prepareStatement("SELECT COUNT(*) FROM " +
	              tableName + " WHERE 1 = 2");
	        results = prepSt.executeQuery();
	        return true;  // if table does exist, no rows will ever be returned
	   }
	   catch (SQLException e) {
	        return false;  // if table does not exist, an exception will be thrown
	   }
	   finally {
	        if (results != null) {
	              results.close();
	        }
	        if (prepSt != null) {
	        	prepSt.close();
	        }
	   }
		
	}

	/*
	 * FIXME There's a bug in this method
	 * doesn't do what it is supposed to do!
	 */
	public boolean tableHasNoRows(String tableName) throws SQLException{
		ResultSet results = null;
		try {
			results = st.executeQuery("SELECT * FROM " + tableName);
	        if (results.getRow() == 0){
		        return true;  
	        }
	        else{
	        	return false;
	        }
	   }
	   catch (SQLException e) {
	        return false;  // if table does not exist, an exception will be thrown
	   }
	   finally {
	        if (results != null) {
	              results.close();
	        }
	   }
		
	}

	public void close() throws SQLException{
		conn.close();
	}
	
	public void clear() throws SQLException{
		st.execute(" DROP SCHEMA public CASCADE ");
		st.execute(" CREATE SCHEMA public ");			
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		DBConnection dbc = new DBConnection();
		dbc.connect();
	}
}
