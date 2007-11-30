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
		
		for(PropertyTable p : schema)
			p.print();
	}
	
	/*
	 * 1.  Put all distinct subject types in property tables
	 * 2.  For each nonzero entry in the frequency table, choose an arc direction based on the cost function in my notebook
	 * 3.  All those property tables which at the end of this pass have nothing in them should be discarded
	 * 4.  Format the SQL commands based on the types gotten from the RDF.  Maybe the PropertyTables can do this for me
	 * 
	 * Cost function:
	 * 	if(one to many)
	 * 		use reverse direction
	 * 	if(many to one)
	 * 		use forward direction
	 * 	if(many to many)
	 * 		set up many to many table
	 * 	if(one to one)
	 * 		choose the direction with the larger frequency
	 */
	private void makeSchema()
	{
		Vector<Vector<Integer>> table = ftable.getFrequencyTable();
		Vector<Vector<FrequencyCounter.Relation>> mask = ftable.getRelationMask();
		HashMap<String, Integer> row_map = ftable.getRowMapping();
		HashMap<PredicateRule, Integer> col_map = ftable.getColumnMapping();
		
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
		
		//Step 2
		for(String subject : row_map.keySet())
		{
			row_index = row_map.get(subject);
			Vector<Integer> row = table.get(row_index);
			Vector<FrequencyCounter.Relation> mask_row = mask.get(row_index);
			for(PredicateRule pred : col_map.keySet())
			{
				col_index = col_map.get(pred);
				//If the entry is nonzero... ugh enums don't let me use a switch statement!
				if(row.get(col_index) != 0)
				{
					FrequencyCounter.Relation rtype = mask_row.get(col_index);
					
					if(rtype == FrequencyCounter.Relation.ONE_TO_MANY)
					{
						String object = pred.getObject();
						PropertyTable p = ptables.get(object);
						
						if(p == null)
						{
							//TODO:
							//Fix this!
							System.out.println("Must have found a one-to-many relation that points to a literal.  Oops");
						}
						else
						{
							PredicateRule new_pred = new PredicateRule(pred.getPredicate(), subject, object, PredicateRule.Direction.BACKWARD);
							p.addAttribute(new_pred, "col_" + suffix(pred.getPredicate()));
						}
					}
					if(rtype == FrequencyCounter.Relation.MANY_TO_ONE)
					{
						PropertyTable p = ptables.get(subject);
						p.addAttribute(pred, "col_" + suffix(pred.getPredicate()));
					}
					if(rtype == FrequencyCounter.Relation.MANY_TO_MANY)
					{
						//TODO:
						//This will make duplicates.  Fix the duplicates by modifying the mask or the frequency table
						String object = pred.getObject();
						ManyToManyTable m = new ManyToManyTable("Table_" + suffix(pred.getPredicate()), subject, "Pkey_" + subject, object, "Pkey_" + object);
						schema.add(m);
					}
					if(rtype == FrequencyCounter.Relation.ONE_TO_ONE)
					{
						String object = pred.getObject();
						Integer o_index = row_map.get(object);
						
						//There is only one direction to go; forward
						if(o_index == null)
						{
							PropertyTable p = ptables.get(subject);
							p.addAttribute(pred, "col_" + suffix(pred.getPredicate()));
						}
						else
						{
							Integer f_freq = row.get(col_index);
							Integer r_freq = table.get(o_index).get(col_index);
							
							if(f_freq >= r_freq)
							{
								PropertyTable p = ptables.get(subject);
								p.addAttribute(pred, "col_" + suffix(pred.getPredicate()));
							}
							else
							{
								PredicateRule new_pred = new PredicateRule(pred.getPredicate(), subject, object, PredicateRule.Direction.BACKWARD);
								PropertyTable p = ptables.get(object);
								p.addAttribute(new_pred, "col_" + suffix(pred.getPredicate()));
							}
						}
					}
				}
			}
		}
		
		//Step 3
		//This is questionable.  I think any lone nodes in the graph are safe to discard, but its not strictly representing the graph.  If this is
		//objectionable, comment out step 3
		for(PropertyTable p : ptables.values())
			if(p.getAttributes().size() > 0)
				schema.add(p);
		
		
		//Step 4
		//TODO:
		/*
		 * Add the call to the method that is not yet written
		 */
	}
	
	
	/**
	 * See interface for explanation
	 */
	public LinkedList<PropertyTable> getSchema()
	{
		return schema;
	}
	
	private String suffix(String predicate)
	{
		if(predicate.contains("#"))
		{
			int index = predicate.indexOf("#");
			return predicate.substring(index + 1);
		}
		
		return predicate;
	}
}
