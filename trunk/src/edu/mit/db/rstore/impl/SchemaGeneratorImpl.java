package edu.mit.db.rstore.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import edu.mit.db.rstore.SchemaGenerator;

/**
 * Class which generates the schema out of the RDF store

 * FIXME: The following algorithm suffers from severe lack of optimization!
 * 
 * It assumes a table of the following form being passed into the method:
 * 
 * (Please note that the numbers used in the table are arbitrary and is only used for the
 * purpose of visualizing the algorithm only.)
 * 
 * 	Angelika's Frequency Table:
 * 
 *  			|Properties							   	|
 * ------------------------------------------------------
 *  Subjects	|P1		|P2		|P3		|P4		|P5		|
 * ------------------------------------------------------
 *  A			|10		|20		|30		|10		|0		|
 *  B			|0		|0		|0		|20		|25		|
 *  C			|0		|10		|20		|10		|10		|
 * ------------------------------------------------------
 *  
 *  The idea is that each of the tables could end up being a table and each of the properties
 *  will be the columns in any of the tables constructed.
 *  
 *  Pass 1 (sort the property table based on the frequencies):
 *  ==========================================================
 *  For each of the subjects sort the properties in the descending order
 *  and ignore the properties which has 0 in the property table.
 *  Let's call this the 'Leftovers' table
 *  
 *  Leftovers
 *  A		P3:30 	P2:20	P1:10	P4:10
 *  B		P5:25	P4:20
 *  C		P3:20	P1:10	P4:10	P5:10
 *  
 *  Pass2 (Generate the initial schema):
 *  ===================================
 *  Create a table for each of the Subjects/Objects
 *  Start grouping each of the tables with pairs of the highest occuring predicates
 *  Update the Leftovers with, well, leftover predicates
 *  
 *  Now we would have something like this:
 *  
 *  Table A
 *  P3 	P2
 *  
 *  Table B
 *  P4	P4
 *  
 *  Table C
 *  P3	P1
 *  
 *  Leftovers
 *  A		P1:10	P4:10
 *  B		
 *  C		P4:10	P5:10
 *
 *  Pass3 (Iterate over the Leftover properties until there are no more):
 *  =====================================================================
 *  For each of the leftover properties pick the one with the highest frequency and stick it in the
 *  corresponding table.
 *  If there is a tie, check which schema has the highest frequency for any property already entered,
 *  and stick the new property in the table which has the least difference. (So, in this situation P4
 *  would go in Schema C instead of in Schema A)
 *  
 *  TODO: Figure out a better way of minimizing NULLs! 
 *  
 *  So, now the above schema would be:
 *  
 *  Table A
 *  P3 	P2	P1
 *  
 *  Table B
 *  P4	P4
 *  
 *  Table C
 *  P3	P1	P4	P5
 *  
 *  Leftovers
 *  A		
 *  B		
 *  C	
 *  
 *  Afterthought: May be we could run some other optimization algorithm on this schema?
 *  But what would be the criteria?
 *  And the cost metrics?
 *  
 * @author oshani
 *
 */
public class SchemaGeneratorImpl implements SchemaGenerator {

	private Vector<Vector<Integer>> originalFrequencyTable ;
	
	private Vector<Vector<Integer>> leftoversTable ; //A copy of the originalFrequencyTable we could mutate

	private HashSet<String> subjectDomains; //This is what the RDFStore returns
	
	private LinkedList<String> schema; //The final schema the 'schema generating' algorithm produces
	
	private Vector<Table> tables;
	
	public LinkedList<String> getSchema() {
		return schema;
	}
	
	/**
	 * For each of the subjects this method should sort the property values sorted by their frequency
	 * Since the FrequencyCounter does not have a method to return which property these frequencies
	 * are referring to let's just for the moment return them like <p1, frequency> where "p1" refers 
	 * to the first property the entire RDF graph has
	 * 
	 * @return sorted frequency values per each property
	 */
	private HashMap<String, Integer> sortPropertyFrequencyValues
							(Vector<Integer> propertyFrequenciesPerSubjectDomain){
		
		return null;
	}
	
	/**
	 * Do Pass1 and Pass2 here
	 */
	private void createInitialSchema
							(HashSet<String> subjectDomains, Vector<Vector<Integer>> propertyFrequencies){
		
	}
	
	/**
	 * Do Pass3 here
	 */
	private void eliminateLeftoverTable(Vector<Vector<Integer>> leftoversTable){
		
	}
	
	/**
	 * Iterate over all the Table objects in the tables vector and output the 'schema' in a format
	 * the RDFBrowser would understand
	 */
	private void constructSchema(){
		
	}

}
