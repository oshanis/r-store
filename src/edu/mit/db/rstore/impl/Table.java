package edu.mit.db.rstore.impl;

import java.util.Vector;

import com.sun.org.apache.regexp.internal.recompile;

/**
 * This class represents a subject which has enough properties that would make it a table in our schema
 * 
 * @author oshani
 *
 */
public class Table {
	
	private String name;
	
	private Vector<String> columns;
	
	public void addColumn(String newColumnName){
		this.columns.add(newColumnName);
	}
	
	public Vector<String> getColumns(){
		return this.columns;
	}
	
	public String getTableName(){
		return this.name;
	}

}
