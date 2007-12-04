package edu.mit.db.rstore.impl;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;



/**
 * The purpose of this class is to identify the data type for a the column of a table
 * @author Sergio
 */

public class DataTypeIdentifier 
{

	private Model rdfModel; 
	
	/**
	 * Postgres data types (reduced) 
	 */
	public enum DBType {
		integer,
		real,
		varchar_n, 
	    bool   
	}
			
	
	/**
	 * Constructor 
	 */
	public DataTypeIdentifier(Model rdfModel)
	{
		this.rdfModel=rdfModel;
	}
	
	/**
	 * This method maps the database types to strings that can be understood by postgres
	 */
	private String TypeToPostgres(DBType dbtype)
	{
		String returnString="";
		
		if (dbtype.equals(DBType.bool))
		{
			returnString= "boolean";
		}
		else if(dbtype.equals(DBType.integer)) 
		{
			returnString= "integer";
		}
		else if(dbtype.equals(DBType.real)) 
		{
			returnString= "real";
		}
		else if(dbtype.equals(DBType.varchar_n)) 
		{
			returnString= "varchar(n)";
		}
		
		
		return returnString;
	}
	
	/**
	 * This method maps types understood by postgres to database types
	 */
	private DBType PostgresToType(String type)
	{
		DBType returnDBType= null;
		
		if (type.equals("boolean"))
		{
			returnDBType= DBType.bool;
		}
		else if(type.equals("integer")) 
		{
			returnDBType= DBType.integer;
		}
		else if(type.equals("real")) 
		{
			returnDBType= DBType.real;
		}
		else if(type.equals("varchar(n)")) 
		{
			returnDBType= DBType.varchar_n;
		}
		
		
		return returnDBType;
	}
	
	/**
	 * This method groups the statements with same predicate and same subject into Lists
	 */
	
	private Vector<List<Statement>> GroupSubjectAndPredicates()
	{
		
		Vector <List<Statement>> vector= new Vector<List<Statement>>();
		HashMap <Statement, Boolean> progressMap= new HashMap <Statement, Boolean> ();  
		
		StmtIterator iter= this.rdfModel.listStatements();
		
		while(iter.hasNext())
		{			
			Statement st= iter.nextStatement();
			
			if (!progressMap.containsKey(st))
			{
				List<Statement> sameSubjectPred= new LinkedList<Statement>();
				
				Resource subject = st.getSubject();
				Property predicate = st.getPredicate();
				
						
				StmtIterator innerIter= this.rdfModel.listStatements();
				
				while (innerIter.hasNext())
				{
					Statement innerSt= innerIter.nextStatement();
					
					Resource innerSubject = st.getSubject();
					Property innerPredicate = st.getPredicate();
					
					if (subject.equals(innerSubject) && predicate.equals(innerPredicate))
					{
						sameSubjectPred.add(innerSt);
						progressMap.put(innerSt, true);
					}
					
				}
				
				vector.add(sameSubjectPred);
			}
			
			
		}
		
		return vector;
	}
	
	/**
	 * This method returns a hashmap that maps predicates with data types
	 */
	public HashMap<Statement, String> GetDataTypeMap ()
	{
				
		
		HashMap<Statement, String> dataTypeMap = new HashMap<Statement,String>();
		
		Vector<List<Statement>> vector = GroupSubjectAndPredicates();
		
		for (int i=0; i<vector.size(); i++)
		{
		
			List<Statement> list = vector.get(i);
			DBType parsedType= DBType.integer;
			String selectedType="";;
			
			for (int j=0; j< list.size(); j++)
			{
				Statement innerSt= list.get(j);
				
				//Get object type
				RDFNode objectnode=(RDFNode) innerSt.getObject();
				
				
				boolean isInt=false;
				boolean isFloat= false;
				boolean isBoolean=false;
				
				
				try
				{
					int objectint= Integer.parseInt(objectnode.toString());
					isInt=true;
				}
				catch(Exception ex)
				{
					isInt=false;
				
				}
				
				try
				{
					float objectfloat= Float.parseFloat(objectnode.toString());
					isFloat=true;
				}
				catch(Exception ex)
				{
					isFloat=false;
				
				}
				if(objectnode.toString().equals("true")||objectnode.toString().equals("false") )
				{
					isBoolean= true;
				}
				else
				{
					isBoolean= false;
				}
				
						
				String parsedTypeString="";
				
				if(isBoolean)
				{
					parsedTypeString= TypeToPostgres(DBType.bool);
				}
				else if(isInt)
				{
					parsedTypeString=  TypeToPostgres(DBType.integer);
				}
				else if(isFloat)
				{
					parsedTypeString=  TypeToPostgres(DBType.real);
				}
				else
				{
					parsedTypeString=  TypeToPostgres(DBType.varchar_n);
				}
				
				
				DBType oldDataType = parsedType;
				parsedType = PostgresToType(parsedTypeString);
				
				
					if(oldDataType.equals(DBType.integer) && !parsedType.equals(DBType.integer))
					{
						if(parsedType.equals(DBType.real))
						{
							// Upgrade data type to float
							selectedType=parsedTypeString;
						}
						else 
						{
							// Upgrade data type to string
							selectedType= TypeToPostgres(DBType.varchar_n);
						}
					}
					else if (oldDataType.equals(DBType.real)&& !parsedType.equals(DBType.real))
					{
						if(parsedType.equals(DBType.integer))
						{
							// Keep upgraded type
							selectedType= TypeToPostgres(DBType.real);
						}
						else 
						{
							// Upgrade data type to string
							selectedType= TypeToPostgres (DBType.varchar_n);
						}
					}
					else if (oldDataType.equals(DBType.bool)&& !parsedType.equals(DBType.bool))
					{
						
						
						// Upgrade data type to string
						selectedType= TypeToPostgres (DBType.varchar_n);
						
					}
				}
			
			
			
			for (int j=0; j< list.size(); j++)
			{
				dataTypeMap.put(list.get(j), selectedType);
			}
		}
				
		
		
		return dataTypeMap;
	}
	
	
	
	/**
	 * Print type for each predicate
	 */
	
	public static void PrintTypes(HashMap<Statement, String> dataTypeMap)
	{
		
		Set<Statement> keySet = dataTypeMap.keySet();
		Iterator<Statement> iter = keySet.iterator();
		
		while(iter.hasNext())
		{
			Statement st= iter.next();
			
			System.out.println("Subject: "+ st.getSubject().toString());
			System.out.println("Predicate: "+ st.getPredicate().toString());
			System.out.println("Object: "+ st.getObject().toString());
			
			System.out.println("Database type: " + dataTypeMap.get(st)+ "\n");
			
		}
	}
	
	
	
}
