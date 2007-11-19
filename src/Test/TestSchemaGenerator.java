package Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.db.rstore.SchemaGenerator;
import edu.mit.db.rstore.impl.FrequencyCounter;
import edu.mit.db.rstore.impl.SchemaGeneratorImpl;
import edu.mit.db.rstore.impl.Store;

public class TestSchemaGenerator {
	
	public static void main(String[] args){
		
		String path= "data/rdf";
	     
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	Store.PrintTriples(myStore.getIterator());

    	FrequencyCounter f = new FrequencyCounter(myStore);
    	SchemaGeneratorImpl sg = new SchemaGeneratorImpl(f);

    	HashMap<String, HashMap<String, Integer>> t = sg.getLeftoversTable();
    	Collection<String> subjects = t.keySet();
		
		for(String s : subjects){
			System.out.println("*****" + s );
			HashMap<String, Integer> properties = t.get(s);
			Collection<String> propertyNames = properties.keySet();
			for (String p: propertyNames){
				System.out.print(p + " : " + properties.get(p) + " ");
			}
			System.out.println();
		}
		
	}
}
