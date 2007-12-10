import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;

import edu.mit.db.rstore.DBPopulator;
import edu.mit.db.rstore.SchemaGenerator;
import edu.mit.db.rstore.impl.DBConnection;
import edu.mit.db.rstore.impl.PropertyTable;
import edu.mit.db.rstore.impl.RDFSBasedDBPopulator;
import edu.mit.db.rstore.impl.RDFSBasedSchemaGenerator;
import edu.mit.db.rstore.impl.StatisticalDBPopulator;
import edu.mit.db.rstore.impl.StatisticalSchemaGenerator;
import edu.mit.db.rstore.impl.Store;

/**
 * This acts as the main entry point to the program from the command line
 * 
 * The accepted command line arguments are:
 * -h	Displays the help menu
 * -r	Use the RDFS based schema generation
 * -s	Use the RDF data Statistics based schema generation
 * -d  	Show the RDB schema populated with the data from RDF 
 * 
 *  Example usage: 
 *  	$java Main -sd
 *  	Run the program with the Statistical Schema Generation and 
 *  	show the populated database table structure
 *  
 * @author oshani
 *
 */
public class Main {

	public static String path= "data/";
	public static Store store = new Store (path);
	public static SchemaGenerator schemaGenerator;
	public static LinkedList<PropertyTable> schemas;
	public static DBPopulator populator;
	public static DBConnection connection = new DBConnection();
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException{

		connection.connect();
		connection.clear();
    	store.CreateModel();
    	store.CreateSchema();

    	if (args.length == 0 || args[0].indexOf('h') != -1){
			showHelp();
		}
		else if (args[0].indexOf('s') != -1){
			runStatisticalSchemaGenerator();
			if (args[0].indexOf('d') != -1){
				printResults();
			}
		}
		else if (args[0].indexOf('r') != -1){
			runRDFSBasedSchemaGenerator();
			if (args[0].indexOf('d') != -1){
				printResults();
			}
		}
	}
	
	private static void printResults() throws SQLException {
		for (PropertyTable p: schemas){
			String tableName = p.getName().toLowerCase();
			System.out.println("\n"+tableName);
			System.out.println("********************");
			try {
			    ResultSet rs = connection.st.executeQuery( "SELECT * FROM "+ tableName);
			    try {
		            ResultSetMetaData rsmd = rs.getMetaData();
		            int numColumns = rsmd.getColumnCount();
		            for ( int i = 1 ; i <= numColumns ; i++ ) {
		            	String columnName = rsmd.getColumnName(i);
		            	System.out.print(columnName +"\t"+"\t"+"\t");
		            	
		            }
		            System.out.println();
	            	   while ( rs.next() ) {
			             for ( int i = 1 ; i <= numColumns ; i++ ) {
					       System.out.print( rs.getObject(i) +"\t"+"\t"+"\t" );
			            }
			            System.out.println();
			        }
			    } finally {
			    //    rs.close();
			    }
			} finally {
			//	connection.st.close();
			}	
			
		}
	}

	public static void runStatisticalSchemaGenerator() throws ClassNotFoundException, SQLException{
		schemaGenerator = new StatisticalSchemaGenerator(store);
    	schemas = schemaGenerator.getSchema();
    	populator = new StatisticalDBPopulator(schemas, store);
	}
	
	public static void runRDFSBasedSchemaGenerator() throws ClassNotFoundException, SQLException{
		schemaGenerator = new RDFSBasedSchemaGenerator(store);
    	schemas = schemaGenerator.getSchema();
    	populator = new RDFSBasedDBPopulator(schemas, store);
	}
	
	public static void showHelp(){
		String help = 	"\n**********"+
						"\n r-store"+
						"\n**********"+
						"\n Please use one of the following switches"+
						"\n -h	Displays the help menu"+
					   	"\n -r	Use the RDFS based schema generation"+
					   	"\n -s	Use the RDF data Statistics based schema generation"+
					   	"\n -d  	Show the RDB schema populated with the data from RDF"+
					   	"\n\n  Example usage:"+
					   	"\n  	$java Main -sd"+
					   	"\n  	(Run the program with the Statistical Schema Generation and"+
					   	"\n  	show the populated database table structure)\n";
		System.out.println(help);
	}
}
