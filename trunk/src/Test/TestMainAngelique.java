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
    	String path= "F:/Workspace/rstore/data/rdf/";
        
    	Store myStore = new Store (path);
    	Model testModel= myStore.CreateModel();
    	//Store.PrintModel(testModel);
    	
    	FrequencyCounter f = new FrequencyCounter(myStore);
	}

}
