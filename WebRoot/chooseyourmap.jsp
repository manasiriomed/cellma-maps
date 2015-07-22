<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Choose Your Map</title>

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

$(document).ready(function(){
	initToolTips();
	$("img.a").hover(
		function() {
		$(this).stop().animate({"opacity": "0"}, "slow");
		},
		function() {
		$(this).stop().animate({"opacity": "1"}, "slow");
	});
});

</script>
</head>
<body>

<div id="toolTipLayer" style="position:absolute; visibility: hidden"></div>

<table align="center" bgColor="#e7e7e7" width="100%" cellpadding="0" cellspacing="0">
		<tr bgColor="#FFFFFF">
			<td background="./images/darkGrayLeft.gif" height="21" width="8">&nbsp;</td>
			<td class="slimDataHeader2" align="center" background="./images/darkGrayCenter.gif"><font color="#FFFFFF"><b>Trinidad Maps</b></font></td>
			<td background="./images/darkGrayRight.gif" height="21" width="11">&nbsp;</td>
		</tr>	
		<tr bgColor="#e7e7e7">
			<td colspan="3">
				<table bgColor="#e7e7e7" cellpadding="1" width="100%">
					<tr>
						<td colspan="3">
							<table width="100%" cellpadding="4" cellspacing="0" align="center">
								<tr bgColor="#FFFFFF">
									<td align="left"><img src="./images/famfamfamsilk/bigicons/reports.png" /></td>
									<td width="100%" style="margin-left: auto; margin-right: auto"><p class="slimDataBold1">Please select your desired map in order to proceed.</p></td>
									<td align="right"><a href="javascript:window.close();"><img onMouseOver="toolTip('Close')" onMouseOut="toolTip()" src="./images/famfamfamsilk/bigicons/arrow_up.png" border="0"  name="Close" /></a></td>
								</tr>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr bgColor="#dde4f7">
			<td align="center" colspan="3">
				<table border="0">
					<tr><td style="font-size: 3px" colspan="5">&nbsp;</td></tr>
					<tr>
						<td class="tdStyler">
							<div class="fadehover">
								<a href="displaymapfordiagnosis.jsp" target="_self">
						            <img src="./images/map-grey.png" alt="" class="a" />
						            <img src="./images/map-color.png" alt="" class="b" />
						        </a>
							</div>
							<h3>Diagnosis</h3>
							<hr />
						</td>
						<td>&nbsp;</td>
						<td class="tdStyler">
							<div class="fadehover">
								<a href="displaymapforreferralreasons.jsp" target="_self">
						            <img src="./images/map-grey.png" alt="" class="a" />
						            <img src="./images/map-color.png" alt="" class="b" />
						        </a>
							</div>
							<h3>Referral Reasons</h3>
							<hr />
						</td>
						<td>&nbsp;</td>
						<td class="tdStyler">
							<div class="fadehover">
								<a href="displaymapforappointmentreasons.jsp" target="_self">
						            <img src="./images/map-grey.png" alt="" class="a" />
						            <img src="./images/map-color.png" alt="" class="b" />
						        </a>
							</div>
							<h3>Appointment Reasons</h3>
							<hr />
						</td>
					</tr>
					<tr><td colspan="3">&nbsp;</td></tr>	
			</table>
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
</body>
