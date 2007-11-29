package edu.mit.db.rstore.impl;

import edu.mit.db.rstore.*;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.*;

/**
 * Iterates once over the RDF store and constructs a frequency table, which is the number of times a given subject S has an outgoing
 * arc with a particular predicate P.  The table will have |S| rows and |P| + 1 columns, because at the end will be appended a count
 * for the total number of occurences of subject type S.  The table will be sorted lexicographically
 * 
 * @author Angelika
 */

public class FrequencyCounter 
{
	//Need to identify the relation types
	public enum Relation
	{
		//caveat:  one to one is really one to zero or one, so its more like all to some
		NONE,
		ONE_TO_ONE,
		ONE_TO_MANY,
		MANY_TO_ONE,
		MANY_TO_MANY
	};
	
	private RDFStore rdf;
	private HashMap<String, Integer> predicates;
	private HashMap<String, Integer> subjects;
	private Integer [] [] frequencies;
	private Relation [] [] mask;
	
	//Introduced the following 2 variables and the supporting methods, 
	//because I had trouble mapping from the String -- Oshani
	private HashMap<Integer, String> predicates_1; 
	private HashMap<Integer, String> subjects_1;

	//diagnostics...
	private int anon_subject;
	private int anon_object;
	private int anon_both;
	private long total_statements;
	
	
	public FrequencyCounter(RDFStore r)
	{
		rdf = r;
		sortStrings();
		//dumpIndicies();
		frequencies = new Integer [subjects.size()][predicates.size() + 1];
		for(int i = 0; i < subjects.size(); i++)
			for(int j = 0; j <= predicates.size(); j++)
				frequencies[i][j] = new Integer(0);
		
		mask = new Relation [subjects.size()] [predicates.size()];
		for(int i = 0; i < subjects.size(); i++)
			for(int j = 0; j < predicates.size(); j++)
				mask[i][j] = Relation.NONE;
	
		System.out.println("Subjects:  " + subjects.keySet().size());
		System.out.println("Predicates:  " + predicates.keySet().size());
		
		constructTable();
	}
	
	public Vector<Vector<Integer>> getFrequencyTable()
	{
		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
		
		for(int i = 0; i < subjects.size(); i++)
		{
			Vector<Integer> row = new Vector<Integer>();
			for(int j = 0; j <= predicates.size(); j++)
				row.add(frequencies[i][j]);
			v.add(row);
		}
		
		
		return v;
	}
	
	public Vector<Vector<Relation>> getRelationMask()
	{
		Vector<Vector<Relation>> v = new Vector<Vector<Relation>>();
		
		for(int i = 0; i < subjects.size(); i++)
		{
			Vector<Relation> row = new Vector<Relation>();
			for(int j = 0; j < predicates.size(); j++)
				row.add(mask[i][j]);
			v.add(row);
		}
		
		return v;
	}
	
	public HashMap<String, Integer> getRowMapping()
	{
		return subjects;
	}
	
	public HashMap<String, Integer> getColumnMapping()
	{
		return predicates;
	}
	
	public HashMap<Integer, String> getRowMapping_1()
	{
		return subjects_1;
	}
	
	public HashMap<Integer, String> getColumnMapping_1()
	{
		return predicates_1;
	}
	
	private void sortStrings()
	{
		predicates = new HashMap<String, Integer>();
		subjects = new HashMap<String, Integer>();

		predicates_1 = new HashMap<Integer, String>();
		subjects_1 = new HashMap<Integer, String>();

		//Put the thingies inside a tree set to sort them
		TreeSet<String> sorter = new TreeSet<String>();
		
		HashSet<String> str = rdf.getSubjectTypes();
		
		for(String s : str)
			if(s != null)
				sorter.add(s);
		
		int i = 0;
		for(String s : sorter)
		{
			subjects.put(s, i);
			subjects_1.put(i, s);
			i++;
		}
		
		i = 0;
		sorter.clear();
		
		//Stupid hash maps...
		Collection<String> strings = rdf.getPredicateTable().keySet();
		
		for(String s : strings)
			if(s != null)
				sorter.add(s);
		
		for(String s : sorter)
		{
			predicates.put(s, i);
			predicates_1.put(i, s);
			i++;
		}
		
		//dumpIndicies();
	}
	
