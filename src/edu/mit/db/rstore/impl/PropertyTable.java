package edu.mit.db.rstore.impl;

/**
 * This class contains all the information that is necessary to completely specify a Property Table which can be entered into a
 * database.  It is designed so that it can be modified dynamically, perhaps as some property table generating algorithm works.
 * It consists of the following pieces of information:
 * 		A String which is the name of the table in the database
 * 		A String which is the command to send to the database command line to actually instantiate the Property Table
 *		A String which is a single RDF subject type which is the primary key for the table
 *		A String which is the column identifier for the primary key
 *		A HashMap from predicates to column identifiers within the table
 * 
 * A Property Table can be thought of in the following way:
 * Schema:	Subject (pkey)					Predicate1													Predicate2  ...
 * Data:	I; an instance of type Subject	O; the Object instance from the triple <I, Predicate1, O> 	...
 * 			.								.															.
 * 			.								.															.
 * 
 * 11/13/07
 * This class specifies ONLY the schema, and a set of rules for populating the table given triples.  It does not actually populate the table
 * Foreign keys are not currently supported, that would be nice to do.
 * Automatic create table generation should be taken care of
 * Storing the database data types for fields should also be done
 * This class currently requires unique subject names.  This is a problem
 * 
 * 11/15/07
 * The class is now only defined for standard property tables.  A subclass should be used for many to many relations
 * Database datatypes should be provided either by the Schema Generator or some other relaxation algorithm module, but not this.
 * That means SQL command generation can not be done internally - it would require type information which exists outside the class
 * Unique subject problem is avoided because now we assume only one primary key.  Will have to revisit it for the many to many case,
 * but it seems best to me to tackle this problem in a different class.
 * 
 * 11/24/07
 * Added the stub for SQL generation method, will complete as soon as external interfaces are satisfied
 * 
 */

import java.util.*;
public class PropertyTable 
{
	//Identifier for the table
	protected String table_name;
	//RDF type of the primary key
	protected String pkey;
	//Identifier for the primary key
	protected String pkey_col_name;
	//The formatted SQL command to create the table
	protected String create_table_command;
	//Maps each RDF entity, a predicate, to the string representation of its column name within the table
	private HashMap<PredicateRule, String> predicates_to_columns;
	
	private HashMap<String, String> columns = new HashMap<String, String>();
	
	/**
	 * PropertyTable constructor.
	 * 
	 * @param tname - String specifying the SQL identifier for the table.  Not mutable.
	 * @param primary_key - String specifying the RDF type of the primary key for the table
	 * @param primary_key_name - String specifying the column name for the primary key
	 */
	public PropertyTable(String tname, String primary_key, String primary_key_name)
	{
		table_name = tname;
		create_table_command = "";
		pkey = primary_key;
		pkey_col_name = primary_key_name;
		predicates_to_columns = new HashMap<PredicateRule, String>();
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
	 * Pass the property table the database type mappings and the predicate's object type mappings, both of which should be available
	 * from the RDFStore, so the Schema Generator has access to them, and the PropertyTable will generate the SQL for you
	 */
	public void constructSQL(/*TODO:  Decide on parameter data structures*/)
	{
		//TODO:
		/*
		 * Write the generation code.  Will be quick once the parameter data structures are decided
		 */
	}
	
	/**
	 * Accessor for primary keys as RDF Subject.
	 */
	public String getPrimaryKey()
	{
		return new String(pkey);
	}
	
	/**
	 * Accessor for the primary key column name
	 */
	public String getPrimaryKeyColumn()
	{
		return new String(pkey_col_name);
	}
	
	/**
	 * Accessor for the other attributes as RDF Predicates.  Also rep exposure here.
	 * 
	 * This will automatically cull predicates with duplicate names, which may not be desirable
	 */
	public HashSet<PredicateRule> getAttributes()
	{
		HashSet<PredicateRule> ret = new HashSet<PredicateRule>();
		
		for(PredicateRule p : predicates_to_columns.keySet())
			ret.add(p);
		
		return ret;
	}
	
	/**
	 * Accessor for the attribute -> column name map
	 */
	public HashMap<PredicateRule, String> getMap()
	{
		return predicates_to_columns;
	}
	
	/**
	 * Accessor for the column names, including the primary key.  Does not guarantee order
	 */
	public HashSet<String> getColNames()
	{
		HashSet<String> ids = new HashSet<String>(predicates_to_columns.values());
		ids.add(new String(pkey_col_name));
		return ids;
	}
	
	/**
	 * Add an attribute.
	 */
	public void addAttribute(PredicateRule pred, String col)
	{
		predicates_to_columns.put(pred, col);
	}
	
	/**
	 * Add an attribute with the column name and the column data type.
	 * I do not know how I could use PredicateRule hence the reason
	 * for thie overloaded method --Oshani
	 */
	public void addAttribute(String col, String type)
	{
		columns.put(col, type);
	}
	
	/**
	 * Remove an attribute.
	 */
	public void removeAttribute(PredicateRule pred)
	{
		predicates_to_columns.remove(pred);
	}
	
	/**
	 * Prints out the state of the table
	 */
	public void print()
	{
		System.out.println(table_name);
		System.out.print(pkey_col_name + " (pkey)  ");
		for(String s : predicates_to_columns.values())
			System.out.print(s + "  ");
		System.out.println();
		System.out.println();
	}
	
	
	/**
	 * Prints out the state of the table
	 * 
	 * I had to duplicate the print method as I am not using the PredicateRule
	 * (I tried to adopt it, but it would make the code too complicated for me) --Oshani
	 */
	public void print_table_wo_PR()
	{
		System.out.println(table_name);
		System.out.print(pkey_col_name + " (pkey)  ");
		Iterator i = columns.keySet().iterator();
		while (i.hasNext()){
			String col_type = (String)i.next();
			System.out.println(col_type + "\t" + columns.get(col_type));
		}
		System.out.println();
		System.out.println();
	}

}