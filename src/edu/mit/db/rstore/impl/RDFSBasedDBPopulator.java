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
import com.hp.hpl.jena.rdf.model.NsIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


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
	public void insertValues() throws ClassNotFoundException, SQLException{

		DBConnection dbConnection = new DBConnection();
		dbConnection.connect();

		for (PropertyTable p: this.schemas){
			if (p instanceof ManyToManyTable){
				
			}
			else{
				String pkeyCol = p.getPrimaryKeyColumn();
				String pkeyVal = p.getPrimaryKey();
				String pkeyValLocalName = pkeyVal.substring(pkeyVal.indexOf('#')+1);
				HashSet<String> pkeys = store.getQualifiedSubjectsFromType(pkeyValLocalName);
				for (String s: pkeys){
					//For each of these primary keys Query over the RDF store for each of the columns
					String insertStatement = " INSERT INTO "+p.table_name+ " VALUES ( '" + s + "' , '";
					HashMap<String, String> cols = p.columns;
					Iterator<String> i = cols.keySet().iterator();
					while (i.hasNext()){
						String colVal = (String)i.next();
						String colName = cols.get(colVal);
//						System.out.println(s+ "  " + colName+ "   "+ colVal);
						String attrVal = getOneToOneAttributeValue(colVal, s);
						if (attrVal != null){
							insertStatement += attrVal + "' , '";
						}
						else{
							insertStatement += "null" + "' , '";
						}
					}
					//Strip off the final ',' and add the ')'
					insertStatement = insertStatement.substring(0, insertStatement.lastIndexOf(','));
					insertStatement += ")";
					System.out.println(insertStatement);
					dbConnection.st.execute(insertStatement);
				}
				
			}
		}
		
	}
	
	public String getOneToOneAttributeValue(String pred, String sub){
		
		//Using the SPARQL template
//		"SELECT ?variable " +
//		"WHERE {" +
//		"      c:MIT6.830 :students ?v ." +
//		"	  ?v  ?x ?variable ." +
//		"      }";

		//A temporary hack to the problem in the the fully qualified name
		//for the predicate
    	NsIterator i = store.getRDFModel().listNameSpaces();
    	while (i.hasNext()){
    		String newPred = (String)i.next() + pred;

    		String queryString = 
    			"SELECT ?variable " +
    			"WHERE {" +
    			"<" +sub + "> <" + newPred + "> "+ " ?variable .}";
    		
    		Query query = QueryFactory.create(queryString);

    		// Execute the query and obtain results
    		QueryExecution qe = QueryExecutionFactory.create(query, store.getRDFModel());
    		ResultSet results = qe.execSelect();

    		Model resultModel =	ResultSetFormatter.toModel(results);
    		
    		StmtIterator iter = resultModel.listStatements();
    		while (iter.hasNext()){
    			Statement statement = iter.nextStatement();
    			//I believe this should be the standard way the final value will be encoded
    			String valueExpected = "http://www.w3.org/2001/sw/DataAccess/tests/result-set#value";
    			if (statement.getPredicate().toString().equals(valueExpected)){
    				return statement.getObject().toString();
    			}
    		}
     		qe.close();

    	}

		return null;
	}
	
}
