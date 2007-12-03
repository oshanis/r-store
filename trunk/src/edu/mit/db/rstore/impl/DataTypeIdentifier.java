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
	public String TypeToPostgres(DBType dbtype)
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
	public DBType PostgresToType(String type)
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
	 * This method returns a hashmap that maps predicates with data types
	 */
	public HashMap<String, String> GetDataTypeMap ()
	{
		HashMap<String, String> dataTypeMap = new HashMap<String, String>();
				
		StmtIterator iter= this.rdfModel.listStatements();
		
		while(iter.hasNext())
		{
			Statement st= (Statement) iter.next();
			
			//Get predicate 
			Property prop =st.getPredicate();
			
			//Get object type
			RDFNode objectnode=(RDFNode) st.getObject();
			
			
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
			
			
			if(dataTypeMap.containsKey(prop.toString()))
			{
				//Compare data types
				DBType oldDataType = PostgresToType(dataTypeMap.get(prop.toString()));
				DBType parsedType = PostgresToType(parsedTypeString);
				
				if(oldDataType.equals(DBType.integer) && !parsedType.equals(DBType.integer))
				{
					if(parsedType.equals(DBType.real))
					{
						// Upgrade data type to float
						dataTypeMap.put(prop.toString(), parsedTypeString);
					}
					else 
					{
						// Upgrade data type to string
						dataTypeMap.put(prop.toString(), TypeToPostgres(DBType.varchar_n));
					}
				}
				else if (oldDataType.equals(DBType.real)&& !parsedType.equals(DBType.real))
				{
					if(parsedType.equals(DBType.integer))
					{
						// Keep upgraded type
						dataTypeMap.put(prop.toString(),dataTypeMap.get(prop.toString()));
					}
					else 
					{
						// Upgrade data type to string
						dataTypeMap.put(prop.toString(),TypeToPostgres (DBType.varchar_n));
					}
				}
				else if (oldDataType.equals(DBType.bool)&& !parsedType.equals(DBType.bool))
				{
					
					
					// Upgrade data type to string
					dataTypeMap.put(prop.toString(),TypeToPostgres (DBType.varchar_n));
					
				}
				
				
			}
			else
			{
				// Add property and data type
				dataTypeMap.put(prop.toString(), parsedTypeString);
				
			}
				
		}
		
		
		return dataTypeMap;
	}
	
	/**
	 * Print type for each predicate
	 */
	
	public static void PrintTypes(HashMap<String, String> dataTypeMap)
	{
		
		Set<String> keySet = dataTypeMap.keySet();
		Iterator<String> iter = keySet.iterator();
		while(iter.hasNext())
		{
			System.out.println(dataTypeMap.get(iter.next()));
		}
	}
	
	
	
}
