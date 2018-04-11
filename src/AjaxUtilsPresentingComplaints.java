import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@SuppressWarnings("serial")
public class AjaxUtilsPresentingComplaints extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public AjaxUtilsPresentingComplaints() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		ConnectDatabase connect = new ConnectDatabase();
	    Connection con = null;
	    
        try {
			con = (Connection)connect.getConnection(con, this.getServletContext());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
        //Variables for jsp file
        String diagSearch = request.getParameter("s");
        
        //This is the session for the Cellma Reports not the Cellma session
  		HttpSession session = request.getSession(false);

  		if(session == null) {
  			//The session has timed out usually after 30 mins
  			ServletContext servletContext = this.getServletContext();
  			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/sessionexpired.jsp");
  			requestDispatcher.forward(request,response);
  			return;
  		}
  		
  		//Session variables
  		String estId = (String)session.getAttribute("estId");
  		boolean flag = false;
  		
  		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (diagSearch != null && diagSearch.length() >= 3)
		{
			out.println("<table style=\"background-color: #fff; width: 205px; font-size: 15px\">");
			
			Statement stmt = null;        
			
	  	    try {
	  	        stmt = con.createStatement();
	  	        String query = "";
	  	        
	  	        if(estId != null){
	  	        	query = "SELECT *" +
						   " FROM question_options qo, realtime_presenting_problems rd " +
					 	   " WHERE qo.quo_id = rd.rrc_quo_id" +
					       " AND qo.quo_est_id = "+ estId +
					       " AND qo.quo_text_summary LIKE '" + diagSearch + "%'" +
					       " GROUP BY rd.shortname";
	  	        }
	  	        else{
	  	        	query = "SELECT *" +
							   " FROM question_options qo, realtime_presenting_problems rd " +
						 	   " WHERE qo.quo_id = rd.rrc_quo_id" +
						       " AND qo.quo_text_summary LIKE '" + diagSearch + "%'" +
						       " GROUP BY rd.shortname";
	  	        }
	  	        
	  	        ResultSet rs = stmt.executeQuery(query);
	  	        //System.out.println(query);
	        	
	        	if(!rs.isBeforeFirst()){
	        		flag = true;	
	        	}
	        	else{
	        		while (rs.next()) {
	        			
	        			Integer qucId = rs.getInt("qo.quo_id");
	    	        	String qucText = rs.getString("qo.quo_text_select_box");
	    	        	String qucTextSend = qucId + ",'" + qucText + "'";
	        		  	   
		  	        	out.println("<tr><td align=\"center\"><a href='#' onClick=\"javascript:showMyPresentingComplaintsMap(" +qucTextSend +");\">" + rs.getString("qo.quo_text_select_box") + "</a></td></tr>");
		  	        }
	        	}
	  	        
	  	        rs.close();
	  	        

	  	    } catch (SQLException s){
		        System.out.println("report SQL code does not execute.");
	  	    } finally {
	  	        if (stmt != null) { try {
					stmt.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
	  	    }
	  	    
	  	    out.println("</table>");
		}
  	   /* else if(diagSearch.length() >= 1 && diagSearch.length() < 3){
			out.println("<p id='minText' style='font-size: 15px'>Please enter a minimum of 3 characters to search</p>");	
		}*/
		
		if(flag){
			out.println("<p id='noDataText' style='font-size: 15px; text-align: left'>No Data returned for this criteria</p>");	
		}
  	    
		out.flush();
		out.close();
		
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
