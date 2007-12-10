package edu.mit.db.rstore.impl;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class represents the tables which are members of a rdf:domain which is a class identified 
 * as a table in the RDB schema and a rdf:range identified as a collection entity such as rdf:Seq
 * 
 *  TODO Add ForeignKey support
 *  FIXME There's not much difference from the ManyToManyTable! (until support for foreign keys are added)
 *  
 * @author oshani
 *
 */
public class OneToManyTable extends PropertyTable {

	private String pkey2;
	private String pkey2_col_name;
	private String predicate_name;
	
	public OneToManyTable(String tname, String primary_key, String primary_key_name, String primary_key2, String primary_key2_name, String pred) {
		super(tname, primary_key, primary_key_name);
		pkey2 = primary_key2;
		pkey2_col_name = primary_key2_name;
		predicate_name = pred;
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
