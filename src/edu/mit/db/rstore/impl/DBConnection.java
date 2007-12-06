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
	
	private Connection conn;
	
	public void connect() throws ClassNotFoundException, SQLException{
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql:rstoreDB";
		
		//Make sure that you have a user named postgres with the same password
		//otherwise it will not work!
		String username = "postgres";
		String password = "postgres";
		
		conn = DriverManager.getConnection(url, username, password);
		
		Statement st = conn.createStatement();
		
		String tableName = "base";
		
		//First Check if the table exists in the Database
		//If it exists drop the table
		if (tableExists(tableName)){
			st.execute("DROP TABLE base");
		}
		
		//Create the new table
		st.execute("CREATE TABLE base ((subject varchar," +
									 " predicate varchar," +
									 " object varchar," +
									 "PRIMARY KEY( subject, object ))");
		
//		ResultSet rs = st.executeQuery("INSERT INTO TABLE base (subject varchar(40)," +
//				 " predicate varchar(40)," +
//				 " object varchar(40))");


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

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		DBConnection dbc = new DBConnection();
		dbc.connect();
	}
}
