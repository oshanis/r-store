package edu.mit.db.rstore.impl;

import java.util.*;

import edu.mit.db.rstore.*;

public class StatisticalSchemaGenerator implements SchemaGenerator
{
	//Needs the RDFStore and a FrequencyCounter
	private RDFStore rdf;
	private FrequencyCounter ftable;
	
	//And it must store the schema which it comes up with
	private LinkedList<PropertyTable> schema;
	
	public StatisticalSchemaGenerator(RDFStore rdf_store)
	{
		rdf = rdf_store;
		ftable = new FrequencyCounter(rdf);
		schema = new LinkedList<PropertyTable>();
	}
	
	/*
	 * 1.  Put all distinct subject types in property tables
	 * 2.  For each nonzero entry in the frequency table, choose an arc direction based on the cost function in my notebook
	 * 3.  All those property tables which at the end of this pass have nothing in them should be discarded
	 * 4.  Format the SQL commands based on the types gotten from the RDF.  Maybe the PropertyTables can do this for me
	 */
	private void makeSchema()
	{
		//Step 1
		HashSet<String> subjects = rdf.getSubjectTypes();
		
		//I'll want to be able to get the property table corresponding to a particular subject (row)
		HashMap<String, PropertyTable> ptables = new HashMap<String, PropertyTable>();
		for(String s : subjects)
			ptables.put(s, new PropertyTable("Table_" + s, s, "PKey_" + s));
		
		Vector<Vector<Integer>> table = ftable.getFrequencyTable();
	}
	
	
	/**
	 * See interface for explanation
	 */
	public LinkedList<PropertyTable> getSchema()
	{
		return schema;
	}
}
