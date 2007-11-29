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
		
		makeSchema();
	}
	
	/*
	 * 1.  Put all distinct subject types in property tables
	 * 2.  For each nonzero entry in the frequency table, choose an arc direction based on the cost function in my notebook
	 * 3.  All those property tables which at the end of this pass have nothing in them should be discarded
	 * 4.  Format the SQL commands based on the types gotten from the RDF.  Maybe the PropertyTables can do this for me
	 * 
	 * Cost in forward direction = subject count - entry
	 * Cost in reverse direction = object count - entry
	 */
	private void makeSchema()
	{
		Vector<Vector<Integer>> table = ftable.getFrequencyTable();
		HashMap<String, Integer> row_map = ftable.getRowMapping();
		HashMap<String, Integer> col_map = ftable.getColumnMapping();
		
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		Integer count_index = new Integer(table.get(0).size() - 1);
		
		//Step 1
		HashSet<String> subjects = rdf.getSubjectTypes();
		
		//I'll want to be able to get the property table corresponding to a particular subject (row)
		HashMap<String, PropertyTable> ptables = new HashMap<String, PropertyTable>();
		for(String s : subjects)
		{
			row_index = row_map.get(s);
			if(row_index == null)
				throw new RuntimeException("Disaster");
			
			//Put them there anyway, because its possible to reverse arcs into them
			//if(table.get(row_index).get(count_index) != 0)
				ptables.put(s, new PropertyTable("Table_" + s, s, "PKey_" + s));
		}
		
		for(PropertyTable p : ptables.values())
			p.print();
		
		//Step 2
	}
	
	
	/**
	 * See interface for explanation
	 */
	public LinkedList<PropertyTable> getSchema()
	{
		return schema;
	}
}
