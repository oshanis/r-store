package edu.mit.db.rstore.impl;

import java.sql.SQLException;
import java.util.*;

import edu.mit.db.rstore.*;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * This class populates the schema generated by the {@link RDFSBasedSchemaGenerator} from the {@link Store}
 * 
 * First it iterates over the tables generated, and based on the primary key(s), gets the relevant attributes
 * through SPARQL queries over the RDF Store
 * 
 * @author oshani
 *
 */
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

		System.out.println("**** Inside insertValues ****");
		for (PropertyTable p: this.schemas){
			if (p instanceof ManyToManyTable){
				
			}
			else{
				String pkeyCol = p.getPrimaryKeyColumn();
				String pkeyVal = p.getPrimaryKey();
				String pkeyValLocalName = pkeyVal.substring(pkeyVal.indexOf('#')+1);
				HashSet<String> pkeys = store.getSubjectsFromType(pkeyValLocalName);
				for (String s: pkeys){
					System.out.println(s);
					//For each of these primary keys Query over the RDF store for each of the columns
					HashMap<String, String> cols = p.columns;
					Iterator<String> i = cols.keySet().iterator();
					while (i.hasNext()){
						String colName = (String)i.next();
						String colVal = cols.get(colName);
						System.out.println( colName+ "   "+ colVal);
					}
				}
				
			}
		}
		
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
	
	public String getOneToOneAttributeValue(String pred, String sub){
		String queryString = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX c: <http://www.db.csail.mit.edu/6.830/>" +
			"PREFIX : <http://www.db.csail.mit.edu/6.830/course_schema.rdf#>" +
			"SELECT ?variable " +
			"WHERE {" +
			"      c:MIT6.830 :students ?v ." +
			"	  ?v  ?x ?variable ." +
			"      }";


		
		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, store.getRDFModel());
		ResultSet results = qe.execSelect();

		// Output query results	
		ResultSetFormatter.out(System.out, results, query);

		// Important - free up resources used running the query
		qe.close();

		String ret = "";
		
		return ret;
		
	}
	
}
