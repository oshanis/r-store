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
	private RDFStore rdf;
	private HashMap<String, Integer> predicates;
	private HashMap<String, Integer> subjects;
	private Integer [] [] frequencies;
	private int discarded_statements;
	
	
	public FrequencyCounter(RDFStore r)
	{
		rdf = r;
		discarded_statements = 0;
		sortStrings();
		//dumpIndicies();
		frequencies = new Integer [subjects.size()][predicates.size() + 1];
		for(int i = 0; i < subjects.size(); i++)
			for(int j = 0; j <= predicates.size(); j++)
				frequencies[i][j] = new Integer(0);
		
		System.out.println("Subjects:  " + subjects.keySet().size());
		System.out.println("Predicates:  " + predicates.keySet().size());
		
		constructTable();
		dumpTable();
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
	
	public HashMap<String, Integer> getRowMapping()
	{
		return subjects;
	}
	
	public HashMap<String, Integer> getColumnMapping()
	{
		return predicates;
	}
	
	private void sortStrings()
	{
		predicates = new HashMap<String, Integer>();
		subjects = new HashMap<String, Integer>();
		
		//Put the thingies inside a tree set to sort them
		TreeSet<String> sorter = new TreeSet<String>();
		
		HashSet<String> str = rdf.getClassNamespaces();
		for(String s : str)
			if(s != null)
				sorter.add(s);
		
		int i = 0;
		for(String s : sorter)
		{
			subjects.put(s, i);
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
			i++;
		}
	}
	
	private void dumpIndicies()
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
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		
		while(triples.hasNext())
		{
			Statement stmt = triples.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();

			this_subject = subject.toString();
			
			row_index = subjects.get(subject.getNameSpace());
			//Need to eventually add some book keeping here for multiple outgoing arcs, but for now we assume none
			col_index = predicates.get(predicate.toString());
			
			if(row_index != null && col_index != null)
			{
				//Increment the frequency
				frequencies[row_index][col_index]++;
				
				//Increment the count of the subject occurrence, if its different than the previous subject
				if(!prev_subject.equals(this_subject))
				{
					frequencies[row_index][width]++;
					prev_subject = this_subject;
				}
			}
			else
				discarded_statements++;
		}
		
		//Need to update the count field for the final subject because the loop has expired.  Its the same row_index
		if(row_index != null)
			frequencies[row_index][width]++;
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
		
		System.out.println("Discarded statements due to null subject or predicate:  " + discarded_statements);
	}
}
