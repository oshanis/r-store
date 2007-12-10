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
	private String predicate_name;
	
	private String fkeyTable1 = null;
	private String fkeyTable2 = null;
	private String fkeyCol1 = null;
	private String fkeyCol2 = null;
	
	
	public ManyToManyTable(String tname, String primary_key, String primary_key_name, String primary_key2, String primary_key2_name, String pred)
	{
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
	 * @param foreign_key_table1
	 * @param foreign_key1_col
	 * @param foreign_key_table2
	 * @param foreign_key2_col
	 */
	public ManyToManyTable(	String tname, String pred,
							String primary_key, String primary_key_name, 
							String primary_key2, String primary_key2_name,
							String foreign_key_table1, String foreign_key1_col,
							String foreign_key_table2, String foreign_key2_col)
	{
		super(tname, primary_key, primary_key_name);
		
		pkey2 = primary_key2;
		pkey2_col_name = primary_key2_name;
		predicate_name = pred;
		fkeyTable1 = foreign_key_table1;
		fkeyTable2 = foreign_key_table2;
		fkeyCol1 = foreign_key1_col;
		fkeyCol2 = foreign_key2_col;
	}
	
	public PredicateRule getPredicateRule()
	{
		return new PredicateRule(predicate_name, pkey, pkey2, PredicateRule.Direction.FORWARD);
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
		if (fkeyTable1 == null && fkeyTable2 == null && fkeyCol1 == null && fkeyCol2 == null){
			create_table_command = "CREATE TABLE " + table_name +
			"( " + pkey_col_name + " varchar, " +
				   pkey2_col_name  + " varchar, " +
			       columnSQLString +
			 " PRIMARY KEY( " + pkey_col_name + " , " + pkey2_col_name +")) ";
		}
		else{
			create_table_command = "CREATE TABLE " + table_name +
			"( " + pkey_col_name + " varchar, " +
				   pkey2_col_name  + " varchar, " +
			       columnSQLString +
			 " PRIMARY KEY( " + pkey_col_name + " , " + pkey2_col_name +" ) , "+ 
			 "FOREIGN KEY ( " + pkey_col_name + " ) " +" REFERENCES " + fkeyTable1 +  " ( "+ fkeyCol1+")  ON DELETE CASCADE , "+
			 "FOREIGN KEY ( " + pkey2_col_name + " ) " +" REFERENCES " + fkeyTable2 +  " ( "+ fkeyCol2 +")  ON DELETE CASCADE ) ";
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
