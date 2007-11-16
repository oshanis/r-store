package edu.mit.db.rstore.impl;
import java.util.*;

/*
 * Written by Sergio
 */

import com.hp.hpl.jena.rdf.model.*;
import edu.mit.db.rstore.*;
import com.hp.hpl.jena.util.FileManager;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


public class Store implements RDFStore 
{

    
	private String directoryPath;
	private Model rdfModel;

	/**
	 * Constructor
	 */

	public Store( String path)
	{
		this.directoryPath= path;
		// create an empty model
        this.rdfModel = ModelFactory.createDefaultModel();
		
	}
	
	/**
	 * Creates a Model from the rdf files in the indicated directory. Models are merged
	 * return Model: Merged model
	 */
	
	public Model CreateModel ()
	{
		
        File folder = new File(this.directoryPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
        	
        	// create an empty model
            Model tempModel = ModelFactory.createDefaultModel();
        	
            if (listOfFiles[i].isFile()) 
            {
        	  InputStream in = FileManager.get().open( listOfFiles[i].getAbsolutePath() );
              
        	  if (in == null) {
                  throw new IllegalArgumentException( "File: " + listOfFiles[i].getName() + " not found");
              }
              
              // read the RDF/XML file
        	               
        	  tempModel.read(in, "");
              this.rdfModel=rdfModel.union(tempModel);
            } 
        }
        
        return this.rdfModel;
	}
	
	/**
	 * Prints the model to the console 
	 */
	
	public static void PrintModel(Model printModel)
	{
		// write it to standard out
		printModel.write(System.out);
	}
	
	/**
	 * Prints the triples to the console 
	 */
	
	public static void PrintTriples (StmtIterator iter)
	{
		// print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();         // get next statement
            Resource  subject   = stmt.getSubject();   // get the subject
            Property  predicate = stmt.getPredicate(); // get the predicate
            RDFNode   object    = stmt.getObject();    // get the object
            
            System.out.print("Subject: " + subject.toString());
            System.out.print(" Predicate: " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print("Object: " + object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
            System.out.println(" .");
        }
	}
	
	/**
	 * Prints the namespaces to the console 
	 */
	
	public static void PrintNamespaces (HashSet<String> nspaces)
	{
		
        if (!nspaces.isEmpty())
        {
        	Iterator <String> nsIterator= nspaces.iterator();
        	
            while ( nsIterator.hasNext())
            {
            	System.out.println( nsIterator.next());
            }
           
        }
	}
	
	/**
	 * Prints the the predicate table to the console 
	 */
	
	public static void PrintPredicateTable (HashMap<String, LinkedList<String>> predTable)
	{
		
        if (!predTable.isEmpty())
        {
        	Set<String> propertySet= predTable.keySet();
        	
        	Iterator<String> propertyIterator= propertySet.iterator();
        	
            while ( propertyIterator.hasNext())
            {
            	String key= propertyIterator.next();
            	LinkedList<String> list= predTable.get(key);
            	
            	String output= "\n Predicate : " + key;
            	if(list.size()>0)
            	{
            		output+= "\n Subject ns : " + list.get(0);
            	}
            	if(list.size()>1)
            	{
            		output+= "\n Object ns : " + list.get(1);
            	}
            	
            	System.out.println(output);
            }
           
        }
	}
	
	/**
	 * Generates an Iterator on the model where the statements are grouped by namespace:subject 
	 * return StmtIterator: Returns the statement iterator
	 */
	
	public StmtIterator getIterator()
	{
		Model orderedModel=ModelFactory.createDefaultModel();
		
		//List all different qualified subjects
		LinkedList<Resource> qsubjects= new LinkedList<Resource>();
		
		ResIterator qsubjectIterator= this.rdfModel.listSubjects();
		
		while(qsubjectIterator.hasNext())
		{
			if(!qsubjects.contains(qsubjectIterator.next()))
			{
				qsubjects.add((Resource)qsubjectIterator.next());
			}
			
		}
		
		for (int i=0; i< qsubjects.size(); i++)
		{
			Property property=null;
			RDFNode rdfNode=null;
			StmtIterator stmtIter = this.rdfModel.listStatements(new SimpleSelector(qsubjects.get(i),property, rdfNode));
			if(stmtIter.hasNext() )
			{	
				orderedModel.add(stmtIter);
			
			}
		}
		
		return orderedModel.listStatements();
	}
	
	/**
	 * These are the namespace prefixes that are at some point used either as a subject or as an object, but not as a predicate. 
	 * 
	 * @return A HashSet of Strings which represent the namespaces, which can be interpreted as class names or types.
	 */
	public HashSet<String> getClassNamespaces()
	{
		HashSet<String> classNamespaces= new HashSet<String>();
		
		
		ResIterator qsubjectIterator= this.rdfModel.listSubjects();
		
		while (qsubjectIterator.hasNext())
		{
			Resource qsubject = (Resource)qsubjectIterator.next();
			
			//Changed this:  Need to make sure never to add nulls...  -AM
			if(qsubject.getNameSpace() != null)
				classNamespaces.add(qsubject.getNameSpace());
			
		}
		
		NodeIterator qobjectIterator= this.rdfModel.listObjects();
		
		while (qobjectIterator.hasNext())
		{
			RDFNode qobject = (RDFNode) qobjectIterator.next();
			if(qobject.isURIResource())
			{
				Resource qobjectResource= (Resource)qobject;
				
				classNamespaces.add(qobjectResource.getNameSpace());
				
			}
		}
		
		return classNamespaces;
	}
	
	
	/**
	 * This data structure encodes a table which maps predicate namespaces to the namespaces of their subjects and objects.  Each
	 * LinkedList will be non-null, and contain precisely two Strings.  The first String will correspond to the subject namespace,
	 * the second String will correspond to the object namespace.  Both the subject and object namespaces must exist in the HashSet
	 * returned by getClassNamespaces, and the predicate namespace must not occur in that HashSet.
	 * 
	 * @return A mapping from predicate namespaces to subject and object namespaces in the form of a HashMap.
	 */
	public HashMap<String, LinkedList<String>> getPredicateTable()
	{
		HashMap<String, LinkedList<String>> predicateTable= new HashMap<String, LinkedList<String>>();
		
		StmtIterator it= this.rdfModel.listStatements();
		
		while (it.hasNext())
		{
			Statement st= (Statement) it.next();
			
			//Get predicate namespaces
			Property prop =st.getPredicate();
			String propString= prop.toString();
			
			//Get subject  namespaces
			Resource res= st.getSubject();
			String resString=res.getNameSpace();
			
			if(resString==null)
			{
				resString="";
			}
			
			//Get object namespace
			RDFNode objectnode=(RDFNode) st.getObject();
			String objectString="";
			
			if(objectnode.isURIResource())
			{
				Resource objectresource= (Resource)objectnode;
				objectString= objectresource.getNameSpace();
				
			}
	
			//Attn:Sergio, I am getting compilation errors here!,  So I commented out - Oshani
			
			//Hey I need that there :)  It compiles just fine.  Check your compiler settings, make sure its compatible with java 6.0 -AM
			
			//Usual SE advice is that we should be backward compatible. :)
			//Anyways, I changed my JDK - Oshani
			
			if(!predicateTable.containsKey(propString) && !objectString.isEmpty()  && !resString.isEmpty())
			{
				LinkedList <String> list= new LinkedList <String>();
				list.add(resString);
				list.add(objectString);				
				
				predicateTable.put(propString, list);
			}
		}
		
		return predicateTable;
	}
	
    
}
