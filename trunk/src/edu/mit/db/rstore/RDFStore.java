package edu.mit.db.rstore;

import java.util.*;

import com.hp.hpl.jena.rdf.model.StmtIterator;

public interface RDFStore 
{
	/**
	 * This is the object that the Schema generator uses to iterate over the triples.  Assuming the triples are stored in the form
	 * <namespace:subject, namespace:predicate, namespace:object>, this iterator must group all statements with the same vaule for
	 * namespace:subject consecutively.  They need not be sorted, but they must be in consecutive blocks.
	 * 
	 * @return A StmtIterator which can be used to iterate over all the triples in the RDF Store
	 */
	public StmtIterator getIterator();
	
	/**
	 * These are the subject types
	 * 
	 * @return A HashSet of Strings which represent the types
	 */
	public HashSet<String> getSubjectTypes();
	
	
	/**
	 * Maps subjects to their types
	 * 
	 * @return A map from subjects to types
	 */
	public HashMap<String, String> getSubjectTypeMap();
	
	/**
	 * This data structure encodes a table which maps predicate types to the types of their subjects and objects.  Each
	 * LinkedList will be non-null, and contain precisely two Strings.  The first String will correspond to the subject type,
	 * the second String will correspond to the object type.  Both the subject and object types must exist in the HashSet
	 * returned by getClassNamespaces, and the predicate type must not occur in that HashSet.
	 * 
	 * @return A mapping from predicate type to subject and object type in the form of a HashMap.
	 */
	public HashMap<String, Vector<LinkedList<String>>> getPredicateTable();
	
}
