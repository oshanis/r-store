package edu.mit.db.rstore.impl;

/**
 * This class contains all the information that is necessary to completely specify a Property Table which can be entered into a
 * database.  It is designed so that it can be modified dynamically, perhaps as some property table generating algorithm works.
 * It consists of the following pieces of information:
 * 		A String which is the name of the table in the database
 * 		A String which is the command to send to the database command line to actually instantiate the Property Table
 * 		A LinkedList of RDF Subject types which comprise the primary key of the table
 * 		A LinkedList of RDF Predicates which comprise the other attributes in the property table, aside from the primary key
 * 		A HashMap to map from RDF Subject / Predicate -> SQL column names
 * 
 * A Property Table can be thought of in the following way:
 * Schema:	Subject (pkey)					Predicate1													Predicate2  ...
 * Data:	I; an instance of type Subject	O; the Object instance from the triple <I, Predicate1, O> 	...
 * 			.								.															.
 * 			.								.															.
 * 
 * This class specifies ONLY the schema, and a set of rules for populating the table given triples.  It does not actually populate the table
 * Foreign keys are not currently supported, that would be nice to do.
 * Automatic create table generation should be taken care of
 * Storing the database data types for fields should also be done
 */

import java.util.*;
public class PropertyTable 
{
	//Identifier for the table
	private String table_name;
	//The formatted SQL command to create the table
	private String create_table_command;
	//Lists the primary keys of a table represented as RDF namespace prefixes (subject types)
	private LinkedList<String> primary_keys;
	//Lists the other attributes in the property table as RDF namespace prefixes (PREDICATE types)
	private LinkedList<String> other_attributes;
	//Maps each RDF entity, either a predicate or a subject type, to the string representation of its column name within the table
	private HashMap<String, String> attributes_to_columns;
	
	/**
	 * PropertyTable constructor.
	 * 
	 * @param name - String specifying the SQL identifier for the table.  Not mutable.
	 * @param pkeys - LinkedList<String> specifying the initial primary keys of the table as RDF Subject(s)
	 * @param attr - LinkedList<String> specifying the initial attributes of the tables as RDF Predicate(s)
	 * @param attr_to_cols - HashMap<String, String> mapping the RDF identifiers in the above two params to their column identifiers in the Property Table
	 */
	public PropertyTable(String name, LinkedList<String> pkeys, LinkedList<String> attr, HashMap<String, String> attr_to_cols)
	{
		table_name = name;
		create_table_command = "";
		primary_keys = pkeys;
		other_attributes = attr;
		attributes_to_columns = attr_to_cols;
	}
	
	/**
	 * Get the name of the table, for use in query processing maybe.
	 */
	public String getName()
	{
		//Thou shalt not mutate!
		return new String(table_name);
	}
	
	/**
	 * Accessor to SQL command
	 */
	public String getSQL()
	{
		return create_table_command;
	}
	
	/**
	 * Ideally, there should be a method to automatically generate the create table statement within this class, but one does not
	 * exist as of yet.  Therefore, the SQL command needs to be set externally.  Hopefully this will be changed in the future.
	 * 
	 * @param sql_command - String which represents the create table SQL statement
	 */
	public void setSQL(String sql_command)
	{
		create_table_command = sql_command;
	}
	
	/**
	 * Accessor for primary keys as RDF Subjects.  Note that there is rep exposure here, DO NOT MODIFY THIS LIST.
	 */
	public LinkedList<String> getPrimaryKey()
	{
		return primary_keys;
	}
	
	/**
	 * Accessor for the other attributes as RDF Predicates.  Also rep exposure here.
	 */
	public LinkedList<String> getAttributes()
	{
		return other_attributes;
	}
	
	/**
	 * Accessor for the attribute -> column name map
	 */
	public HashMap<String, String> getMap()
	{
		return attributes_to_columns;
	}
	
	/**
	 * Add a primary key Subject.  You will also need to specify a column identifier.  This method will keep the interals consistent
	 * 
	 * @param subj - String specifying the RDF Subject to add
	 * @param col - String specifying the identifier with in the Property Table to map to
	 */
	public void addPrimaryKey(String subj, String col)
	{
		primary_keys.add(subj);
		attributes_to_columns.put(subj, col);
	}
	
	/**
	 * Remove a primary key Subject
	 * 
	 * @param subj - String specifying what to remove
	 */
	public void removePrimaryKey(String subj)
	{
		primary_keys.remove(subj);
		attributes_to_columns.remove(subj);
	}
	
	/**
	 * Add an attribute.  Does the same thing as adding a primary key
	 */
	public void addAttribute(String pred, String col)
	{
		other_attributes.add(pred);
		attributes_to_columns.put(pred, col);
	}
	
	/**
	 * Remove an attribute.  Does the same thing as removing a primary key
	 */
	public void removeAttribute(String pred)
	{
		other_attributes.remove(pred);
		attributes_to_columns.remove(pred);
	}
}