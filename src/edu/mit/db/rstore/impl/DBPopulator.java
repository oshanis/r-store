package edu.mit.db.rstore.impl;

import java.util.*;
import edu.mit.db.rstore.*;

public class DBPopulator 
{
	//Needs at least the schemas from the schema generator and the RDFStore to get the triples from
	private LinkedList<PropertyTable> schemas;
	private RDFStore store;
	
	public DBPopulator(LinkedList<PropertyTable> db_schemas, RDFStore rdfstore)
	{
		schemas = db_schemas;
		store = rdfstore;
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
	 */
	
}
