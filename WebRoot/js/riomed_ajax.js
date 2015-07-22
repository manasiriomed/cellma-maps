  function getXMLHTTPRequest() {
  	var request = false;
  	try {
  		request = new XMLHttpRequest(); /* Firefox */
  	}
  	catch(error1){
  		
  		try {
  			request = new ActiveXObject("Msxml2.XMLHTTP"); /* some versions of IE */
  		}
  		catch (error2) {
  			try {
  				request = new ActiveXObject("Microsoft.XMLHTTP"); /* some versions of IE */  			
  			}
  			catch (error3) {
  				request = false;
  			}
  		}
  		
  	}
  	
	return request  
  }	