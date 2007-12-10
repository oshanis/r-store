package edu.mit.db.rstore.impl;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class represents the tables which are members of a rdf:domain which is a class identified 
 * as a table in the RDB schema and a rdf:range identified as a collection entity such as rdf:Seq
 * 
 * @author oshani
 *
 */
public class OneToManyTable extends PropertyTable {

	private String pkey2;
	private String pkey2_col_name;
	private String predicate_name;

	private String fkeyTable = null;
	private String fkeyCol = null;

	public OneToManyTable(String tname, String primary_key, String primary_key_name, String primary_key2, String primary_key2_name, String pred) {
		super(tname, primary_key, primary_key_name);
		pkey2 = primary_key2;
		pkey2_col_name = primary_key2_name;
		predicate_name = pred;
	}

	/**
	 * Overloaded constructor to facilitate foreign keys
	 * 
	 * @param tname
	 * @param pred
	 * @param primary_key
	 * @param primary_key_name
	 * @param primary_key2
	 * @param primary_key2_name
	 * @param foreign_key_table
	 * @param foreign_key_col
	 */
	public OneToManyTable(	String tname, String pred,
							String primary_key, String primary_key_name, 
							String primary_key2, String primary_key2_name,
							String foreign_key_table, String foreign_key_col)
	{
		super(tname, primary_key, primary_key_name);
		
		pkey2 = primary_key2;
		pkey2_col_name = primary_key2_name;
		predicate_name = pred;
		fkeyTable = foreign_key_table;;
		fkeyCol = foreign_key_col;
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
		if (fkeyTable == null && fkeyCol == null){
			System.out.println("here");
			create_table_command = "CREATE TABLE " + table_name +
			"( " + pkey_col_name + " varchar, " +
				   pkey2_col_name  + " varchar, " +
			       columnSQLString +
			 " PRIMARY KEY( " + pkey_col_name + " , " + pkey2_col_name +								 
			 "))";
		}
		else{
			create_table_command = "CREATE TABLE " + table_name +
			"( " + pkey_col_name + " varchar, " +
				   pkey2_col_name  + " varchar, " +
			       columnSQLString +
			 " PRIMARY KEY( " + pkey_col_name + " , " + pkey2_col_name +" ) , "+ 
			 "FOREIGN KEY ( " + pkey_col_name + " ) " +" REFERENCES " + fkeyTable +  " ( "+ fkeyCol+") " +
			 		" ON DELETE CASCADE ) ";
		}
	}

	public String getSQL()
	{
		constructSQL();
		return create_table_command;
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
	
	public String getPredicate(){
		return this.predicate_name;
	}
	
	public void print()
	{

		System.out.println(table_name);
		System.out.println(pkey_col_name + "  " + pkey2_col_name + " (pkey)  ");

		//Predicates to columns is not used in this class
		
		System.out.println();
	}

}
