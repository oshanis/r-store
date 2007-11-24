package edu.mit.db.rstore;

import edu.mit.db.rstore.impl.*;
import java.util.LinkedList;

public interface SchemaGenerator {

	/**
	 * This method processes the RDF statements from the RDF store to determine what schema would 
	 * minimize the number of nulls in the row-store.
	 * 
	 * @return schema as a list of PropertyTables, which contain the SQL schemas for the tables and the rules on how to populate them
	 * with triples taken from the RDFStore
	 */
	public LinkedList<PropertyTable> getSchema();
	

}
