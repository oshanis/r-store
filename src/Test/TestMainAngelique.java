package Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import edu.mit.db.rstore.impl.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.mit.db.rstore.impl.Store;

public class TestMainAngelique 
{
	//Dumps a frequency generator
	public static void main(String[] args) 
	{
		
		//Let's always try to use relative paths. (I changed the path variable here) 
		//Otherwise we would have to change the code back and forth 
		//when we commit and get an update in the SVN. - Oshani
    	
		String path= "data/rdf/";
     
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	//Store.PrintModel(testModel);
    	
    	FrequencyCounter f = new FrequencyCounter(myStore);
	}

}
