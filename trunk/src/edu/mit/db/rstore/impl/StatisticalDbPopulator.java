package edu.mit.db.rstore.impl;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

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

	/**
	 * I wrote this stub many days ago, before there was enough information to implement.  But, here is the general idea:
	 * 
	 * First execute all the SQL create statements in the schemas
	 * 
	 * Iterate over the store.  For each statement
	 * 	1.  Identify the predicate, which Sergio pointed out is completely specified by predicate name, subject type, and object type
	 * (all of which I think can currently be gotten from RDFStore).  This information will indicate which PropertyTable in the schema
	 * it belongs to.
	 * 
	 * 2.  Insert according to the following rule:
	 * if(predicate is in forward direction)
	 * 		result set <- update <table> where <pkey> = stmt.subject, <col> <- stmt.obj
	 * 		if( |result set| == 0 )
	 * 			insert into <table> (pkey, <col>) values (stmt.subject, stmt.object)
	 * else
	 * 		do the same thing, but reversing the subject and the object
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */

	public void insertValues() {
		
		StmtIterator iter = store.getIterator();
		
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();// get next statement
            Resource  subject   = stmt.getSubject();   // get the subject
            Property  predicate = stmt.getPredicate(); // get the predicate
            RDFNode   object    = stmt.getObject();    // get the object
        
//			String objectType = "";
//			
//			if(object.isLiteral())
//				objectType = Store.LITERAL;
//			else
//				if(object.isURIResource())
//					objectType = store.getTypeFromSubjects(((Resource)object).getLocalName());
//				else
//				{
//					System.out.println("Found an object that was neither a literal or a URI");
//					objectType = "";
//				}
            
//!predicate.getLocalName().equals(null) && !subject.getLocalName().equals(null) && !object.toString().equals(null)
            
			if (!subject.equals(null) && !predicate.equals(null) && !object.equals(null) ){
	            PredicateRule p = new PredicateRule(predicate.getLocalName(), 
	            					store.getTypeFromSubjects(subject.getLocalName()), 
	            					object.toString(), 
	            					PredicateRule.Direction.FORWARD);
				
	            String tableName = store.getTypeFromSubjects(subject.getLocalName());
				
				//Need to get the column mapping for this predicate rule
				StatisticalSchemaGenerator schemaGen = new StatisticalSchemaGenerator(store);
				
				LinkedList<PropertyTable> propertyTables = schemaGen.getSchema();
				
				for (int i = 0; i<propertyTables.size(); i++){
					PropertyTable propTable = propertyTables.get(i);				
					HashMap<PredicateRule, String> cols = propTable.getMap();
					if (cols.containsKey(p)){
						System.out.println("*****FOUND*****");
						System.out.println(cols.get(p));
					}
					else{
						System.out.println("NOT FOUND");
					}
				}
			}
        }     
	}

}
