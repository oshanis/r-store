package edu.mit.db.rstore.impl;

import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.*;

import edu.mit.db.rstore.SchemaGenerator;

/**
 * Class which generates the RDB schema out of the RDF schema
 * 
 * This class bases the decision about what relational schema should be represented
 * in the database based on the RDF Schema associated with the RDF data
 * 
 * Step 1:
 * Consider all the Classes. Filter the classes which are the leaf classes (i.e the Classes which are 
 * not subclassed from any other Class). The algorithm assumes that these classes are likely to be 
 * relations.
 * 
 * Step 2:
 * Consider all the Properties. If a property has a range and a domain (or even multiple ranges and domains),
 * the algorithm deduces that this property could represent a cardinality relationship in the RDB Schema
 * The distinction as to ONE-TO-ONE, ONE-TO-MANY and MANY-TO-MANY is made based on the following criteria:
 * 
 * ONE-TO-ONE: 
 * Basically, all the subclasses identified in step 1 falls in to this category. 
 * If the range of the property is an RDF literal (or any similar category), the algorithm determines that
 * this property could very well fit as an attribute in the the class(es) given in the domain(s). 
 * 
 * ONE-TO-MANY: 
 * If the range of the property matches an RDF collection attribute such as rdf:seq, the algorithm assumes that 
 * there is a ONE-TO-MANY relationship, based on the property considered. Therefore, another relation is added 
 * which takes the form of {DOMAIN CLASS}{propertyName}. 
 * 
 * MANY-TO-MANY:
 * If both the range and the domain of the property matches to classes which are already identified as a relation
 * in the RDF, create a MANY-TO-MANY relations such as {DOMAIN CLASS}{RANGE CLASS}
 * 
 * @author oshani
 *
 */
public class RDFSBasedSchemaGenerator implements SchemaGenerator {

	//The final schema the 'schema generating' algorithm produces
	private LinkedList<PropertyTable> schema; 
	
	private Model schemaModel;
	
	// Contains all the tables identified
	private HashMap<String, PropertyTable> tables = new HashMap<String, PropertyTable>();
	
	//Contains the table and a list of Foreign key string which would be useful in creating the schema
	private HashMap<String, String> foreignKeys = new HashMap<String, String>();

	/**
	 * Constructor
	 */
	public RDFSBasedSchemaGenerator(Store store) {
		this.schema = new LinkedList<PropertyTable>();
		schemaModel= store.CreateSchema();
		makeSchema();

	}

	//Modified return type and set return value to null for now  -AM
	public LinkedList<PropertyTable> getSchema() {
		return schema;
	}
	
	
	public void makeSchema(){
		
     	HashSet<Resource> superSubjectSet = new HashSet<Resource>();
    	HashSet<Resource> subjectSet = new HashSet<Resource>();
    	HashSet<String> tableNames = new HashSet<String>();
    	
    	//Create the tables based on the subClass relationships

    	//This is the ONE-TO-ONE case
		
    	NodeIterator superNodes = schemaModel.listObjectsOfProperty(RDFS.subClassOf);
    	while (superNodes.hasNext()){
    		Resource superClass = (Resource)superNodes.next();
    		superSubjectSet.add(superClass);
    	}
    	
    	ResIterator sc = schemaModel.listSubjectsWithProperty(RDFS.subClassOf);
    	while (sc.hasNext()){
    		Resource subClass = sc.nextResource();
    		if (!superSubjectSet.contains(subClass)){
    			subjectSet.add(subClass);
    			String s = subClass.getLocalName();
				tables.put(s, new PropertyTable("Table_" + s, subClass.getURI(), "PKey_" + s));
				tableNames.add(s);
				//Attributes are added later as and when they are determined by the RDF ranges
    		}
    	}
    	
    	//Create the tables based on the DomainRange pair
    	ResIterator domainSubs = schemaModel.listSubjectsWithProperty(RDFS.domain);
    	
    	while (domainSubs.hasNext()){

    		Resource domainSub = domainSubs.nextResource();
    		
    		StmtIterator iter = domainSub.listProperties();
    		
    		//There can be multiple domains for a single statement, hence the vector
    		Vector<Resource> domain = new Vector<Resource>();
    		Resource range = null;
    		
    		while (iter.hasNext()){
    		
    			Statement st = (Statement)iter.next();
    			Property prop = st.getPredicate();
    			if (prop.equals(RDFS.range) ){
    				range = (Resource) st.getObject();
    			}
    			if (prop.equals(RDFS.domain)){
    				domain.add((Resource) st.getObject());
    			}
    		}
    				
    		//FIXME Check this for statements which would have other RDF stuff which we are not
    		// interested in including in our schema structure
			
    		//This is the MANY-TO-MANY case

    		if (domain.size() > 0 && range != null && !(range.getLocalName().equals("Literal")) 
					&& !(range.getLocalName().equals("Seq"))){
				for (int i=0; i< domain.size(); i++){
					String d = domain.get(i).getLocalName();
					String r = range.getLocalName();
					if (tableNames.contains(d) && tableNames.contains(r)){
						//Have to account for foreign keys!
						String dType = domain.get(i).getLocalName();
						String rType = range.getLocalName();
						String t = d+"_"+r;
						tableNames.add(t);
						String pred = domainSub.getLocalName();
						PropertyTable p = new ManyToManyTable("Table_" +t,dType, "Pkey_" +d, rType, "Pkey_" + r, pred );
						tables.put(t, p);
		    		
					}
				}				
			}
    		
    		//FIXME It would be wise to add support for subClass relationships here
    		//For eg: <rdfs:domain rdf:resource="#Person"/> instead of having 2 	      
    	    // separate domains for Student and Teacher

    		//Add the attributes in tables which are ONE-TO-ONE
    		else if (domain.size() > 0  && (range.getLocalName().equals("Literal"))){
				for (int i=0; i< domain.size(); i++){
					String t = domain.get(i).getLocalName();
					PropertyTable p =  tables.get(t);
					if (p != null){
						p.addAttribute(domainSub.getLocalName(), "col_"+domainSub.getLocalName());
					}	
					tables.put(t, p);	
				}
    		}
    		
    		//This is the ONE-TO-MANY case
    		
    		else if (domain.size() > 0  &&  (range.getLocalName().equals("Seq"))){
				for (int i=0; i< domain.size(); i++){
					String d = domain.get(i).getLocalName();
					String dType = domain.get(i).getLocalName();
					String r = domainSub.getLocalName();
					String rType = domainSub.getLocalName();
					String t = d+"_"+r;
					tableNames.add(t);
					String pred = domainSub.getLocalName();
					PropertyTable p = new OneToManyTable("Table_" +t,dType, "Pkey_" +d, rType, "Pkey_" + r, pred );
					tables.put(t, p);
				}
    		}

    	}
    	
    	Iterator it = tables.keySet().iterator();
    	while (it.hasNext()){
    		String tableName = (String)it.next();
    		PropertyTable p = tables.get(tableName);
    		if (p!= null){
    			if (p instanceof ManyToManyTable) {
    				ManyToManyTable mmt = (ManyToManyTable)p;
    				mmt.print();
    			}
    			else{
    				p.print();
    			}
    		}
    	}
	}
	
	
	/**
	 * Iterate over all the Table objects in the tables vector and output the 'schema' in a format
	 * the RDFBrowser would understand
	 */
	public void constructSchema(){
    	Iterator it = tables.keySet().iterator();
    	while (it.hasNext()){
    		String tableName = (String)it.next();
    		PropertyTable p = tables.get(tableName);
    		if (p!= null)
	    		schema.add(p);
    	}
	}
}

