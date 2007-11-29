package edu.mit.db.rstore.impl;

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
	public void constructSQL(/*TODO:  Decide on parameter data structures*/)
	{
		//TODO:
		/*
		 * Write the generation code.  Will be quick once the parameter data structures are decided
		 */
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
		System.out.print(pkey_col_name + "  " + pkey2_col_name + " (pkey)  ");
		System.out.println();
		System.out.println();
	}
}