	public void dumpIndicies()
	{
		System.out.println("Subjects:  ");
		for(String s : subjects.keySet())
			System.out.println(s + "  " + subjects.get(s));
		System.out.println();
		
		System.out.println("Predicates:  ");
		for(String s : predicates.keySet())
			System.out.println(s + "  " + predicates.get(s));
		System.out.println();
		System.out.println();
	}
	
	private void constructTable()
	{
		StmtIterator triples = rdf.getIterator();
		int width = predicates.keySet().size();
		
		String prev_subject = "";
		String this_subject = "";
		String this_subject_type = "";
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		
		anon_subject = 0;
		anon_object = 0;
		anon_both = 0;
		total_statements = 0;
		
		//Assuming for now that there are no duplicate predicate names, though I know there are
		HashSet<String> outgoing = new HashSet<String>();
		
		while(triples.hasNext())
		{
			total_statements++;
			
			Statement stmt = triples.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
         
            //Normal statement
            if(!subject.isAnon() && !object.isAnon())
            {
            	this_subject = subject.getLocalName();
    			this_subject_type = rdf.getTypeFromSubjects(this_subject);
    			row_index = subjects.get(this_subject_type);
    			col_index = predicates.get(predicate.toString());
    			
    			//Still don't know how to deal with blank node identification
    			
    			if(row_index != null && col_index != null)
    			{
    				//Increment the frequency
    				frequencies[row_index][col_index]++;
    				
    				//Increment the count of the subject occurrence, if its different than the previous subject
    				if(!prev_subject.equals(this_subject))
    				{
    					frequencies[row_index][width]++;
    					prev_subject = this_subject;
    					outgoing.clear();
    				}
    				
    				//This needs to be after the previous block
    				if(outgoing.contains(predicate.toString()))
    					mask[row_index][col_index] = Relation.ONE_TO_MANY;
    				else
    				{
    					mask[row_index][col_index] = Relation.ONE_TO_ONE;
    					outgoing.add(predicate.toString());
    				}
    			}
    			else
    			{
    				
    				System.out.print("Invalid statement:  ");
    				if(subject.getLocalName() != null)
    					System.out.print(subject.getLocalName());
    				else
    					System.out.print(subject.toString());
    				System.out.print("  " + predicate.toString() + "  ");
    	            System.out.println(object.toString());
    			}
            }
            else
            {
            	if(subject.isAnon() && object.isAnon())
            		anon_both++;
            	else
            	{
            		if(subject.isAnon())
            			anon_subject++;
            		else
            			anon_object++;
            	}
            }
		}
		
		//Now the reverse direction so I can get the many to one relations
		//Oops, need the damn thing sorted again
	}
	
	//For now just the number format
	public void dumpTable()
	{
		for(int i = 0; i < subjects.size(); i++)
		{
			for(int j = 0; j <= predicates.size(); j++)
				System.out.print(frequencies[i][j] + "  ");
			System.out.println();
		}
		
		System.out.println();
		
		for(int i = 0; i < subjects.size(); i++)
		{
			for(int j = 0; j < predicates.size(); j++)
				System.out.print(mask[i][j].toString() + "  ");
			System.out.println();
		}
		
		System.out.println();
		
		//We need to do something about those blank nodes
		System.out.println("Statements with anonymous subject:  " + anon_subject);
		System.out.println("Statements with anonymous object:  " + anon_object);
		System.out.println("Statements with anonymous subject and object:  " + anon_both);
		System.out.println("Total statements processed:  " + total_statements);
	}
}
