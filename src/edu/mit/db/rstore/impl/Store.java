package edu.mit.db.rstore.impl;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import edu.mit.db.rstore.*;
import com.hp.hpl.jena.util.FileManager;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This class implements the RDFStore interface
 * @author Sergio
 */


public class Store implements RDFStore 
{
	/*
	 * I put these things here so they could be referred to outside the class without introducing bugs such as capitalization.
	 * I need to know when something is a Seq (blank node) or a Literal, and didn't want to rely on writing the string out everywhere
	 * The String "Literal" occurred twice in your code.  I replaced it with the variable below.   -AM
	 */
	public static String LITERAL = "Literal";
	public static String SEQ = "Seq";
    
	private String directoryPath;
	
	private Model rdfModel;
	private Model schemaModel;
	private InfModel infModel;

	/**
	 * Constructor
	 */

	public Store( String path)
	{
		this.directoryPath= path;
		
		// create an empty model
        this.rdfModel = ModelFactory.createDefaultModel();
        this.schemaModel= ModelFactory.createDefaultModel();
		
	}
	
	/**
	 * Creates a Model from the rdf files in the indicated directory. Models are merged
	 * return Model: Merged model
	 */
	
	public Model CreateModel ()
	{
		
        File folder = new File(this.directoryPath+"rdf/");
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
	 * Creates a Model from the rdf files in the indicated directory. Models are merged
	 * return Model: Merged model
	 */
	
	public Model CreateSchema ()
	{
		
        File folder = new File(this.directoryPath+"schema/");
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
              this.schemaModel=schemaModel.union(tempModel);
            } 
        }
        
