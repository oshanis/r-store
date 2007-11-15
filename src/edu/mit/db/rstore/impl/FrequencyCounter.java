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
	private Vector<Vector<Integer>> frequencies;
	
	
	public FrequencyCounter(RDFStore r)
	{
		rdf = r;
		sortStrings();
		dumpIndicies();
		frequencies = new Vector<Vector<Integer>>(subjects.keySet().size());
		int width = predicates.keySet().size() + 1;
		for(Vector<Integer> v : frequencies)
		{
			v = new Vector<Integer>(width);
			for(Integer i : v)
				i = new Integer(0);
		}
		
		constructTable();
	}
	
	public Vector<Vector<Integer>> getFrequencyTable()
	{
		return frequencies;
	}
	
	private void sortStrings()
	{
		predicates = new HashMap<String, Integer>();
		subjects = new HashMap<String, Integer>();
		
		//Put the thingies inside a tree set to sort them
		TreeSet<String> sorter = new TreeSet<String>();
		
		HashSet<String> str = rdf.getClassNamespaces();
		for(String s : str)
			sorter.add(s);
		
		int i = 0;
		for(String s : sorter)
		{
			subjects.put(s, i);
			i++;
		}
		
		i = 0;
		sorter.clear();
		
		str = (HashSet<String>)rdf.getPredicateTable().keySet();
		
		for(String s : str)
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
			
			if(row_index == null || col_index == null)
				System.err.println("ERROR in FrequencyCounter.constructTable(); row index or column index was null!");
			
			//Increment the frequency
			Vector<Integer> row = frequencies.get(row_index);
			Integer count = row.get(col_index);
			count++;
			
			//Increment the count of the subject occurrence, if its different than the previous subject
			if(!prev_subject.equals(this_subject))
			{
				count = row.get(width);
				count++;
				prev_subject = this_subject;
			}
			
		}
		
		//Need to update the count field for the final subject because the loop has expired.  Its the same row_index
		Integer count = frequencies.get(row_index).get(width);
		count++;
	}
	
	//For now just the number format
	public void dumpTable()
	{
		for(Vector<Integer> v : frequencies)
		{
			for(Integer i : v)
				System.out.print(i + "  ");
			System.out.println();
		}
	}
}
