package edu.mit.db.rstore.impl;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Hashtable;
import java.util.LinkedList;

import edu.mit.db.rstore.RDFBrowser;

public class RDFBrowserImpl extends HttpServlet implements RDFBrowser{

	public Hashtable runQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	public void displayData(LinkedList<String> data) {
		// TODO Auto-generated method stub
		
	}

	public void displaySchema(LinkedList<String> schemaStructure) {
		// TODO Auto-generated method stub
	
	}
	
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<head>");
        out.println("<title>RDF Browser</title>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h3>RDF Browser</h3>");
        out.println("</body>");
        out.println("</html>");
    }
    
    /**
     * Just pass the work to the GET method
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        doGet(request, response);
    }

}
