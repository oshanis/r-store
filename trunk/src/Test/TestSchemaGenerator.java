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

		SchemaGeneratorImpl s = new SchemaGeneratorImpl();
		s.createInitialSchema();
		
	}
}
