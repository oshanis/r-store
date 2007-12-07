package Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

import edu.mit.db.rstore.DBPopulator;
import edu.mit.db.rstore.impl.RDFSBasedDBPopulator;
import edu.mit.db.rstore.impl.PropertyTable;
import edu.mit.db.rstore.impl.RDFSBasedSchemaGenerator;
import edu.mit.db.rstore.impl.StatisticalDbPopulator;
import edu.mit.db.rstore.impl.StatisticalSchemaGenerator;
import edu.mit.db.rstore.impl.Store;

public class TestDBPopulator {
	
	public static void main(String args[]) throws ClassNotFoundException, SQLException, IOException{
	
    	String path= "data/";
		
    	Store myStore = new Store (path);
    	Model dataModel= myStore.CreateModel();
    	Model schemaModel= myStore.CreateSchema();
    	InfModel infModel= myStore.CreateInferenceModel();

    	StatisticalSchemaGenerator schemaGenerator = new StatisticalSchemaGenerator(myStore);

//		RDFSBasedSchemaGenerator schemaGenerator = new RDFSBasedSchemaGenerator();
//		schemaGenerator.createInitialSchema();
//		schemaGenerator.constructSchema();

    	LinkedList<PropertyTable> schemas = schemaGenerator.getSchema();
		
//		DBPopulator populator = new RDFSBasedDBPopulator(schemas, myStore);
		
		DBPopulator populator = new StatisticalDbPopulator(schemas, myStore);
		populator.createTables();
		
//		populator.insertValues();
		
	}

}
