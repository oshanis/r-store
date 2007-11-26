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
    	//Store.PrintModel(dataModel);
    	//Store.PrintTriples(dataModel.listStatements());
    	
    	Model schemaModel= myStore.CreateSchema();
    	//Store.PrintModel(schemaModel);  	
    	//Store.PrintTriples(schemaModel.listStatements());
    	
    	InfModel infModel= myStore.CreateInferenceModel();
    	//Store.PrintModel(infModel);
    	
    	StmtIterator iter= myStore.getIterator();  	
    	//Store.PrintTriples(iter);
    	
    	HashSet<String> typeSet= myStore.getSubjectTypes();
    	//Store.PrintTypes(typeSet);
    	
    	HashMap<String, String> typeMap= myStore.getSubjectTypeMap();
    	//Store.PrintTypeMap(typeMap);
    	
    	
    	
    	HashMap<String, Vector<LinkedList<String>>>  predicateTable= myStore.getPredicateTable();
    	Store.PrintPredicateTable(predicateTable);
            
    }
}

