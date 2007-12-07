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
	
	public Connection conn;
	public Statement st;
	
	public void connect() throws ClassNotFoundException, SQLException{
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql:rstoreDB";
		
		//Make sure that you have a user named postgres with the same password
		//otherwise it will not work!
		String username = "postgres";
		String password = "postgres";
		
		conn = DriverManager.getConnection(url, username, password);
		
		st = conn.createStatement();
		
		String tableName = "base";
		
//		//First Check if the table exists in the Database
//		//If it exists drop the table
//		if (tableExists(tableName)){
//			st.execute("DROP TABLE base");
//		}
//		
//		st.execute( "CREATE TABLE base ("  +
//         "Entry      INTEGER      NOT NULL, "    +
//         "Customer   VARCHAR (20) NOT NULL, "    +
//         "DOW        VARCHAR (3)  NOT NULL, "    +
//         "Cups       INTEGER      NOT NULL, "    +
//         "Type       VARCHAR (10) NOT NULL,"     +
//         "PRIMARY KEY( Entry )"                  +
//                                            ")" );
		

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
