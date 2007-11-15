package Test;

import java.util.*;

import edu.mit.db.rstore.impl.Store;
import com.hp.hpl.jena.rdf.model.*;

public class TestStore 
{
	
	public static void main (String args[]) {
    	
    	String path= "C:/Users/Sergio/Desktop/RStore/data/rdf/";
        
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	//Store.PrintModel(testModel);
    	
    	StmtIterator iter= myStore.getIterator();  	
    	//Store.PrintTriples(iter);
    	
    	HashSet<String> nsSet= myStore.getClassNamespaces();
    	//Store.PrintNamespaces(nsSet);
    	
    	HashMap<String, LinkedList<String>> predicateTable= myStore.getPredicateTable();
    	Store.PrintPredicateTable(predicateTable);
            
    }
}

