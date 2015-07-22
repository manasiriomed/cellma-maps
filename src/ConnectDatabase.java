import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;

public class ConnectDatabase {
	public Connection getConnection(Connection con, ServletContext sc) throws ClassNotFoundException{
	    
	    /* String url = "jdbc:odbc:testcellma"; 
	    String db = "cellma";*/

	    /* String driver = "com.mysql.jdbc.Driver"; */
		
		String url = sc.getInitParameter("dburl");
		String password = sc.getInitParameter("password");
		String username = sc.getInitParameter("username");
		
		try{
	    	  /* Class.forName(driver).newInstance(); */
	    	  Class.forName("com.mysql.jdbc.Driver");
	    	  /* Class.forName("java.sql.Driver");  /* java.sql.Driver */ 
	    	  con = DriverManager.getConnection(url, username, password);
	  	      /* con = DriverManager.getConnection(url, user, pass); */
	      }
	      catch (SQLException s){
	    	  System.out.println("connection SQL code does not execute.");
	      }
		return con;
	}
}