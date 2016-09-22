import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@SuppressWarnings("serial")
public class LoginCheck extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		
		ServletContext servletContext = this.getServletContext();	
			
		ConnectDatabase connect = new ConnectDatabase();
	    Connection conn 		= null;
		    
	    String username 		= null;
	    String yearAppReasons 	= null;
	    String yearRefReasons 	= null;
	    String yearDiagnosis 	= null;
	    String estId			= null;
	    String cliId 			= null;
	    
	    ArrayList<String> yearListAppReasons = new ArrayList<String>();
	    ArrayList<String> yearListDiagnosis  = new ArrayList<String>();
	    ArrayList<String> yearListRefReasons = new ArrayList<String>();
	    //String estName  = null;
	    
	    String sessionId = request.getParameter("sessionId");
	    
	    if(sessionId != null && sessionId.length() > 0) {
		    try {
				conn = (Connection)connect.getConnection(conn, servletContext);
				
		        //String query="select * from users where use_session='"+sessionId+"'";
		        String query = "SELECT *" 
	                        + " FROM users u, establishments e" 
	        		        + " WHERE u.use_session='"+sessionId+"'" 
	                        + " AND u.use_est_id = e.est_id";
		        Statement statement=conn.createStatement();
		        ResultSet resultSet=statement.executeQuery(query); 
		        
		        /*String query2 =  " SELECT EXTRACT(YEAR FROM rea_date) AS Year" +
   				 				 " FROM realtime_appointment_reasons r, establishments e" +
			 					 " WHERE r.rea_est_id = e.est_id " +
			 					 " GROUP BY Year" +
			 					 " ORDER BY Year DESC";
		        
		        Statement statement2 = conn.createStatement();
		        ResultSet resultSet2 = statement2.executeQuery(query2); */
		        
		        String query3 =  " SELECT EXTRACT(YEAR FROM ref_acceptance_date) AS Year" +
			 				 	 " FROM realtime_referral_reasons r, establishments e" +
			 				 	 " WHERE r.rer_est_id = e.est_id " +
			 				 	 " GROUP BY Year" +
			 				 	 " ORDER BY Year DESC";
		        
		        Statement statement3 = conn.createStatement();
		        ResultSet resultSet3 = statement3.executeQuery(query3); 
		        
		        String query4 =  " SELECT EXTRACT(YEAR FROM rrc_clinic_date) AS Year" +
	 				 	 		 " FROM realtime_diagnosis r, establishments e" +
	 				 	 		 " WHERE r.rrc_est_id = e.est_id " +
	 				 	 		 " GROUP BY Year" +
	 				 	 		 " ORDER BY Year DESC";
		        
		        Statement statement4 = conn.createStatement();
		        ResultSet resultSet4 = statement4.executeQuery(query4); 
		    

		        if(resultSet !=null && resultSet.next()){
		        	username=resultSet.getString("u.use_username");
		        	estId=resultSet.getString("u.use_est_id");
		        	cliId=resultSet.getString("u.use_cli_id");
		        }
		        
		        resultSet.close();
		        statement.close();	
		        
		        //Creating a list of years applicable for each version of the AnyMaps (Diagnosis, Referral Reasons and Appointment Reasons)
		        
		        /*if(resultSet2 !=null){
		        	while (resultSet2.next()){
		        		yearAppReasons = resultSet2.getString("Year"); 
		        		yearListAppReasons.add(yearAppReasons);
		            }
		        }
		        
		        resultSet2.close();
		        statement2.close();	*/
		        
		        if(resultSet3 !=null){
		        	while (resultSet3.next()){
		        		yearRefReasons = resultSet3.getString("Year"); 
		        		yearListRefReasons.add(yearRefReasons);
		            }
		        }
		        
		        resultSet3.close();
		        statement3.close();	
		        
		        if(resultSet4 !=null){
		        	while (resultSet4.next()){
		        		yearDiagnosis = resultSet4.getString("Year"); 
		        		yearListDiagnosis.add(yearDiagnosis);
		            }
		        }
		        
		        resultSet4.close();
		        statement4.close();	
		        
		    } catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}finally {
				try {
					if(conn != null && (!conn.isClosed())) {
						try {
							conn.close();
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
	    }	
		
		if(username != null && estId != null) {
			//Successfully logged into Cellma display report select page
			HttpSession session = request.getSession(true);
			session.setAttribute("username",username);
			session.setAttribute("estId",estId);
			session.setAttribute("cliId",cliId);
			try {
			      getServletConfig().getServletContext().getRequestDispatcher("/chooseyourmap.jsp").forward(request,response);

			    } catch (ServletException e) {
			      // TODO Auto-generated catch block
			      e.printStackTrace();
			    } catch (IOException e) {
			      // TODO Auto-generated catch block
			      e.printStackTrace();
			    }
		}
		else {
			//Not logged into Cellma display failed to log in page
			try {
			      getServletConfig().getServletContext().getRequestDispatcher(
			        "/failedlogin.jsp").forward(request,response);

			    } catch (ServletException e) {
			      // TODO Auto-generated catch block
			      e.printStackTrace();
			    } catch (IOException e) {
			      // TODO Auto-generated catch block
			      e.printStackTrace();
			    }
		}
		
		//Sending List of applicable Years for each map type to jsp pages
		if(yearListAppReasons != null){
			HttpSession session = request.getSession(true);
			session.setAttribute("yearListAppReasons", yearListAppReasons);
		}
		
		if(yearListRefReasons != null){
			HttpSession session = request.getSession(true);
			session.setAttribute("yearListRefReasons", yearListRefReasons);
		}
		
		if(yearListDiagnosis != null){
			HttpSession session = request.getSession(true);
			session.setAttribute("yearListDiagnosis", yearListDiagnosis);
		}
        
	}
}
