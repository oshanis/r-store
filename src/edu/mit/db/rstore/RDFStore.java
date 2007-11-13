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
	 * These are the namespace prefixes that are at some point used either as a subject or as an object, but not as a predicate. 
	 * 
	 * @return A HashSet of Strings which represent the namespaces, which can be interpreted as class names or types.
	 */
	public HashSet<String> getClassNamespaces();
	
	/**
	 * This data structure encodes a table which maps predicate namespaces to the namespaces of their subjects and objects.  Each
	 * LinkedList will be non-null, and contain precisely two Strings.  The first String will correspond to the subject namespace,
	 * the second String will correspond to the object namespace.  Both the subject and object namespaces must exist in the HashSet
	 * returned by getClassNamespaces, and the predicate namespace must not occur in that HashSet.
	 * 
	 * @return A mapping from predicate namespaces to subject and object namespaces in the form of a HashMap.
	 */
	public HashMap<String, LinkedList<String>> getPredicateTable();
	
}
