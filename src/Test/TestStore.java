package Test;

import java.util.*;

import edu.mit.db.rstore.impl.Store;
import com.hp.hpl.jena.rdf.model.*;

public class TestStore 
{
	
	public static void main (String args[]) {
    	
    	String path= "data/";
        
		
    	Store myStore = new Store (path);
    	Model dataModel= myStore.CreateModel();
//    	Store.PrintModel(dataModel);
//    	Store.PrintTriples(dataModel.listStatements());
    	
    	Model schemaModel= myStore.CreateSchema();
    	
    	//Store.PrintModel(schemaModel);  	
    	//Store.PrintTriples(schemaModel.listStatements());
    	
    	InfModel infModel= myStore.CreateInferenceModel();
    	//Store.PrintModel(infModel);
    	
    	//StmtIterator iter= myStore.getIterator();
//    	StmtIterator iter= myStore.getBackwardsIterator();
//    	Store.PrintTriples(iter);

    	//Just Testing --Oshani
//    	HashSet<String> hs = myStore.getSubjectsFromType("name");
//    	Iterator i = hs.iterator();
//    	while (i.hasNext())
//    		System.out.println((String)i.next());
    	
    	HashSet<String> typeSet= myStore.getSubjectTypes();
    	//Store.PrintTypes(typeSet);
    	
    	HashMap<String, String> typeMap= myStore.getSubjectTypeMap();
    	//Store.PrintTypeMap(typeMap);
    	    	   	
    	HashMap<String, Vector<LinkedList<String>>>  predicateTable= myStore.getPredicateTable();
    	//Store.PrintPredicateTable(predicateTable);
          
    	//myStore.PrintSubjectsAndTypes();
    }
}

