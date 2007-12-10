package edu.mit.db.rstore.impl;

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
	
	public OneToManyTable(String tname, String primary_key, String primary_key_name) {
		super(tname, primary_key, primary_key_name);
		// TODO Auto-generated constructor stub
	}

}
