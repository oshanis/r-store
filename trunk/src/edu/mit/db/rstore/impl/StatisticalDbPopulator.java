package edu.mit.db.rstore.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import edu.mit.db.rstore.DBPopulator;
import edu.mit.db.rstore.RDFStore;

public class StatisticalDbPopulator implements DBPopulator {
	
	//Needs at least the schemas from the schema generator and the RDFStore to get the triples from
	private LinkedList<PropertyTable> schemas;
	private RDFStore store;
	
	private HashMap<PropertyTable, LinkedList<String>> insertStatements = new HashMap<PropertyTable, LinkedList<String>>();
	
	public StatisticalDbPopulator(LinkedList<PropertyTable> db_schemas, RDFStore rdfstore)
	{
		schemas = db_schemas;
		store = rdfstore;
	}

	public void createTables() throws ClassNotFoundException, SQLException {

		DBConnection dbConnection = new DBConnection();
		
		dbConnection.connect();
		
		for (PropertyTable p: this.schemas){
			
			String tableName = p.table_name;
			
			//First Check if the table exists in the Database
			//If it exists drop the table
			if (dbConnection.tableExists(tableName)){
				dbConnection.st.execute("DROP TABLE "+ tableName);
			}
			
			//Then create the new table
			String createTableStatement = "";
			createTableStatement = p.getSQL() ;				
			dbConnection.st.execute(createTableStatement);
		}

	}

	public void insertValues() {
		// TODO Auto-generated method stub

	}

}
