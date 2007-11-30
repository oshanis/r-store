package Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.mit.db.rstore.impl.Store;
import edu.mit.db.rstore.impl.DataTypeIdentifier;;

public class TestDataTypeIdentifier 
{
	public static void main (String args[]) {
    	
    	String path= "data/rdf/";
        
		
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	    	
    	DataTypeIdentifier dataTypeIdentifier= new DataTypeIdentifier(testModel);
    	HashMap<String,String> dataTypeMap= dataTypeIdentifier.GetDataTypeMap();
    	DataTypeIdentifier.PrintTypes(dataTypeMap);
            
    }
}