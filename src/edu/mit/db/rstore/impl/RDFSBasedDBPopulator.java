package edu.mit.db.rstore.impl;

import java.sql.SQLException;
import java.util.*;

import edu.mit.db.rstore.*;

public class RDFSBasedDBPopulator implements DBPopulator 
{
	//Needs at least the schemas from the schema generator and the RDFStore to get the triples from
	private LinkedList<PropertyTable> schemas;
	private RDFStore store;
	
	private HashMap<PropertyTable, LinkedList<String>> insertStatements = new HashMap<PropertyTable, LinkedList<String>>();
	
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

		//Do the inserts for each table
		for (PropertyTable p: this.schemas){
//			System.out.println(p.table_name);
			if (p instanceof ManyToManyTable) {
				ManyToManyTable m = (ManyToManyTable) p;
				LinkedList<String> l = m.getPrimaryKeys();
				//Each primary key value will have one insert statement each in the respective table
				for (int i=0; i<l.size(); i++){
					HashSet<String> primaryKeys = store.getSubjectsFromType(l.get(i));
			    	Iterator primaryKeyIterator = primaryKeys.iterator();
			    	while (primaryKeyIterator.hasNext()){
						String statement = "INSERT INTO " + p.table_name + " VALUES ( ";
						statement += "'" + (String)primaryKeyIterator.next() + "'" + " , ";
						LinkedList<String> colTypes = m.getColTypes();
						for (int j=0; j<colTypes.size(); j++){
							HashSet<String> colVals = store.getSubjectsFromType(colTypes.get(j));
					    	if (colVals.size()>0){
						    	Iterator colIterator = primaryKeys.iterator();
						    	while (colIterator.hasNext()){
									statement += "'" + (String)colIterator.next() + "'" + " ,";
						    	}

					    	}
					    	//TODO Do the getSubjectsFromPredicate
					    	
							
						}
						statement = statement.substring(0, statement.lastIndexOf(','));
						statement += ")";
						System.out.println(statement);
					}
		    	}
			}
			else{
//				statement += "'" +  p.getPrimaryKey() + "'" +  " , ";
			}
			
//			LinkedList<String> l = p.getColTypes();
//			
//			for (int i=0; i<l.size(); i++){
//				statement += "'" + l.get(i) + "'" + " ,";
//			}
			
		}

	}
	
}
