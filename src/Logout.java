

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
public class Logout extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public Logout() {
		super();
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
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//Here we invalidate the session and display logged out message
		HttpSession session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		
		ServletContext servletContext = this.getServletContext();
		
		RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/successfullogout.jsp");
		requestDispatcher.forward(request,response);
		
		/**response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>You have successfully logged out of Cellma AnyMaps</TITLE></HEAD>");
		out.println("  <BODY>");
		out.print("You have successfully logged out of Cellma AnyMaps");
		out.println("  </BODY>");
		out.println("</HTML>");
		out.flush();
		out.close();*/
	}

}
