package edu.mit.db.rstore.impl;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a many to many linkage table.  It adds only a second primary key and overrides those methods appropriate for the SQL
 * 
 * @author Angelika
 *
 */

public class ManyToManyTable extends PropertyTable
{
	private String pkey2;
	private String pkey2_col_name;
	
	public ManyToManyTable(String tname, String primary_key, String primary_key_name, String primary_key2, String primary_key2_name)
	{
		super(tname, primary_key, primary_key_name);
		
		pkey2 = primary_key2;
		pkey2_col_name = primary_key2_name;
	}
	
	/**
	 * Needs to be overridden in this class.  Should be simpler than the superclass
	 */
	public void constructSQL()
	{	
		String columnSQLString = "";
		if (predicates_to_columns.size() > 0){
			for(String s : predicates_to_columns.values())
				columnSQLString += s + "  varchar, ";
		}
		else{
			Iterator<String> i = columns.keySet().iterator();
			while (i.hasNext()){
				String col_type = (String)i.next();
				columnSQLString += columns.get(col_type) + "  varchar, ";
			}
		}
		create_table_command = "CREATE TABLE " + table_name +
								"( " + pkey_col_name + " varchar, " +
									   pkey2_col_name  + " varchar, " +
								       columnSQLString +
								 " PRIMARY KEY( " + pkey_col_name + " , " + pkey2_col_name +								 
								 "))";

	}
	
	public LinkedList<String> getPrimaryKeys()
	{
		LinkedList<String> pkeys = new LinkedList<String>();
		pkeys.add(pkey);
		pkeys.add(pkey2);
		
		return pkeys;
	}
	
	public LinkedList<String> getPrimaryKeyColumns()
	{
		LinkedList<String> pkey_names = new LinkedList<String>();
		
		pkey_names.add(pkey_col_name);
		pkey_names.add(pkey2_col_name);
		
		return pkey_names;
	}
	
	public void print()
	{

		System.out.println(table_name);
		System.out.println(pkey_col_name + "  " + pkey2_col_name + " (pkey)  ");

		//Predicates to columns is not used in this class
		
		System.out.println();
	}
}
