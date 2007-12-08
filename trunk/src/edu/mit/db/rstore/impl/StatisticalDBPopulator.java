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

public class StatisticalDBPopulator implements DBPopulator {
	
	//Needs at least the schemas from the schema generator and the RDFStore to get the triples from
	private LinkedList<PropertyTable> schemas;
	private RDFStore store;
	private DBConnection dbConnection;
	
	public StatisticalDBPopulator(LinkedList<PropertyTable> db_schemas, RDFStore rdfstore) throws ClassNotFoundException, SQLException
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
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws ClassNotFoundException 
	 */

	public void insertValues() throws SQLException, ClassNotFoundException {

		DBConnection dbConnection = new DBConnection();
		dbConnection.connect();

		StmtIterator iter = store.getIterator();
		
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();// get next statement
            Resource  subject   = stmt.getSubject();   // get the subject
            Property  predicate = stmt.getPredicate(); // get the predicate
            RDFNode   object    = stmt.getObject();    // get the object
        
			if (!subject.equals(null) && !predicate.equals(null) && !object.equals(null) )
			{
				/*
				 * The subject could be a blank node.  These statements need to be treated specially.
				 * The object could be a literal, a blank node, or a URI.  
				 * The predicate needs toString().  Sorry, I thought you were talking about the subject on the phone.
				 * 
				 * Statements that need special treatment:  Ones with blank nodes
				 * Statements thats should be ignored:  Ones with #type predicate
				 * 
				 * Note modifications:
				 */
				
				//Filter blank nodes out
				if(!subject.isAnon() && !object.isAnon())
				{
					String object_type;
					if(object.isLiteral())
						object_type = Store.LITERAL;
					else
						object_type = store.getTypeFromSubjects(((Resource)object).getLocalName());
					
					//Kill off the type predicate
					if(object_type != null)
					{
						PredicateRule p = new PredicateRule(predicate.toString(), 
	        					store.getTypeFromSubjects(subject.getLocalName()), 
	        					object_type, 
	        					PredicateRule.Direction.FORWARD);
						
						//Need to get the column mapping for this predicate rule
						StatisticalSchemaGenerator schemaGen = new StatisticalSchemaGenerator(store);
						
						LinkedList<PropertyTable> propertyTables = schemaGen.getSchema();
						
						for (int i = 0; i<propertyTables.size(); i++)
						{
							PropertyTable propTable = propertyTables.get(i);				
							HashMap<PredicateRule, String> cols = propTable.getMap();
							if (cols.containsKey(p))
							{
								
								String tableName = propTable.table_name;
								String pkeyCol = propTable.getPrimaryKeyColumn();
								String pkeyVal = subject.getLocalName();
								String attrCol = cols.get(p);
								String attrVal = object.toString();
								
								String updateStatement = " UPDATE " + tableName+
												" SET " + attrCol + " = '" + attrVal + "' " +
												" WHERE "+ pkeyCol +" = '" + pkeyVal + "' ";
								
								int success = dbConnection.st.executeUpdate(updateStatement);
								if (success == 0){
									String insertStatement = " INSERT INTO " + tableName +
									" ("+ pkeyCol + " , " + attrCol + ")" +
									" VALUES ( '" + pkeyVal +"' , '"+ attrVal + "' ) ";
									success = dbConnection.st.executeUpdate(insertStatement);
								}
							}
						}
					}
					
				}
				else
				{
					System.out.println("This statement has a blank node in it");
					HashMap<String, String> bnode_to_subject = new HashMap<String, String>();
					HashMap<String, String> bnode_to_subj_pred = new HashMap<String, String>();
					HashMap<String, HashMap<String, Integer>> bnode_obj_ct = new HashMap<String, HashMap<String, Integer>>();

					//Case:  S -P- [blank]
					if(object.isAnon())
					{
						bnode_to_subject.put(object.toString(), store.getTypeFromSubjects(subject.getLocalName()));
						bnode_to_subj_pred.put(object.toString(), predicate.toString());
					}
					//Case:  [blank] -P- O
					if(subject.isAnon())
					{
						HashMap<String, Integer> objs = bnode_obj_ct.get(subject.toString());
						if(objs == null)
						{
							objs = new HashMap<String, Integer>();
							bnode_obj_ct.put(subject.toString(), objs);
						}
						
						String key;
						
						if(object.isLiteral())
							key = Store.LITERAL;
						else
							if(object.isURIResource())
								key = store.getTypeFromSubjects(((Resource)object).getLocalName());
							else
							{
								System.out.println("Found a statement in a blank node whose object was not literal or uri:  ");
								printStatement(subject, predicate, object);
								key = "";
							}
						
						//Bug check
						if(key == null)
						{
							//Note that I should be discarding those statements which define the blank nodes to be Seq for now
							System.out.println("^^^^^^^^^^Failed to retrieve object type");
							printStatement(subject, predicate, object);
						}
						else
						{
							Integer count = objs.get(key);
							if(count == null)
								objs.put(key, 1);
							else
								objs.put(key, count + 1);
						}
					}

					
					
				}
				
			}
        }     
	}
	
	public static void printStatement(Resource subject, Property predicate, RDFNode object)
	{
		if(subject.getLocalName() != null)
			System.out.print(subject.getLocalName());
		else
			System.out.print(subject.toString());
		System.out.print("  " + predicate.toString() + "  ");
        System.out.println(object.toString());
	}


}