        return this.schemaModel;
	}
	
	/**
	 * Creates a Model from the rdf file and the rdfs schema.
	 * return Model: Inference Model
	 */
	
	public InfModel CreateInferenceModel ()
	{			
		this.infModel= ModelFactory.createRDFSModel(this.schemaModel, this.rdfModel);	     
        return this.infModel;
	}
	
	/**
	 * This is an accessor to the model.
	 * return Model: private model
	 */
	
	public InfModel GetInfModel ()
	{
		return this.infModel;
	}
	
	/**
	 * This is an accessor to the data model.
	 * return Model: private data model
	 */
	
	public Model GetDataModel ()
	{
		return this.rdfModel;
	}
	
	/**
	 * This is an accessor to the data model.
	 * return Model: private schema model
	 */
	
	public Model GetSchemaModel ()
	{
		return this.schemaModel;
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
            System.out.print("Object:" + object.toString() + "\n");
           
            
        }
	}
	
	/**
	 * Prints the Types to the console 
	 */
	
	public static void PrintTypes (HashSet<String> nspaces)
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
	 * Prints the predicate table to the console 
	 */
	
	public static void PrintPredicateTable (HashMap<String, Vector<LinkedList<String>>> predTable)
	{
		
        if (!predTable.isEmpty())
        {
        	Set<String> propertySet= predTable.keySet();
        	
        	Iterator<String> propertyIterator= propertySet.iterator();
        	
            while ( propertyIterator.hasNext())
            {
            	String key= propertyIterator.next();
            	Vector<LinkedList<String>> list= predTable.get(key);
            	
            	String output= "\n Predicate : " + key;
            	    
            	/*
            	LinkedList<String> subjectTypes= list.get(0);
            	LinkedList<String> objectTypes= list.get(1);
            	
            	for (int i=0; i< subjectTypes.size(); i++)
            	{
            		output+= "\n Subject Type : " + subjectTypes.get(i);
            	}
            	
            	for (int i=0; i< objectTypes.size(); i++)
            	{
            		output+= "\n Object Type : " + objectTypes.get(i);
            	}
            	*/
            	    
            	output+="\n";
            	for (int i=0; i< list.size(); i++)
            	{
            		LinkedList<String> l= list.get(i);
            		output+= "\n Subject Type : " + l.get(0);
            		output+= "\n Object Type : " + l.get(1);
            		output+="\n";
            		
            		
            	}
            	
            	System.out.println(output);
            }
           
        }
	}
	
	/**
	 * Prints the the predicate table to the console 
	 */
	
	public static void PrintTypeMap (HashMap<String, String> typeMap)
	{
		
        if (!typeMap.isEmpty())
        {
        	Set<String> subjectSet= typeMap.keySet();
        	
        	Iterator<String> subjectIterator= subjectSet.iterator();
        	
            while ( subjectIterator.hasNext())
            {
            	String key= subjectIterator.next();
            	String type= typeMap.get(key);
            	
            	String output= "\n Subject : " + key;
            	
            	
            	output+= "\n Type : " + type;
            	
            	
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
		
		StmtIterator iterOuter = this.rdfModel.listStatements();
		
		while(iterOuter.hasNext())
		{
			Statement stat= iterOuter.nextStatement();
			if(!qsubjects.contains(stat.getSubject()))
			{
				qsubjects.add(stat.getSubject());
				
				StmtIterator iterInner = this.rdfModel.listStatements();
				while(iterInner.hasNext())
				{
					Statement statInner= iterInner.nextStatement();
					if(statInner.getSubject().equals(stat.getSubject()))
					{
						orderedModel.add(statInner);
					}
				}
			}
		}
		
				
		return orderedModel.listStatements();
	}
	
	
	
	/**
	 * Generates an Iterator on the model where the statements are grouped by namespace:subject 
	 * return StmtIterator: Returns the statement iterator
	 */
	
	public StmtIterator getBackwardsIterator()
	{
		Model backOrderedModel=ModelFactory.createDefaultModel();
		Model tempModel=ModelFactory.createDefaultModel();
		//List all different qualified subjects
		LinkedList<Resource> qsubjects= new LinkedList<Resource>();
		
		StmtIterator iterOuter = this.rdfModel.listStatements();
		
		while(iterOuter.hasNext())
		{
			Statement stat= iterOuter.nextStatement();
			
			Resource subject= stat.getSubject();
			Property predicate= stat.getPredicate();
			RDFNode object= stat.getObject();
			
			//I changed this thing to differentiate between blank nodes and everything else, so the only remaining ambiguity is for Literals
			//My test will be if a subject.toString() contains "://", then its a URI.  Otherwise its a literal
			//Resource newResource= tempModel.createResource(object.toString()).addProperty(predicate, subject.toString());
			
			Resource newResource;
			if(object.isAnon())
				newResource = tempModel.createResource(((Resource)object).getId());
			else
				newResource = tempModel.createResource(object.toString());
			
			newResource.addProperty(predicate, subject.toString());
		}
		
		
		StmtIterator tempOuter = tempModel.listStatements();
		
		while(tempOuter.hasNext())
		{
			Statement stat= tempOuter.nextStatement();
			if(!qsubjects.contains(stat.getSubject()))
			{
				qsubjects.add(stat.getSubject());
								
				StmtIterator tempInner = tempModel.listStatements();
				while(tempInner.hasNext())
				{
					Statement statInner= tempInner.nextStatement();
					if(statInner.getSubject().equals(stat.getSubject()))
					{
						backOrderedModel.add(statInner);
					}
				}
			}
		}
		
				
		return backOrderedModel.listStatements();
	}
	
	/**
	 * These are the subject types
	 * 
	 * @return A HashSet of Strings which represent the types
	 */
	public HashSet<String> getSubjectTypes()
	{
		HashSet<String> types= new HashSet<String>();
		
		StmtIterator stmtIter= this.schemaModel.listStatements();
		
		while(stmtIter.hasNext())
		{
			Statement st= stmtIter.nextStatement();
			RDFNode object= st.getObject();
			if(object.isResource())
			{
				Resource objectRes= (Resource)object;
				String objectType= objectRes.getLocalName();
				if(objectType.equals("Class"))
				{
					if(!types.contains(st.getSubject().toString()))
					{
						types.add(st.getSubject().getLocalName().toString());
					}
				}
			}
			
			
		}
		
		
		return types;
	}
	

	
	
	/**
	 * Maps subjects to their types
	 * 
	 * @return A map from subjects to types
	 */
	public HashMap<String, String> getSubjectTypeMap()
	{
		HashMap<String, String> typeMap= new HashMap<String, String>();
		
		StmtIterator stmtIter= this.rdfModel.listStatements();
			
		
		while(stmtIter.hasNext())
		{
			Statement st= stmtIter.nextStatement();
			Resource subject= st.getSubject();
			
			if(subject!=null)
			{
				String subjectString="";
				
				if (subject.isURIResource())
				{
					subjectString= subject.getLocalName();
				}
				else
				{
					subjectString= subject.toString();
					
				}
				
				String propertyString= st.getPredicate().getLocalName().toString();
							
				RDFNode object= st.getObject();
				
				if(object.isURIResource())
				{
					Resource objectResource= (Resource)object;
					String objectString= objectResource.getLocalName().toString();
					
					
				
				
					if(!typeMap.containsKey(subjectString) && propertyString.equals("type"))
					{
						typeMap.put(subjectString, objectString );
					}
				}
				else
				{
					String objectString= object.toString();
					
					if(!typeMap.containsKey(subjectString) && propertyString.equals("type"))
					{
						typeMap.put(subjectString, objectString );
					}
				}
			}
		}
		
		
		return typeMap;
	}
	

	
	/**
	 * Given a type, returns a set of subjects that belong to this type
	 * 
	 * @return A set of subjects
	 */
	public HashSet<String> getSubjectsFromType( String type )
	{
		HashMap<String,String> typeMap= getSubjectTypeMap();
		
		HashSet<String> subjects = new HashSet<String>();
		
		Set <String> keySet=typeMap.keySet();
		
		Iterator<String> keyIterator= keySet.iterator();
    	
        while ( keyIterator.hasNext())
        {
        	String key= keyIterator.next();
			String keyType= typeMap.get(key);
			
			if(keyType.equals(type))
			{
				subjects.add(key);
			}
			
		}
		
		return subjects;
	}
	
	/**
	 * Maps qualified subjects to their types
	 * 
	 * @return A map from qualified subjects to types
	 */
	public HashMap<String, String> getQualifiedSubjectTypeMap()
	{
		HashMap<String, String> typeMap= new HashMap<String, String>();
		
		StmtIterator stmtIter= this.rdfModel.listStatements();
			
		
		while(stmtIter.hasNext())
		{
			Statement st= stmtIter.nextStatement();
			Resource subject= st.getSubject();
			
			if(subject!=null)
			{
				String subjectString="";
								
				subjectString= subject.toString();
					
				
				
				String propertyString= st.getPredicate().getLocalName().toString();
							
				RDFNode object= st.getObject();
				
				if(object.isURIResource())
				{
					Resource objectResource= (Resource)object;
					String objectString= objectResource.getLocalName().toString();
					
					
				
				
					if(!typeMap.containsKey(subjectString) && propertyString.equals("type"))
					{
						typeMap.put(subjectString, objectString );
					}
				}
				else
				{
					String objectString= object.toString();
					
					if(!typeMap.containsKey(subjectString) && propertyString.equals("type"))
					{
						typeMap.put(subjectString, objectString );
					}
				}
			}
		}
		
		
		return typeMap;
	}
	
	/**
	 * Given a type, returns a set of qualified subjects that belong to this type
	 * 
	 * @return A set of qualified subjects
	 */
	public HashSet<String> getQualifiedSubjectsFromType( String type )
	{
		HashMap<String,String> typeMap= getQualifiedSubjectTypeMap();
		
		HashSet<String> subjects = new HashSet<String>();
		
		Set <String> keySet=typeMap.keySet();
		
		Iterator<String> keyIterator= keySet.iterator();
    	
        while ( keyIterator.hasNext())
        {
        	String key= keyIterator.next();
			String keyType= typeMap.get(key);
			
			if(keyType.equals(type))
			{
				subjects.add(key);
			}
			
		}
		
		return subjects;
	}
	
	/**
	 * Given a subject, returns its type
	 * 
	 * @return a string that indicates the type
	 */
	public String getTypeFromSubjects(String subject )
	{
		String type="";
		HashMap<String,String> typeMap= getSubjectTypeMap();
		type=typeMap.get(subject);
		
		return type;
	}
	
	
	/**
	 * Prints subjects and their types
	 * 
	 * @return void
	 */
	
	public void PrintSubjectsAndTypes ( )
	{		
		
		HashSet<String> types = getSubjectTypes();
		
		Iterator<String> typeIterator= types.iterator();
		
		while (typeIterator.hasNext())
		{
			String type= typeIterator.next();
			HashSet <String> subjectSet= getSubjectsFromType (type);
			Iterator<String> subjectIterator= subjectSet.iterator();

			System.out.println("Type: " + type);
			
			while(subjectIterator.hasNext())
			{
				System.out.println("Subject: " + subjectIterator.next());
				
			}
			
		}
	}
	
	/**
	 * This data structure encodes a table which maps predicate types to the types of their subjects and objects.  Each
	 * LinkedList will be non-null, and contain precisely two Strings.  The first String will correspond to the subject type,
	 * the second String will correspond to the object type.  Both the subject and object types must exist in the HashSet
	 * returned by getClassNamespaces, and the predicate type must not occur in that HashSet.
	 * 
	 * @return A mapping from predicate type to subject and object type in the form of a HashMap.
	 */
	public HashMap<String, Vector<LinkedList<String>>> getPredicateTable()
	{
		HashMap<String, Vector<LinkedList<String>>> predicateTable= new HashMap<String, Vector<LinkedList<String>>>();
		HashMap<String, String> typeMap= this.getSubjectTypeMap();
		
		StmtIterator stmtIter= this.rdfModel.listStatements();
		
		
		while( stmtIter.hasNext())
		{
			Statement st= stmtIter.nextStatement();
			
			Resource subj= st.getSubject();
			Property prop=st.getPredicate();
			RDFNode obj= st.getObject();
			
			
			
			String subjectType="";
			
			if(subj!=null)
			{
				String subjectString="";
				
				if (subj.isURIResource())
				{
					subjectString= subj.getLocalName();
				}
				else
				{
					subjectString= subj.toString();
					
				}
				
				if(typeMap.containsKey(subjectString))
				{
					subjectType= typeMap.get(subjectString);
				}
				else
				{
					//First line that I modified
					subjectType=LITERAL;
				}
			}
		
			String objectType="";
			
			if(obj!=null)
			{
				String objectString="";
				
				if (obj.isURIResource())
				{
					Resource objResource= (Resource)obj;
					
					if(objResource.getLocalName()== null)
					{
						objectString= obj.toString();
					}
					else
					{
						objectString= objResource.getLocalName();
					}
				}
				else
				{
					objectString= obj.toString();
					
				}
				
				
				if(typeMap.containsKey(objectString))
				{
					objectType= typeMap.get(objectString);
				}
				else
				{
					//Second and last line that I modified
					objectType=LITERAL;
				}
				
				
			}
					
			
			
			if(!predicateTable.containsKey(prop.toString()))
			{
				/*LinkedList<String> subjectTypes= new LinkedList<String>();
				
				if(!subjectTypes.contains(subjectType))
				{
					subjectTypes.add(subjectType);
				}
				
				LinkedList<String> objectTypes = new LinkedList <String>();
				objectTypes.add(objectType);
				
				if(!objectTypes.contains(objectType))
				{
					objectTypes.add(objectType);
				}
				
				Vector<LinkedList<String>> vector= new Vector<LinkedList<String>>();
				
				vector.add(subjectTypes);
				vector.add(objectTypes);
			*/	
				
				LinkedList<String> resourceTypes= new LinkedList<String>();
				resourceTypes.add(subjectType);
				resourceTypes.add(objectType);
				
				Vector<LinkedList<String>> vector= new Vector<LinkedList<String>>();
				vector.add(resourceTypes);
				predicateTable.put(prop.toString(), vector);
			
				
				
				
			}
			else
			{
				Vector<LinkedList<String>> vector= predicateTable.get(prop.toString());
				
				/*LinkedList<String> subjectTypes= vector.get(0);
				
				if(!subjectTypes.contains(subjectType))
				{
					subjectTypes.add(subjectType);
				}
				
				vector.set(0, subjectTypes);
				
				LinkedList<String> objectTypes= vector.get(1);
				
				if(!objectTypes.contains(objectType))
				{
					objectTypes.add(objectType);
				}
				
				vector.set(1, objectTypes);
				*/
				
				
				LinkedList<String> resourceTypes= new LinkedList<String>();
				resourceTypes.add(subjectType);
				resourceTypes.add(objectType);
				vector.add(resourceTypes);
				
				predicateTable.put(prop.toString(),vector);
				
			}
			
			
		}
		
		
	
		
				
		return predicateTable;
	}
	
    
}
