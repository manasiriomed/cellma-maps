<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Cellma AnyMaps - Diagnosis</title>

<!--[if gte IE 7]>
<link type="text/css" rel="stylesheet" href="./css/loader.css"></link>
<![endif]-->
<!--[if !IE]>-->
<link type="text/css" rel="stylesheet" href="./css/loader-non-ie.css"></link>
<!--<![endif]-->

<script type="text/javascript" src="./js/tooltip.js"></script>
<script type="text/javascript" src="./js/calendar1.js"></script>
<script type="text/javascript" src="./js/riomed_ajax.js"></script>
<script type="text/javascript" src="./js/jquery-1.7.2.min.js"></script>

<script type="text/javascript" language="JavaScript">
<!--
	initToolTips();
//-->	
	
	function livesearch(){
		var s = document.getElementById('search').value;
			
		var myRequest = getXMLHTTPRequest();
		
		if(!myRequest) {
			alert('Failed to create activeX object');	
		}
		
			var url = "./AjaxUtilsDiagnosis.do";
			var params =  "?s=" + s;

			url = url + params;
			
			if(myRequest != null) {
				myRequest.open("POST",url,true);
				myRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				myRequest.setRequestHeader("Content-length",params.length);
				myRequest.setRequestHeader("Connection","close");
				
				myRequest.onreadystatechange = function () {
						
					if(myRequest.readyState == 4) {
						if(myRequest.status == 200) {
							document.getElementById('searchResults').innerHTML = myRequest.responseText;	
						}
					}
										
				};
				myRequest.send(params);
		 	}
		
	}
	
	function showMyDiagnosisMap(qucId, qucText){
        var startDate = document.getElementById("startDate").value;
        var endDate = document.getElementById("endDate").value;
		var dateRangeValue = startDate + " - " + endDate;
		
		if(startDate == "" || endDate == ""){
			alert("Please select a date period");
		}
		else{
	        var url = "./DisplayMapAjaxUtilsDiagnosis.do?dateRange=" + dateRangeValue + "&qsId=" + qucId + "&qsText=" +qucText + "&startDate=" +startDate + "&endDate=" +endDate;
	        location.href=url;
	        $( "#searchinfo" ).fadeOut( "slow", function() {
	    		$("#loading").css({"display":"block"});
	   	  	});
		}
	}
	
	function clearSearch(){

		var startSearchControl = document.getElementById('search');
		var resultsFromSearch = document.getElementById('searchResults');
		var dateRangeControl = document.getElementById('startDate');
		
		startSearchControl.value = "";
		dateRangeControl.value = "";
		resultsFromSearch.value = "";
		
		resultsFromSearch.innerHTML = "";
		startSearchControl.innerHTML = "";
	}

</script>
</head>

<body>
<div id="toolTipLayer" style="position:absolute; visibility: hidden"></div>

<form name="frm1">
<table align="center" bgColor="#e7e7e7" width="100%" cellpadding="0" cellspacing="0">
		<tr bgColor="#FFFFFF">
			<td background="./images/darkGrayLeft.gif" height="21" width="8">&nbsp;</td>
			<td class="slimDataHeader2" align="center" background="./images/darkGrayCenter.gif"><font color="#FFFFFF"><b>Trinidad Diagnostic Map</b></font></td>
			<td background="./images/darkGrayRight.gif" height="21" width="12">&nbsp;</td>
		</tr>	
		<tr bgColor="#e7e7e7">
			<td colspan="3">
				<table bgColor="#e7e7e7" cellpadding="1" width="100%">
					<tr>
						<td colspan="3">
							<table width="100%" cellpadding="4" cellspacing="0" align="center">
								<tr bgColor="#FFFFFF">
									<td align="left"><img src="./images/famfamfamsilk/bigicons/reports.png" /></td>
									<td width="100%" style="margin-left: auto; margin-right: auto"><p class="slimDataBold1">Please select your date period followed by the desired diagnosis in order to proceed.</p>
									</td>
									<td align="right"><a href="./chooseyourmap.jsp"><img src="./images/famfamfamsilk/bigicons/arrow_left.png" onMouseOver="toolTip('Back');" onMouseOut="toolTip();" border="0" /></a></td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr bgColor="#dde4f7">
			<td align="center" colspan="3">
				<div id="searchinfo">
					<br />
					<table>
						<tr>
							<td class="slimDataBold2">Start Date</td>
							<td class="slimDataBold2">End Date</td>
							<td class="slimDataBold2" align="left">&nbsp;Enter a Diagnosis</td>
						</tr>
					<tr>
						<td>
							<input type="text" name="startDate" id="startDate"/>
							<a href="javascript:cal1.popup();"><img src="./images/cal/cal.gif" width="16" height="16" border="0" alt="Pick the date"></a>
						</td>
						<td>
							<input type="text" name="endDate" id="endDate"/>
							<a href="javascript:cal2.popup();"><img src="./images/cal/cal.gif" width="16" height="16" border="0" alt="Pick the date"></a>
						</td>
						<td align="left"><input type="text" style="border: 1px solid #BFBFBF" id="search" value="" onKeyUp="javascript:livesearch();" size="30"  maxlength="255" />&nbsp;&nbsp;<a style="font-size: 15px" href="javascript:clearSearch();">Clear search</a></td>
					</tr>
					<tr>
						<td onClick="javascript:clearSearch();">&nbsp;</td>
						<td onClick="javascript:clearSearch();">&nbsp;</td>
						<td><div id="searchResults" style="width:299px"></div></td>
					</tr>	
				</table>
				</div>
				<div id="loading" style="display: none">
					<br />
					<p class="slimDataBold3">Please wait retrieving data to generate map</p>
					<p class="slimDataBold3">This may take up to 2 mins depending on volume of data required.</p> 
					<img src="./images/loading-wheel.gif" width="200px" height="200px"/>
				</div>	
			</td>
		</tr>
		<tr>
			<td  onClick="javascript:clearSearch();">	
				<table align="center" border="0">
					<tr>
					<td  onClick="javascript:clearSearch();">&nbsp;</td>
					</tr>
				</table>
		</td>
	</tr>
</table>
</form>
</body>

<script type="text/javascript" >

	// create calendar object(s) just after form tag closed
	// specify form element as the only parameter (document.forms['formname'].elements['inputname']);
	// note: you can have as many calendar objects as you need for your application
	var cal1 = new calendar1(document.forms['frm1'].elements['startDate']);
	cal1.year_scroll = true;
	cal1.time_comp = false;	
	
	var cal2 = new calendar1(document.forms['frm1'].elements['endDate']);
	cal2.year_scroll = true;
	cal2.time_comp = false;	

</script>
