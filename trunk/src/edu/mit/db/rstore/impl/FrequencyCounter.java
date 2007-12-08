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
	private HashMap<PredicateRule, Integer> predicates;
	private HashMap<String, Integer> subjects;
	private Integer [] [] frequencies;
	private Relation [] [] mask;
	
	//blank node book keeping
	private HashMap<PredicateRule, Integer> aux_statistics;
	private HashSet<PredicateRule> aux_mask;

	//diagnostics...
	private int discarded_forward;
	private int discarded_backward;
	private int backward_anon;
	private int null_obj_forward;
	private int anon_subject;
	private int anon_object;
	private int anon_both;
	private long total_statements;
	
	
	public FrequencyCounter(RDFStore r)
	{
		rdf = r;
		
		subjects = new HashMap<String, Integer>();
		predicates = new HashMap<PredicateRule, Integer>();
		
		sortSubjects();
		constructPredicates();

		frequencies = new Integer [subjects.size()][predicates.size() + 1];
		for(int i = 0; i < subjects.size(); i++)
			for(int j = 0; j <= predicates.size(); j++)
				frequencies[i][j] = new Integer(0);
		
		mask = new Relation [subjects.size()] [predicates.size()];
		for(int i = 0; i < subjects.size(); i++)
			for(int j = 0; j < predicates.size(); j++)
				mask[i][j] = Relation.NONE;
	
		/*System.out.println();
		System.out.println("Subjects:  " + subjects.keySet().size());
		System.out.println("Predicates:  " + predicates.keySet().size());
		System.out.println();*/
		
		constructTable();
		
		dumpIndicies();
		dumpTable();
	}
	
	private void constructPredicates()
	{
		//First collect all those predicates which don't have anything to do with blank nodes, we'll keep those
		//I am assuming the duplicates problem has been fixed, so I don't have to cull them here
		HashMap<String, Vector<LinkedList<String>>> ptable = rdf.getPredicateTable();
		int i = 0;
		for(String s : ptable.keySet())
		{
			Vector<LinkedList<String>> vals = ptable.get(s);
			//Assuming they are of size 2 so I don't have to check that
			for(LinkedList<String> v : vals)
				if(!v.getFirst().equals(Store.SEQ) && !v.getLast().equals(Store.SEQ))
				{
					PredicateRule p = new PredicateRule(s, v.getFirst(), v.getLast(), PredicateRule.Direction.FORWARD);
					if(predicates.get(p) == null)
					{
						predicates.put(p, i);
						i++;
					}
				}			
		}
		
		//Now the blank node business.  Will gather statistics on them now as well... nasty things
		//This concerns me because it could require a lot of memory in the case where there are many of these things, but I didn't know how
		//to eliminate this problem
		
		//Assuming no chained sequences.  Collapse the blank nodes out, with original subject and predicate, but blank node objects
		//This will result in new predicates being formed, which won't be picked up in constructTable, so log the statistics now
		
		//Also assuming that precisely ONE subject initially points to any given blank node.  In RDF this is probably not a constraint, but RDF
		//is an unconstrained standard
		
		//Requires memory on the order of the number of statements involving blank nodes.  This is a serious violation of the not-everything-in-
		//memory constraint.  I have not yet thought of a fix for it
		
		//Also assuming that no literals point to blank nodes.  This I believe is a standard
		HashMap<String, String> bnode_to_subject = new HashMap<String, String>();
		HashMap<String, String> bnode_to_subj_pred = new HashMap<String, String>();
		HashMap<String, HashMap<String, Integer>> bnode_obj_ct = new HashMap<String, HashMap<String, Integer>>();
		
		StmtIterator triples = rdf.getIterator();
		
		Statement stmt;
		Resource subject;
		Property predicate;
		RDFNode object;
		
		//Diagnostics
		int statements = 0;
		
		while(triples.hasNext())
		{
			statements++;
			
			stmt = triples.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();
			
			if(subject.isAnon() && object.isAnon())
			{
				System.out.print("Found a chain of sequences:  ");
//				printStatement(subject, predicate, object);
			}
			else
			{
				//Case:  S -P- [blank]
				if(object.isAnon())
				{
					bnode_to_subject.put(object.toString(), rdf.getTypeFromSubjects(subject.getLocalName()));
					bnode_to_subj_pred.put(object.toString(), predicate.toString());
				}
				//Case:  [blank] -P- O
				if(subject.isAnon())
				{
					HashMap<String, Integer> objs = bnode_obj_ct.get(subject.toString());
					if(objs == null)
					{
						objs = new HashMap<String, Integer>();
						bnode_obj_ct.put(subject.toString(), objs);
					}
					
					String key;
					
					if(object.isLiteral())
						key = Store.LITERAL;
					else
						if(object.isURIResource())
							key = rdf.getTypeFromSubjects(((Resource)object).getLocalName());
						else
						{
//							System.out.println("Found a statement in a blank node whose object was not literal or uri:  ");
//							printStatement(subject, predicate, object);
							key = "";
						}
					
					//Bug check
					if(key == null)
					{
						//Note that I should be discarding those statements which define the blank nodes to be Seq for now
//						System.out.println("Failed to retrieve object type");
//						printStatement(subject, predicate, object);
					}
					else
					{
						Integer count = objs.get(key);
						if(count == null)
							objs.put(key, 1);
						else
							objs.put(key, count + 1);
					}
				}
			}
		}
		
		//Now aggregate over the blank nodes to find the new predicate rules
		aux_statistics = new HashMap<PredicateRule, Integer>();
		
		for(String bnode_id : bnode_to_subject.keySet())
		{
			String subj = bnode_to_subject.get(bnode_id);
			String pred = bnode_to_subj_pred.get(bnode_id);
			HashMap<String, Integer> objs = bnode_obj_ct.get(bnode_id);
			
			//More debugging checks
			if(objs == null)
				System.out.println("Found no objects for bnode:  " + bnode_id + " < " + subj + ", " + pred);
			
			for(String obj : objs.keySet())
			{
				PredicateRule p = new PredicateRule(pred, subj, obj, PredicateRule.Direction.FORWARD);
				Integer count = aux_statistics.get(p);
				//This is the case where this type of predicate rule has not already been discovered
				if(count == null)
				{
					aux_statistics.put(p, objs.get(obj));
					predicates.put(p, i);
					i++;
				}
				//This is the aggregation for predicate rules that already have been discovered
				else
					aux_statistics.put(p, count + objs.get(obj));
			}
		}
		
		/*
		 * A final step is to look for blank node statements that are really many to many relations
		 * 
		 * I need to group blank nodes by predicate rule, and then I need to use the iterator to check if any of their object sets intersect
		 * 
		 * Thinking about it, these operations are a lot like SQL queries.  I wonder, could we have saved ourselves the trouble by putting the
		 * whole damn thing into a modified triple store in Postgres, and using the DB to do the work, then generating the schema, and then recreating?
		 * 
		 * This is a slow step.  It requires as many passes over the iterator as there are distinct predicate rules involving blank nodes
		 */
		aux_mask = new HashSet<PredicateRule>();
		HashMap<PredicateRule, HashSet<String>> prule_to_bnode_ids = new HashMap<PredicateRule, HashSet<String>>();
		
		for(String bnode : bnode_obj_ct.keySet())
		{
			String subj = bnode_to_subject.get(bnode);
			String pred = bnode_to_subj_pred.get(bnode);
			Set<String> bnode_objs = bnode_obj_ct.get(bnode).keySet();
			for(String obj : bnode_objs)
			{
				PredicateRule p = new PredicateRule(pred, subj, obj, PredicateRule.Direction.FORWARD);
				HashSet<String> ids_so_far = prule_to_bnode_ids.get(p);
				if(ids_so_far == null)
				{
					ids_so_far = new HashSet<String>();
					prule_to_bnode_ids.put(p, ids_so_far);
				}
				ids_so_far.add(bnode);
			}
		}
		
		for(PredicateRule p : prule_to_bnode_ids.keySet())
		{
			HashSet<String> bnodes = prule_to_bnode_ids.get(p);
			HashSet<String> object_set = new HashSet<String>();
			boolean decided = false;
			StmtIterator stmts = rdf.getIterator();
			
			while(stmts.hasNext() && !decided)
			{
				Statement statement = stmts.nextStatement();
				
				Resource sub = statement.getSubject();
				if(sub.isAnon() && bnodes.contains(sub.toString()))
				{
					RDFNode obj = statement.getObject();
					if(object_set.contains(obj.toString()))
					{
						decided = true;
						aux_mask.add(p);
					}
					else
						object_set.add(obj.toString());
				}
			}
		}
	}
	
	/**
	 * This table is a copy, so you can alter the values without breaking the table
	 */
	public Vector<Vector<Integer>> getFrequencyTable()
	{
		Vector<Vector<Integer>> v = new Vector<Vector<Integer>>();
		
		for(int i = 0; i < subjects.size(); i++)
		{
			Vector<Integer> row = new Vector<Integer>();
			for(int j = 0; j <= predicates.size(); j++)
				row.add(new Integer(frequencies[i][j]));
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
	
	public HashMap<PredicateRule, Integer> getColumnMapping()
	{
		return predicates;
	}
	
	public HashMap<Integer, String> getReverseRowMapping()
	{
		HashMap<Integer, String> rev = new HashMap<Integer, String>();
		
		for(String s : subjects.keySet())
			rev.put(subjects.get(s), s);
		
		return rev;
	}
	
	public HashMap<Integer, PredicateRule> getReverseColumnMapping()
	{
		HashMap<Integer, PredicateRule> rev = new HashMap<Integer, PredicateRule>();
		
		for(PredicateRule p : predicates.keySet())
			rev.put(predicates.get(p), p);
		
		return rev;
	}
	
	private void sortSubjects()
	{
		//Put the thingies inside a tree set to sort them.  I have no idea why I sorted them.  Simply indexing them would do
		TreeSet<String> sorter = new TreeSet<String>();
		
		HashSet<String> str = rdf.getSubjectTypes();
		
		for(String s : str)
			if(s != null)
				sorter.add(s);
		
		int i = 0;
		for(String s : sorter)
		{
			subjects.put(s, i);
			i++;
		}
	}
	
	public void dumpIndicies()
	{
		System.out.println("Subjects:  ");
		for(String s : subjects.keySet())
			System.out.println(subjects.get(s) + "  " + s);
		System.out.println();
		
		System.out.println("Predicates:  ");
		for(PredicateRule p : predicates.keySet())
		{
			System.out.print(predicates.get(p) + "  ");
			p.print();
		}
		System.out.println();
		System.out.println();
	}
	
	public void dumpAux()
	{
		System.out.println("Auxillary Statistics:  ");
		for(PredicateRule p : aux_statistics.keySet())
		{
			System.out.print("(" + aux_statistics.get(p) + ")" + "  ");
			p.print();
		}
		
		System.out.println();
		System.out.println();
	}
	
	private void constructTable()
	{
		forwardPass();
		backwardPass();
		addAux();
	}
	
	/*
	 * Iterates in S-P-O order over statements which obey the predicate rules defined in constructor and listed in predicates
	 * Blank nodes can be ignored, they've already been counted.
	 * Tabulates all frequency statistics and counts
	 * 
	 * Known issues:  
	 * 
	 * For rdf type predicates, the object type gets returned as null.  Since we are not really interested in making these
	 * tables, its ok to do this I think.
	 */
	private void forwardPass()
	{
		StmtIterator triples = rdf.getIterator();
		
		//Index of the count field in the frequency table
		int width = predicates.keySet().size();
		
		String prev_subject = "";
		String this_subject = "";
		String this_subject_type = "";
		String this_object_type = "";
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		
		Statement stmt;
		Resource subject;
		Property predicate;
		RDFNode object;
		
		//Diagnostics
		anon_subject = 0;
		anon_object = 0;
		anon_both = 0;
		total_statements = 0;
		discarded_forward = 0;
		null_obj_forward = 0;
		
		HashSet<PredicateRule> outgoing = new HashSet<PredicateRule>();
		
		while(triples.hasNext())
		{
			total_statements++;
			
			stmt = triples.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
            object = stmt.getObject();
         
            //No subjects should be literals here, if there are I will discard them
            if(!subject.isLiteral() && !subject.isAnon() && !object.isAnon())
            {
            	this_subject = subject.getLocalName();
    			this_subject_type = rdf.getTypeFromSubjects(this_subject);
    			if(object.isLiteral())
    				this_object_type = Store.LITERAL;
    			else
    				if(object.isURIResource())
    					this_object_type = rdf.getTypeFromSubjects(((Resource)object).getLocalName());
    				else
    				{
    					System.out.println("Found an object that was neither a literal or a URI");
    					this_object_type = "";
    				}
    			
    			//This test discards rdf type statements
    			if(this_object_type != null)
    			{
	    			PredicateRule p = new PredicateRule(predicate.toString(), this_subject_type, this_object_type, PredicateRule.Direction.FORWARD);
	    			
	    			row_index = subjects.get(this_subject_type);
	    			col_index = predicates.get(p);
	    			
	    			if(row_index != null && col_index != null)
	    			{
	    				//Increment the the predicate frequency
	    				frequencies[row_index][col_index]++;
	    				
	    				//Increment the count of the subject occurrence, if its different than the previous subject
	    				if(!prev_subject.equals(this_subject))
	    				{
	    					frequencies[row_index][width]++;
	    					prev_subject = this_subject;
	    					outgoing.clear();
	    				}
	    				
	    				//This needs to be after the previous block due to clearing the outgoing set
	    				if(outgoing.contains(p))
	    					mask[row_index][col_index] = Relation.ONE_TO_MANY;
	    				else
	    				{
	    					mask[row_index][col_index] = Relation.ONE_TO_ONE;
	    					outgoing.add(p);
	    				}
	    			}
	    			else
	    			{
	    				//Couldn't find row or column indicies, there was a disaster
	    				/*System.out.print("(Forward) Discarded statement:  ");
	    				printStatement(subject, predicate, object);
	    				p.print();
	    				System.out.println();*/
	    				discarded_forward++;
	    			}
    			}
    			else
    				null_obj_forward++;	//Most likely an rdftype predicate or something else which doesn't belong in the schema
    		}
            else
            {
            	//Debugging output for blank nodes
				/*System.out.print("Blank Node:  ");
				printStatement(subject, predicate, object);*/
				
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
	}
	
	/*
	 * Iterates over the tuples with all their subjects and objects switched.  Its arc reversal.  Its looking for many to one -ness only.
	 * 
	 * Known Issues:
	 * 
	 * Needs BackwardsIterator working
	 * 
	 * Its very possible for subjects to now be literals.  I will discard these things, as they shouldn't be counted - the arc will go in the
	 * forward direction anyway, and should never be reversed
	 * 
	 * It seems that none of the type tests are working correctly after reversal, but I think I can get around it.  Note that this will probably
	 * not generalize to arbitrary RDF syntax.  The correct solution would be to extend the Jena library to support statement reversal.
	 */
	private void backwardPass()
	{
		StmtIterator triples = rdf.getBackwardsIterator();
		
		//Book keeping for grouping subjects in the iterator and indexing into the frequency table
		String prev_subject = "";
		String this_subject = "";
		String this_subject_type = "";
		String this_object_type = "";
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		
		Statement stmt;
		Resource subject;
		Property predicate;
		RDFNode object;
		
		//Diagnostics
		discarded_backward = 0;
		backward_anon = 0;

		HashSet<PredicateRule> outgoing = new HashSet<PredicateRule>();
		
		while(triples.hasNext())
		{
			stmt = triples.nextStatement();
			subject = stmt.getSubject();
			predicate = stmt.getPredicate();
			object = stmt.getObject();
			
			if(!subject.isAnon() && ! object.isAnon())
			{
				if(isLiteral(subject.toString()))
				{
					this_subject = subject.toString();
					this_subject_type = Store.LITERAL;
				}
				else
				{
					this_subject = subject.getLocalName();
					this_subject_type = rdf.getTypeFromSubjects(subject.getLocalName());
				}
				
				if(isLiteral(object.toString()))
					this_object_type = Store.LITERAL;
				else
					this_object_type = rdf.getTypeFromSubjects(suffix(object.toString()));
				
				//At this point is possible that blank nodes and type predicates have slipped through.  The null check filters the type predicate
				if(this_subject_type != null && this_object_type != null)
				{
					//Retrieve the original arc
					PredicateRule p = new PredicateRule(predicate.toString(), this_object_type, this_subject_type, PredicateRule.Direction.FORWARD);
    				row_index = subjects.get(this_object_type);
    				col_index = predicates.get(p);
    				
    				//Finally, this will filter blank nodes because one of the indicies will have returned null, but only because in real RDF,
    				//a literal is always a leaf.  Therefore the predicate rule <Literal, X, X> will always return null.
    				if(row_index != null && col_index != null)
    				{
	    				if(!prev_subject.equals(this_subject))
	    				{
	    					prev_subject = this_subject;
	    					outgoing.clear();
	    				}
	    				//This needs to be after the previous block due to clearing the outgoing set
	    				if(outgoing.contains(p))
	    				{
	    					if(mask[row_index][col_index] == Relation.ONE_TO_MANY)
	    						mask[row_index][col_index] = Relation.MANY_TO_MANY;
	    					else
	    						mask[row_index][col_index] = Relation.MANY_TO_ONE;
	    				}
	    				else
	    				{
	    					//Don't mark anything as one to one, that was already done in the forward pass
	    					outgoing.add(p);
	    				}
    				}
    				else
    				{
    					backward_anon++;
    				}
				}
				else
				{
					discarded_backward++;
				}
				

			}
			else
			{
				backward_anon++;
			}
		}
		

	}
	
	/*
	 * Adds in the auxillary statistics in their proper cells
	 */
	private void addAux()
	{
		Integer row_index = new Integer(0);
		Integer col_index = new Integer(0);
		Integer count = new Integer(0);
		
		for(PredicateRule p : aux_statistics.keySet())
		{
			row_index = subjects.get(p.getSubject());
			col_index = predicates.get(p);
			count = aux_statistics.get(p);
			
			if(frequencies[row_index][col_index] != 0)
			{
				System.out.println("Predicate naming conflict while merging aux:  ");
				p.print();
			}
			
			frequencies[row_index][col_index] = frequencies[row_index][col_index] + count;
			
			Relation r = mask[row_index][col_index];
			if(r == Relation.NONE)
				mask[row_index][col_index] = Relation.ONE_TO_MANY;
			else
				if(r != Relation.ONE_TO_MANY)
					mask[row_index][col_index] = Relation.MANY_TO_MANY;
		}
		
		for(PredicateRule p : aux_mask)
		{
			row_index = subjects.get(p.getSubject());
			col_index = predicates.get(p);
			
			mask[row_index][col_index] = Relation.MANY_TO_MANY;
		}
	}
	
	/*
	 * Since Jena forbids me to convert a Literal into a Resource, I use the test that something is literal if it does not contain "://" in it
	 */
	private boolean isLiteral(String s)
	{
		return !s.contains("://");
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
		
		System.out.println("Statements with anonymous subject:  " + anon_subject);
		System.out.println("Statements with anonymous object:  " + anon_object);
		System.out.println("Statements with anonymous subject and object:  " + anon_both);
		System.out.println("Statements with null object types:  " + null_obj_forward);
		System.out.println("Discarded in forward pass:  " + discarded_forward);
		System.out.println();
		System.out.println("Discarded in bacward pass due to blank node:  " + backward_anon);
		System.out.println("Discarded in backward pass for other reasons:  " + discarded_backward);
		System.out.println();
		System.out.println("Total statements processed:  " + total_statements);
	}
	
	public static void printStatement(Resource subject, Property predicate, RDFNode object)
	{
		if(subject.getLocalName() != null)
			System.out.print(subject.getLocalName());
		else
			System.out.print(subject.toString());
		System.out.print("  " + predicate.toString() + "  ");
        System.out.println(object.toString());
	}
	
	private static String suffix(String s)
	{
		if(s.contains("/"))
		{
			StringTokenizer st = new StringTokenizer(s, "/");
			String local_name = "";
			
			while(st.hasMoreElements())
				local_name = st.nextToken();
			
			return local_name;
		}
		
		return s;
	}
}
