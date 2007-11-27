package edu.mit.db.rstore.impl;

import java.util.*;

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
 * 	Angelika's Frequency Table: (have ignored the last column for the count)
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
 *  Start grouping each of the tables with pairs of the highest occurring predicates
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

	private FrequencyCounter frequencyCounter;
	
	private HashMap<String, HashMap<String, Integer>> leftoversTable;
	
	private LinkedList<String> schema; //The final schema the 'schema generating' algorithm produces
	
	private HashMap<String, Vector<String>> tables;
	
	/**
	 * Constructor
	 */
	public SchemaGeneratorImpl(FrequencyCounter fc){
		this.frequencyCounter = fc;
		this.sortPropertyFrequencyValues();
	}
	
	//Modified return type and set return value to null for now  -AM
	public LinkedList<PropertyTable> getSchema() {
		return null;
	}
	
	public HashMap<String, HashMap<String, Integer>> getLeftoversTable(){
		return this.leftoversTable;
	}
	
	public HashMap<String, Vector<String>> getTables(){
		return this.tables;
	}
	
	/**
	 * Pass 1:
	 * For each of the subjects this method should sort the property values by their frequency
	 * 
	 * @return sorted frequency values per each subject
	 */
	private void sortPropertyFrequencyValues(){
		
		HashMap<Integer, String> predicates = frequencyCounter.getColumnMapping_1();
		HashMap<Integer, String> subjects = frequencyCounter.getRowMapping_1();
		Vector<Vector<Integer>> frequencies = frequencyCounter.getFrequencyTable();
		this.leftoversTable = new HashMap<String, HashMap<String,Integer>>();
		
		//Get the property strings into an array
		String [] propertyLabelArray = new String[predicates.size()];
		for (int i=0; i<predicates.size(); i++){
			propertyLabelArray[i] = predicates.get(i);
		}
		
		for(int i = 0; i < subjects.size(); i++){
			
			Vector<Integer> frequenciesPerSubject = frequencies.get(i);
			int[] propertyFrequencyArray = new int[predicates.size()];
			
			for(int j = 0; j < predicates.size(); j++){
				//Add the frequencies in a row to an array
				propertyFrequencyArray[j] = frequenciesPerSubject.get(j); 
			}
			
			//Arrays.sort(propertyFrequencyArray);
			//Do the sorting explicitly, as we need the property associated with the frequency
			quickSort(propertyFrequencyArray, propertyLabelArray, propertyFrequencyArray.length);
			
			HashMap<String, Integer> propertiesAndFrequencies = new HashMap<String, Integer>();
			
			//Add the properties and frequencies in sorted order ignoring the 0s
			for(int j = predicates.size()-1 ; j >= 0 ; j--){
				if (propertyFrequencyArray[j] != 0){
					propertiesAndFrequencies.put(propertyLabelArray[j], propertyFrequencyArray[j]);
				}
			}

			//Add the subject and the corresponding HashMap to the leftovers table if we have good properties
			if (!(propertiesAndFrequencies.keySet().equals(null)))
				leftoversTable.put(subjects.get(i), propertiesAndFrequencies);
		}
	}
	
	/**
	 * Quick sort algorithm copied and modified from http://linux.wku.edu/~lamonml/algor/sort/quick.html
	 * @param numbers
	 * @param array_size
	 */
	private void quickSort(int numbers[], String[] labels, int array_size)
	{
	  q_sort(numbers, labels, 0, array_size - 1);
	}


	private void q_sort(int numbers[], String[] labels, int left, int right)
	{
	  int pivot, l_hold, r_hold;
	  String pivotStr;
	  
	  l_hold = left;
	  r_hold = right;
	  pivot = numbers[left];
	  pivotStr = labels[left];
	  
	  while (left < right)
	  {
	    while ((numbers[right] >= pivot) && (left < right))
	      right--;
	    if (left != right)
	    {
	      numbers[left] = numbers[right];
	      labels[left] = labels[right];
	      left++;
	    }
	    while ((numbers[left] <= pivot) && (left < right))
	      left++;
	    if (left != right)
	    {
	      numbers[right] = numbers[left];
	      labels[right] = labels[left];
	      right--;
	    }
	  }
	  numbers[left] = pivot;
	  labels[left] = pivotStr;
	  pivot = left;
	  left = l_hold;
	  right = r_hold;
	  if (left < pivot)
	    q_sort(numbers, labels, left, pivot-1);
	  if (right > pivot)
	    q_sort(numbers, labels, pivot+1, right);
	}
	
	//End of the customized QuickSort Algorithm
	
	
	/**
	 * Pass2
	 */
	private void createInitialSchema(){
		
	}
	
	/**
	 * Pass3
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
