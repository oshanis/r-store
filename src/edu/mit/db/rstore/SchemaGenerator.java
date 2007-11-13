package edu.mit.db.rstore;

import java.util.HashMap;
import java.util.LinkedList;

public interface SchemaGenerator {

	/**
	 * This method processes the RDF statements from the RDF store to determine what schema would 
	 * minimize the number of nulls in the row-store.
	 * 
	 * @return schema string which would create the table and the columns
	 * For eg: The single string in the linked list returned by this method should be of this format.
	 * "CREATE TABLE cds
	 * 	( name VARCHAR(50) NOT NULL,
	 * 	artist VARCHAR(50),
	 * 	country VARCHAR(25),
	 * 	company VARCHAR(25),
	 *  price VARCHAR(10));" 
	 *  year VARCHAR(10));" 
	 */
	public LinkedList<String> getSchema();
	

}
