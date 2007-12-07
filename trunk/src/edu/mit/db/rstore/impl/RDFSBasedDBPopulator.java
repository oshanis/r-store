package edu.mit.db.rstore.impl;

import java.sql.SQLException;
import java.util.*;

import edu.mit.db.rstore.*;

public class RDFSBasedDBPopulator implements DBPopulator 
{
	//Needs at least the schemas from the schema generator and the RDFStore to get the triples from
	private LinkedList<PropertyTable> schemas;
	private RDFStore store;
	
	private HashMap<PropertyTable, String> insertStatements = new HashMap<PropertyTable, String>();
	
	public RDFSBasedDBPopulator(LinkedList<PropertyTable> db_schemas, RDFStore rdfstore)
	{
		schemas = db_schemas;
		store = rdfstore;
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.db.rstore.impl.DBPopulaterInt#createTables()
	 */
	
	public void createTables() throws ClassNotFoundException, SQLException{
		
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
			if (p instanceof ManyToManyTable) {
				createTableStatement = ((ManyToManyTable) p).getSQL();				
			}
			else {
				createTableStatement = p.getSQL() ;				
			}
			dbConnection.st.execute(createTableStatement);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.mit.db.rstore.impl.DBPopulaterInt#insertValues()
	 */
	public void insertValues(){

		for (PropertyTable p: this.schemas){
			String statement = "INSERT INTO " + p.table_name + " VALUES ( ";
//			System.out.println(p.table_name);
			if (p instanceof ManyToManyTable) {
				ManyToManyTable m = (ManyToManyTable) p;
				LinkedList<String> l = m.getPrimaryKeys();
				for (int i=0; i<l.size(); i++){
					statement += "'" + l.get(i) + "'" + " , ";
//					System.out.print(l.get(i) + " ");
				}
			}
			else{
				statement += "'" +  p.getPrimaryKey() + "'" +  " , ";
//				System.out.print(p.getPrimaryKey() + " ");
			}
			
			LinkedList<String> l = p.getColTypes();
			
			for (int i=0; i<l.size(); i++){
				statement += "'" + l.get(i) + "'" + " ,";
			}
			
			statement = statement.substring(0, statement.lastIndexOf(','));
			statement += ")";
			System.out.println(statement);
		}

	}
	
}
