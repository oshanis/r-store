package Test;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.db.rstore.SchemaGenerator;
import edu.mit.db.rstore.impl.FrequencyCounter;
import edu.mit.db.rstore.impl.RDFSBasedSchemaGenerator;
import edu.mit.db.rstore.impl.StatisticalSchemaGenerator;
import edu.mit.db.rstore.impl.Store;

public class TestSchemaGenerator {
	
	public static void main(String[] args) throws IOException{

    	String path= "data/";
        
    	Store myStore = new Store (path);

   // 	StatisticalSchemaGenerator s = new StatisticalSchemaGenerator(myStore);
		RDFSBasedSchemaGenerator s = new RDFSBasedSchemaGenerator(myStore);
		
	}
}
