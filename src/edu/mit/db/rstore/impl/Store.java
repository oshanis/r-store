package edu.mit.db.rstore.impl;


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
		
		return classNamespaces;
	}
	
	public HashMap<String, LinkedList<String>> getPredicateTable()
	{
		HashMap<String, LinkedList<String>> table= new HashMap<String, LinkedList<String>>();
		return table;
	}
	
    
}
