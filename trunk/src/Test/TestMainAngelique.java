package Test;

import java.util.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import edu.mit.db.rstore.impl.*;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.mit.db.rstore.impl.Store;

public class TestMainAngelique 
{
	//Dumps a frequency generator
	public static void main(String[] args) throws IOException 
	{	
		String path= "F:/Workspace/rstore/data/";
     
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	Model schemaModel= myStore.CreateSchema();
    	InfModel infModel= myStore.CreateInferenceModel();
    	
    	/*System.out.println("Forward statements:");
    	StmtIterator s = myStore.getIterator();
    	while(s.hasNext())
    	{
    		Statement next = s.nextStatement();
    		Resource subject = next.getSubject();
    		Property predicate = next.getPredicate();
    		RDFNode object = next.getObject();
    		
    		FrequencyCounter.printStatement(subject, predicate, object);
    	}*/
    	
    	/*HashMap<String, Vector<LinkedList<String>>> ptable = myStore.getPredicateTable();
    	for(String s : ptable.keySet())
    	{
    		System.out.println("Key:  " + s);
    		Vector<LinkedList<String>> vals = ptable.get(s);
    		if(vals != null)
    		{
    			for(LinkedList<String> v : vals)
    			{
    				for(String t : v)
    					System.out.println(t);
    				System.out.println();
    			}
    		}
    	}*/
    	
    	StatisticalSchemaGenerator sg = new StatisticalSchemaGenerator(myStore);
    	
	}

}
