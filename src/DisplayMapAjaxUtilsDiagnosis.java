import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DisplayMapAjaxUtilsDiagnosis extends HttpServlet {

	/**
	 * 
	 */
	
	//Defining global variable regions
	public final String regions[] = {"Chaguanas", "Couva/Tabaquite/Talparo", "Chaguaramas", "Mayaro/Rio Claro", "Penal/Debe", "Point Fortin", 
			"Port of Spain", "Princes Town", "Siparia", "San Fernando", "Sangre Grande", "San Juan/Laventille", "Tunapuna/Piarco", "Arima"};
	
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of the object.
	 */
	
	public DisplayMapAjaxUtilsDiagnosis() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}
	
	//Query to acquire total count of people with Diagnosis for all regions
	private int[][][] diagnosisValueForRegion(Connection con, String estId, String dateRange, int qucId, String regions[], String startDate, String endDate){
		
		Statement stmt = null;        
		int total[][][] = new int[15][16][3]; //Multidimensional Array with the first array representing regions, second array representing districts within each region, and the third used for the result
		int regionNo = 13;
		String regionQuery = "", regionTemp = "";
		
	    try {
	        stmt = con.createStatement();
	        String query = "";

	        for (int i= 0; i <= regionNo; i++){ 
	        	//massage the regions to fit the IN query criteria for sql
	        	if(regions[i] == "Couva"){
					regionTemp = "Couva/Tabaquite/Talparo";
				}
				else{
					regionTemp = regions[i];
				}
	        	
	        	if(regionTemp != null){
	        		regionQuery += "'" + regionTemp + "'"; 
	        		
	        		if(i < regionNo){
	        			regionQuery += ", "; //need , to separate the regions
	        		}
	        	}
	       	}
	        
	        if(estId != null){
				//CALCULATION OF TOTAL AMOUNT VIA REGION ONLY				
				query =  "SELECT r.add_reporting_region as region, COUNT(*) AS total_count" +
						" FROM realtime_diagnosis r" +
						" WHERE r.rrc_est_id = "+ estId +
						" AND r.rrc_answered_id = "+ qucId +
						" AND r.add_reporting_region IN (" + regionQuery +")" +
						" AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
						
				String groupByPart = "";
	            groupByPart += " GROUP BY r.add_reporting_region";
	            query += groupByPart;
	            
	            String orderByPart = "";
	            orderByPart += " ORDER BY r.shortname ASC";
	            query += orderByPart;		
	            
			}
			else{
				//CALCULATION OF TOTAL AMOUNT VIA REGION ONLY
				query =  "SELECT r.add_reporting_region as region, COUNT(*) AS total_count" +
						" FROM realtime_diagnosis r" +
						" WHERE r.rrc_est_id IS NULL" +
						" AND r.rrc_answered_id = "+ qucId +
						" AND r.add_reporting_region IN (" + regionQuery +")" +
						" AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
						
				String groupByPart = "";
	            groupByPart += " GROUP BY r.add_reporting_region";
	            query += groupByPart;
	            
	            String orderByPart = "";
	            orderByPart += " ORDER BY r.shortname ASC";
	            query += orderByPart;		
			}
	        
	        if(query != null){
	        	ResultSet rset = stmt.executeQuery(query);
	        	//go through each result set and see if the region matches. Set count if they do
	        	while (rset.next()){
		        	for (int i= 0; i <= regionNo; i++){//these are regions
		        		if(regions[i] == "Couva"){
							regionTemp = "Couva/Tabaquite/Talparo";
						}
						else{
							regionTemp = regions[i];
						}
			        	if(regionTemp != null && regionTemp.equalsIgnoreCase(rset.getString("region").toLowerCase())){
			        		total[14][i][0] = rset.getInt("total_count");
			        	}
						        
					}
		        }
	        	rset.close();
	        }

	    } catch (SQLException s){
	    	System.out.println(s.getMessage() + " report SQL code does not execute.");
	    } finally {
	        if (stmt != null) { try {
			stmt.close();
			//con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} }
	    }
		
		return total;
	}
	
	private StringBuffer county(Connection con, StringBuffer xmlData, String estId, String regions[], String regionsAmap[], int totals[][][], int populationForRegions[]){
		double totalRate = 0.00d;
		
		for(int i=0; i<regions.length; i++){
			totalRate = ((double)totals[14][i][0]/(double)populationForRegions[i]) * 1000; 
			
			//TESTING
			/*System.out.println("Total Rate: " +totalRate);
			System.out.println("Total Male Rate: " +totalRateMale);
			System.out.println("Total Female Rate: " +totalRateFemale);
			System.out.println(totals[14][i][0]);
			System.out.println(regionsAmap[i]);*/
			//System.out.println(populationForRegions[i]);

			xmlData.append("          <point name=\""+regions[i]+"\">");
			xmlData.append("            <attributes>");
			xmlData.append("              <attribute name=\"Region\">"+regionsAmap[i]+"</attribute>");
			xmlData.append("              <attribute name=\"Pop\">"+populationForRegions[i]+"</attribute>");  //Census Population
			xmlData.append("              <attribute name=\"Y\">"+totalRate+"</attribute>");                  //Total rate of people with Referral Reason in region
		    xmlData.append("              <attribute name=\"X\">"+totals[14][i][0]+"</attribute>");           //Total amount of people that have Referral Reason in region
			xmlData.append("            </attributes>");
			xmlData.append("          </point>");
		}
		return xmlData;
	}
	
	
	private StringBuffer subRegion(Connection con, StringBuffer xmlData, String estId, String dateRange, int qucId, String regionText, String[][] districts, int totalsd[][][], int populationForRegions[], String startDate, String endDate) {
		
		int regionNo = 14;
		int districtNo = 16;
		int regionID = -1;
		String districtQuery = "";
		
		//Find regionID
		for(int i = 0; i < regionNo; i++) {
			if(regionText.equalsIgnoreCase(regions[i])){
				regionID = i;
				break;
			}
		}
		
		//Finding all districts which is then separated by a comma until the final one
		for(int j = 0; j < districtNo; j++){ 
			if(regionID >= 0 && districts[regionID][j] != null){
				districtQuery += "\"" + districts[regionID][j] + "\""; 
        		if(j < districtNo - 1 && (j < districtNo - 1 && districts[regionID][j + 1] != null)){
        			districtQuery += ", ";//need , to separate the regions
        		}
        	}
		}
		
		//String Array storing query
		String /*queryTotal, */ queryMale, queryFemale;
		
		if(estId != null){			
			//CALCULATING MALE COUNT FOR ALL DISTRICTS IN A PARTICULAR REGION
			queryMale =  "SELECT r.add_reporting_district as district, COUNT(*) AS total_count" +
					 " FROM realtime_diagnosis r" +
					 " WHERE r.rrc_est_id = "+ estId +
					 " AND r.rrc_answered_id = "+ qucId +
					 " AND r.add_reporting_region = '" + regions[regionID] + "'" +
				     " AND r.add_reporting_district IN (" + districtQuery +")" +
				     " AND r.pat_sex = 'M'" +
					 " AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
					
			String groupByPartMale = "";
			groupByPartMale += " GROUP BY district";
	        queryMale += groupByPartMale;
	       
	        String orderByPartMale = "";
	        orderByPartMale += " ORDER BY r.shortname ASC";
	        queryMale += orderByPartMale;	
			
			//CALCULATING FEMALE COUNT FOR ALL DISTRICTS IN A PARTICULAR REGION
	        queryFemale = "SELECT r.add_reporting_district as district, COUNT(*) AS total_count" +
	        			 " FROM realtime_diagnosis r" +
	        			 " WHERE r.rrc_est_id = "+ estId +
	        			 " AND r.rrc_answered_id = "+ qucId +
	        			 " AND r.add_reporting_region = '" + regions[regionID] + "'" +
	        			 " AND r.add_reporting_district IN (" + districtQuery +")" +
	        			 " AND r.pat_sex = 'F'" +
						 " AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
					
			String groupByPartFemale = "";
			groupByPartFemale += " GROUP BY district";
	        queryFemale += groupByPartFemale;
	       
	        String orderByPartFemale = "";
	        orderByPartFemale += " ORDER BY r.shortname ASC";
	        queryFemale += orderByPartFemale;	
		}
		else {
			
			//CALCULATING MALE COUNT FOR ALL DISTRICTS IN A PARTICULAR REGION
			queryMale =  "SELECT r.add_reporting_district as district, COUNT(*) AS total_count" +
					 " FROM realtime_diagnosis r" +
					 " WHERE r.rrc_est_id IS NULL " +
					 " AND r.rrc_answered_id = "+ qucId +
					 " AND r.add_reporting_region = '" + regions[regionID] + "'" +
				     " AND r.add_reporting_district IN (" + districtQuery +")" +
				     " AND r.pat_sex = 'M'" +
					 " AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
					
			String groupByPartMale = "";
			groupByPartMale += " GROUP BY district";
	        queryMale += groupByPartMale;
	       
	        String orderByPartMale = "";
	        orderByPartMale += " ORDER BY r.shortname ASC";
	        queryMale += orderByPartMale;	
			
			//CALCULATING FEMALE COUNT FOR ALL DISTRICTS IN A PARTICULAR REGION
			queryFemale = "SELECT r.add_reporting_district as district, COUNT(*) AS total_count" +
       			 " FROM realtime_diagnosis r" +
       			 " WHERE r.rrc_est_id IS NULL" +
       			 " AND r.rrc_answered_id = "+ qucId +
       			 " AND r.add_reporting_region = '" + regions[regionID] + "'" +
       			 " AND r.add_reporting_district IN (" + districtQuery +")" +
       			 " AND r.pat_sex = 'F'" +
				 " AND r.rrc_clinic_date BETWEEN '"+ startDate +"' AND '"+ endDate +"'";
					
			String groupByPartFemale = "";
			groupByPartFemale += " GROUP BY district";
	       queryFemale += groupByPartFemale;
	      
	       String orderByPartFemale = "";
	       orderByPartFemale += " ORDER BY r.shortname ASC";
	       queryFemale += orderByPartFemale;	
		}
		
		Statement stmt = null;
		
		try {
	        stmt = con.createStatement();
	        ResultSet rset = null;
			/*if(queryTotal != null){
				//Debugging - System.out.println(i + " queryTotal: " + i + queryTotal[i]);
				rset = stmt.executeQuery(queryTotal);
				while (rset.next()){
					//check if the district name matches that in the query if not, count is 0
					for(int j = 0; j < districtNo; j++) {
						if(districts[regionID][j] != null && districts[regionID][j].equalsIgnoreCase(rset.getString("district"))){
							totalsd[regionID][j][0] = rset.getInt("total_count");
						}
						else{
							totalsd[regionID][j][0] = 0;
						}
					}
                }
			}*/
			if(queryMale != null){
				//Debugging - System.out.println(i + " queryMale: " + queryMale[i]);
				rset = stmt.executeQuery(queryMale);
				while (rset.next()){
					//check if the district name matches that in the query if not, count is 0
	                for(int j = 0; j < districtNo; j++) {
	                	if(districts[regionID][j] != null && districts[regionID][j].equalsIgnoreCase(rset.getString("district"))){
	                		totalsd[regionID][j][0] = rset.getInt("total_count");
	                    }
	                }
	            }
			}
			if(queryFemale != null){
				//Debugging - System.out.println(i + " queryFemale: " + queryFemale[i]);
				rset = stmt.executeQuery(queryFemale);
				while (rset.next()){
					//check if the district name matches that in the query if not, count is 0
	                for(int j = 0; j < districtNo; j++) {
	                	if(districts[regionID][j] != null && districts[regionID][j].equalsIgnoreCase(rset.getString("district"))){
	                		totalsd[regionID][j][1] = rset.getInt("total_count");
	                    }
	                }
	            }
			}
		rset.close();
		}catch (SQLException s){
			System.out.println(s.getMessage() + " report SQL code does not execute.");
	    }finally {
	        if (stmt != null) { 
	        	try {
					stmt.close();
					//con.close();
	        	} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
	        	} 
	        }
	    }
		
		for(int i = 0; i < districtNo; i++){
			if(districts[regionID][i] != null){
				
				int totals = totalsd[regionID][i][0] + totalsd[regionID][i][1];
				//System.out.println(populationForRegions[i]); 
				
				xmlData.append("          <point name=\"" + districts[regionID][i].replaceAll("'", "&#39;") + "\">");
				xmlData.append("            <attributes>");
				xmlData.append("              <attribute name=\"Y\">" + totals + "</attribute>");    							//Total number of people with Referral Reasons
				xmlData.append("              <attribute name=\"MValue\">" + totalsd[regionID][i][0] + "</attribute>");         //Total number of males with Referral Reason in region
				xmlData.append("              <attribute name=\"FValue\">" + totalsd[regionID][i][1] + "</attribute>");         //Total number of females with Referral Reason in region  
			    xmlData.append("            </attributes>");
				xmlData.append("          </point>");
			}
		}
		
		return xmlData;
	}
	
	
	private StringBuffer writeMap(Connection con, String dateRange, String estId, int qucId, String qsText, String[] regions, String[] regionsAmap, String districts[][], int populationForRegions[], String startDate, String endDate){
		
		int totalsd[][][] = new int[15][16][3];
		double nationalAverage = 0.00d;
		int sum = 0;
		int sumRegions = 0;
		double lowerQuartileValue = 0.00d;
		double lowerMiddleQuartileValue = 0.00d;
		double upperMiddleQuartileValue = 0.00d;
		//double upperQuartileValue  = 0.00d;
		DecimalFormat df2 = new DecimalFormat("#0.00");
		DecimalFormat df3 = new DecimalFormat("#0.00");
		df2.setRoundingMode(RoundingMode.HALF_DOWN);
		df3.setRoundingMode(RoundingMode.HALF_UP);
		
		totalsd = diagnosisValueForRegion(con, estId, dateRange, qucId, regions, startDate, endDate);
		
		//Acquiring the total based on the population figures for each region
  		for (int m : populationForRegions){
  			sum += m;
  		}
  		  		
  		//Acquiring the total figure for the number of people with an Diagnosis in each region
  		for (int i= 0; i<14; i++){
  			sumRegions = sumRegions + totalsd[14][i][0];
  		}
  		
  		//Calculation of the Country Rate of people with Diagnosis
  		nationalAverage = (sumRegions/(double)sum) * 1000;
  		
  		//Converting Approach for nationalAverage
  		double nationalAverageDD = new Double(df2.format(nationalAverage));
  		//BigDecimal nationalAverageBD = new BigDecimal(nationalAverage).setScale(3,java.math.RoundingMode.HALF_DOWN);
  		//System.out.println("nationalAverageDD: " + nationalAverageDD);
  		
  		//TESTING 
  		//System.out.println(nationalAverage);
  		//System.out.println(nationalAverageBD);
  		//System.out.println(nationalAverageDD);
  		//System.out.println(sum);
  		//System.out.println(sumRegions);
  		
  		//Calculation of colours based on Trinidad national average
  		lowerQuartileValue = nationalAverageDD/2;
		lowerMiddleQuartileValue = nationalAverageDD/2; 
		upperMiddleQuartileValue = ((nationalAverageDD * 2)*0.75);
		//upperQuartileValue = ((nationalAverageDD * 2)*0.75); //redundant variables
		
		//Decimal Format Conversion
		double lowerQuartileValueDD = new Double(df2.format(lowerQuartileValue));
		double lowerMiddleQuartileValueDD = new Double(df3.format(lowerMiddleQuartileValue)); 
		double upperMiddleQuartileValueDD = new Double(df2.format(upperMiddleQuartileValue));
		//double upperQuartileValueDD = new Double(df3.format(upperQuartileValue)); //redundant variables
		
		//XML Builder for AnyMaps
		StringBuffer xmlData = new StringBuffer();
		
		if(nationalAverage > 0.00){
			xmlData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			
			//Region side of AnyMaps
			xmlData.append("<anychart>"); //Start Tag of XML
			xmlData.append("  <margin all=\"0\" />");
			xmlData.append("	<charts>");
			xmlData.append("		<chart plot_type=\"Map\" name=\"trinidad\">");
			xmlData.append("        	<thresholds>");
			xmlData.append("          		<threshold name=\"thrDiagnosis\">");
			xmlData.append("            		<condition name=\"0 to "+lowerQuartileValueDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\"0\" value_3=\""+lowerQuartileValueDD+"\" color=\"0xFFFFB2\" />");
			xmlData.append("            		<condition name=\""+lowerMiddleQuartileValueDD+" to "+nationalAverageDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\""+lowerMiddleQuartileValueDD+"\" value_3=\""+nationalAverageDD+"\" color=\"0xFECC5C\" />");
			xmlData.append("            		<condition name=\""+nationalAverageDD+" to "+upperMiddleQuartileValueDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\""+nationalAverageDD+"\" value_3=\""+upperMiddleQuartileValueDD+"\" color=\"0xFD8D3C\" />");
			xmlData.append("            		<condition name=\"Greater Than "+upperMiddleQuartileValueDD+"\" type=\"greaterThan\" value_1=\"{%Y}\" value_2=\""+upperMiddleQuartileValueDD+"\" color=\"0xE31A1C\" />");
			xmlData.append("          		</threshold>");
			xmlData.append("        	</thresholds>");		
			xmlData.append("      		<chart_settings>");
			xmlData.append("        		<title enabled=\"true\">");
			xmlData.append("          			<text><![CDATA["+qsText+"]]></text>");
			xmlData.append("          			<font color=\"DarkColor(%Color)\" bold=\"true\" family=\"Tahoma\" size=\"20\" underline=\"true\" />");
			xmlData.append("        		</title>");
			xmlData.append("        		<chart_background enabled=\"false\"></chart_background>");
			xmlData.append("       			<controls>");
			xmlData.append("          			<zoom_panel enabled=\"true\" width=\"60\" position=\"left\" align=\"near\" inside_dataplot=\"true\"></zoom_panel>");
			xmlData.append("          			<navigation_panel enabled=\"true\" position=\"left\" align=\"near\" inside_dataplot=\"true\" />");
			xmlData.append("          			<label position=\"Fixed\" anchor=\"RightBottom\" align=\"Near\" horizontal_padding=\"60\" vertical_padding=\"-5\">");
			xmlData.append("           		 		<font size=\"13\" bold=\"false\" family=\"Verdana\" />");
			xmlData.append("            			<text>Please hover over the Municipalities and click for more detail</text>");
			xmlData.append("            			<background enabled=\"false\">");
			xmlData.append("             				<border enabled=\"false\" />");
			xmlData.append("              				<inside_margin left=\"5\" right=\"5\" top=\"5\" bottom=\"5\" />");
			xmlData.append("            			</background>");
			xmlData.append("         			</label>");
			xmlData.append("          			<label position=\"Fixed\" anchor=\"LeftBottom\" align=\"Near\" horizontal_padding=\"15\" vertical_padding=\"10\">");
			xmlData.append("            			<font size=\"13\" bold=\"true\" family=\"Verdana\" />");
			xmlData.append("            			<text>Trinidad Average Rate for " + qsText + ": " + nationalAverageDD + " per 1,000</text>");
			xmlData.append("            			<background enabled=\"false\">");
			xmlData.append("              				<border enabled=\"false\" />");
			xmlData.append("              				<inside_margin left=\"1\" right=\"1\" top=\"1\" bottom=\"1\" />");
			xmlData.append("            			</background>");
			xmlData.append("          			</label>");
			xmlData.append("        		</controls>");
			xmlData.append("        		<legend enabled=\"true\" ignore_auto_item=\"True\" position=\"right\" align=\"Near\" align_by=\"dataplot\" columns=\"1\">");
			xmlData.append("          			<title>");
			xmlData.append("            			<text>"+qsText+" Rate</text>");
			xmlData.append("            			<text>Rate Values</text>");
			xmlData.append("          			</title>");
			xmlData.append("          			<background>");
			xmlData.append("            			<fill opacity=\"1\" />");
			xmlData.append("          			</background>");
			xmlData.append("          			<items>");
			xmlData.append("            			<item source=\"Thresholds\" thrshold=\"thrDiagnosis\"></item>");
			xmlData.append("          			</items>");
			xmlData.append("        		</legend>");
			xmlData.append("        		<footer enabled=\"true\">");
			xmlData.append("          			<text></text>");
			xmlData.append("          			<font bold=\"True\" underline=\"False\" />");
			xmlData.append("         			<background enabled=\"false\">");
			xmlData.append("            			<border type=\"Solid\" color=\"#AAAAAA\" enabled=\"False\" opacity=\"0.5\" />");
			xmlData.append("            			<inside_margin top=\"15\" bottom=\"0\" />");
			xmlData.append("          			</background>");
			xmlData.append("        		</footer>");
			xmlData.append("        		<data_plot_background enabled=\"false\" />");
			xmlData.append("      		</chart_settings>");
			xmlData.append("      		<data_plot_settings>");
			xmlData.append("        		<map_series source=\"trinidad.amap\" labels_display_mode=\"RegionBoundsNonOverlap\" id_column=\"REGION_ID\">");
			xmlData.append("          			<defined_map_region>");
			xmlData.append("            			<tooltip_settings enabled=\"true\">");
			xmlData.append("              				<format>{%REGION_ID}\\n");
			//xmlData.append("Region: {%Region}\\n");
			xmlData.append("Population: {%Pop}{numDecimals:0}\\n");
			xmlData.append("No. with "+qsText+": {%X}{numDecimals:0}\\n");
			xmlData.append(""+qsText+" Rate: {%Y}{numDecimals:2}</format>");
			xmlData.append("              				<font bold=\"false\" family=\"Tahoma\" size=\"13\"></font>");
			xmlData.append("              				<background>");
			xmlData.append("                				<border enabled=\"false\" type=\"Solid\" color=\"%Color\" />");
			xmlData.append("              				</background>");
			xmlData.append("              				<position anchor=\"CenterTop\" halign=\"Top\" valign=\"Top\" />");
			xmlData.append("            			</tooltip_settings>");
			xmlData.append("          			</defined_map_region>");
			xmlData.append("        		</map_series>");
			xmlData.append("      		</data_plot_settings>");
			xmlData.append("      		<data threshold=\"thrDiagnosis\">");
			xmlData.append(" 				<actions>");
	        xmlData.append(" 					<action type=\"updateChart\" view=\"trinidad\" source_mode=\"internalData\" source=\"{%REGION_ID}\">");
	        xmlData.append("  						<replace token=\"{$region_amap}\"><![CDATA[{%Region}]]></replace>");
	        xmlData.append("  						<replace token=\"{$title}\"><![CDATA[{%REGION_ID}]]></replace>");
	        xmlData.append(" 					</action>");
	        xmlData.append(" 				</actions>");
			xmlData.append("        <series>");
			county(con, xmlData,estId,regions, regionsAmap, totalsd, populationForRegions);  //Method containing XML code of all counties and the applicable data
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//District Side of AnyMaps
			//Chaguanas Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Chaguanas\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Chaguanas</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_x=\"-61.41735077\" centroid_y=\"10.5364206\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Chaguanas", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Chaguanas region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Couva Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Couva/Tabaquite/Talparo\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Couva/Tabaquite/Talparo</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.44054609\" centroid_x=\"-61.31401062\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Couva/Tabaquite/Talparo", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Couva region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Chaguaramas Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Chaguaramas\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Chaguaramas</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_x=\"-61.62574768\" centroid_y=\"10.71323734\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Chaguaramas", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Chaguaramas region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Mayaro Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Mayaro/Rio Claro\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Mayaro/Rio Claro</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.25816813\" centroid_x=\"-61.09771729\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Mayaro/Rio Claro", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Mayaro/Rio Claro region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Penal Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Penal/Debe\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Penal/Debe</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.16085679\" centroid_x=\"-61.43554687\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Penal/Debe", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Penal/Debe region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Point Fortin Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Point Fortin\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Point Fortin</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.17200855\" centroid_x=\"-61.67106628\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Point Fortin", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Point Fortin region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Port of Spain Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Port of Spain\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Port of Spain</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_x=\"-61.52326584\" centroid_y=\"10.66145145\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Port of Spain", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Port-of-Spain region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Princes Town Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Princes Town\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Princes Town</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.20140679\" centroid_x=\"-61.28997803\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Princes Town", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Princes Town region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Siparia Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Siparia\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Siparia</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.14598716\" centroid_x=\"-61.69647217\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Siparia", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Siparia region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			
			//San Fernando Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"San Fernando\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>San Fernando</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.28384253\" centroid_x=\"-61.45614624\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "San Fernando", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the San Fernando region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Sangre Grande Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Sangre Grande\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Sangre Grande</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.65925836\" centroid_x=\"-61.10870361\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Sangre Grande", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Sangre Grande region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//San Juan Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"San Juan/Laventille\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>San Juan/Laventille</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.66870538\" centroid_x=\"-61.4540863\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "San Juan/Laventille", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the San Juan/Laventille region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Tunapuna/Piarco Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Tunapuna/Piarco\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Tunapuna/Piarco</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.68489957\" centroid_x=\"-61.31263733\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Tunapuna/Piarco", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Tunapuna region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			
			//Arima Districts
			xmlData.append("    <chart plot_type=\"Map\" name=\"Arima\" template=\"SubRegion\">");
			xmlData.append("      <chart_settings>");
			xmlData.append("        <title>");
			xmlData.append("          <text>Arima</text>");
			xmlData.append("        </title>");
			xmlData.append("      </chart_settings>");
			xmlData.append("      <data_plot_settings>");
			xmlData.append(" 		<map_series source=\"{$region_amap}.amap\">");
			xmlData.append("          <projection type=\"mercator\" centroid_y=\"10.62872226\" centroid_x=\"-61.28019333\"></projection>");
			xmlData.append("        </map_series>");
			xmlData.append("      </data_plot_settings>");
			xmlData.append("      <data>");
			xmlData.append("        <series>");
			subRegion(con, xmlData, estId, dateRange, qucId, "Arima", districts, totalsd, populationForRegions, startDate, endDate); //Loop of districts within the Arima region
			xmlData.append("        </series>");
			xmlData.append("      </data>");
			xmlData.append("    </chart>");
			xmlData.append("  </charts>");
			
		    //Formatting of District Maps
		    xmlData.append(" <templates>");
		    xmlData.append(" 	<template name=\"SubRegion\">");
		    xmlData.append(" 	  <chart plot_type=\"Map\">");
		    xmlData.append(" 		<thresholds>");
		    xmlData.append("          <threshold name=\"thrDiagnosis\">");
			xmlData.append("            <condition name=\"0 to "+lowerQuartileValueDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\"0\" value_3=\""+lowerQuartileValueDD+"\" color=\"0xFFFFB2\" />");
			xmlData.append("            <condition name=\""+lowerMiddleQuartileValueDD+" to "+nationalAverageDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\""+lowerMiddleQuartileValueDD+"\" value_3=\""+nationalAverageDD+"\" color=\"0xFECC5C\" />");
			xmlData.append("            <condition name=\""+nationalAverageDD+" to "+upperMiddleQuartileValueDD+"\" type=\"between\" value_1=\"{%Y}\" value_2=\""+nationalAverageDD+"\" value_3=\""+upperMiddleQuartileValueDD+"\" color=\"0xFD8D3C\" />");
			xmlData.append("            <condition name=\"Greater Than "+upperMiddleQuartileValueDD+"\" type=\"greaterThan\" value_1=\"{%Y}\" value_2=\""+upperMiddleQuartileValueDD+"\" color=\"0xE31A1C\" />");
			xmlData.append("          </threshold>");
		    xmlData.append(" 		</thresholds>");
		    xmlData.append(" 		<chart_settings>");
		    xmlData.append(" 			<title enabled=\"true\">");
		    xmlData.append(" 		 		<font color=\"DarkColor(%Color)\" bold=\"true\" family=\"Tahoma\" size=\"20\" underline=\"true\" />");
		    xmlData.append(" 			</title>");
		    xmlData.append(" 			<chart_background enabled=\"false\" />");
		    xmlData.append(" 			<data_plot_background enabled=\"false\" />");
		    xmlData.append(" 			<controls>");
		    xmlData.append(" 				<label position=\"Fixed\" anchor=\"LeftBottom\" align=\"Near\" horizontal_padding=\"15\" vertical_padding=\"30\">");
		    xmlData.append(" 					<font size=\"14\" bold=\"true\" family=\"Verdana\" />");
		    xmlData.append("            		<text>Trinidad Average Rate for " + qsText + ": " + nationalAverageDD + " per 1,000</text>");
		    xmlData.append(" 					<background enabled=\"false\">");
		    xmlData.append(" 						<border enabled=\"false\" />");
		    xmlData.append(" 						<inside_margin left=\"1\" right=\"1\" top=\"1\" bottom=\"1\" />");
		    xmlData.append(" 					</background>");
		    xmlData.append(" 				</label>");
		    xmlData.append(" 				<zoom_panel enabled=\"true\" width=\"60\" position=\"left\" align=\"near\" inside_dataplot=\"true\"></zoom_panel>");
		    xmlData.append(" 				<navigation_panel enabled=\"true\" position=\"left\" align=\"near\" inside_dataplot=\"true\" />");
		    xmlData.append(" 			</controls>");
		    xmlData.append(" 			<legend enabled=\"true\" position=\"float\" inside_dataplot=\"true\" anchor=\"RightTop\" horizontal_padding=\"5\" vertical_padding=\"5\" ignore_auto_item=\"true\">");
		    xmlData.append(" 				<title>");
		    xmlData.append(" 					<text>"+qsText+"</text>");
		    xmlData.append(" 				</title>");
		    xmlData.append(" 				<background>");
		    xmlData.append(" 					<fill opacity=\"1\" />");
		    xmlData.append(" 				</background>");
		    xmlData.append("				<items>");
		    xmlData.append("					<item source=\"Thresholds\" thrshold=\"thrDiagnosis\"></item>");
		    xmlData.append("				</items>");
		    xmlData.append("			</legend>");
		    xmlData.append("			<footer enabled=\"true\">");
		    xmlData.append("				<text>Back to Trinidad Map</text>");
		    xmlData.append("				<font size=\"15\" bold=\"true\" family=\"Verdana\" color=\"Blue\" underline=\"true\" />");
		    xmlData.append("				<background enabled=\"true\">");
		    xmlData.append("					<border enabled=\"false\" />");
		    xmlData.append("					<inside_margin left=\"5\" right=\"5\" top=\"5\" bottom=\"5\" />");
		    xmlData.append("				</background>");
		    xmlData.append("				<actions>");
		    xmlData.append("					<action type=\"updateChart\" source_mode=\"internalData\" source=\"Trinidad\" />");
		    xmlData.append("				</actions>");
		    xmlData.append("			</footer>");
		    xmlData.append("		</chart_settings>");
		    xmlData.append("		<data_plot_settings>");
		    xmlData.append("			<map_series>");
		    xmlData.append("			<projection type=\"mercator\" flag=\"true\" />");
		    xmlData.append("			<defined_map_region>");
		    xmlData.append("				<tooltip_settings enabled=\"true\">");
		    xmlData.append("              		<format>{%REGION_ID}\\n");
			xmlData.append("Region: {%Region}\\n");
			xmlData.append("Population: {%Pop}{numDecimals:0}\\n");
			xmlData.append("No. with "+qsText+": {%Y}{numDecimals:0}\\n");
			xmlData.append("No. of males with "+qsText+": {%MValue}{numDecimals:0}\\n");
			xmlData.append("No. of females with "+qsText+": {%FValue}{numDecimals:0}");
			xmlData.append("					</format>");
		    xmlData.append("					<font bold=\"false\" family=\"Tahoma\" size=\"13\" />");
		    xmlData.append("					<background>");
		    xmlData.append("						<border type=\"Solid\" color=\"%Color\" />");
		    xmlData.append("					</background>");
		    xmlData.append("					<position anchor=\"CenterTop\" halign=\"Top\" valign=\"Top\" />");
		    xmlData.append("				</tooltip_settings>");
		    xmlData.append("				<label_settings enabled=\"false\">");
		    xmlData.append("					<format>{%REGION_ID}</format>");
		    xmlData.append("				</label_settings>");
		    xmlData.append("			</defined_map_region>");
		    xmlData.append("		</map_series>");
		    xmlData.append("	</data_plot_settings>");
		    xmlData.append("<data threshold=\"thrDiagnosis\"></data>");
		   
		    xmlData.append(" </chart>");
		    xmlData.append("</template>");
		    xmlData.append("</templates>");
		   
			xmlData.append("</anychart>"); //End Tag for XML
		}
		else{
			xmlData.append("No applicable data for the Diagnosis selected");
		}
		return xmlData;
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

		ConnectDatabase connect = new ConnectDatabase();
	    Connection con = null;
	    
        try {
        	
			con = (Connection)connect.getConnection(con, this.getServletContext());
			
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
        
		//This is the session for the Cellma Reports not the Cellma session
		HttpSession session = request.getSession(false);

		if(session == null) {
			//The session has timed out usually after 30 mins
			ServletContext servletContext = this.getServletContext();
			RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher("/sessionexpired.jsp");
			requestDispatcher.forward(request,response);
			return;
		}
		
		//Session Variable
		String estId = (String)session.getAttribute("estId");
		
		//Variables retrieved from jsp file called displaymapfordiagnosis.jsp from the showMyDiagnosisMap() function
		String dateRange = request.getParameter("dateRange");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		int qucId = Integer.parseInt(request.getParameter("qsId"));
		String qsText = request.getParameter("qsText");
		
		DateUtils du= new DateUtils();
		String dateFormat = du.createSQLDate(startDate).toString();
		startDate = dateFormat;
		dateFormat = du.createSQLDate(endDate).toString();
		endDate = dateFormat;
		/**ARRAYS FOR FOR REGION, DISTRICT AND POPULATION START*/	
  	 	
  	 	//Arrays for regions for AMAP Files
  	 	String regionsAmap[] = new String[14];
	       
  	 	regionsAmap[0] = "Chaguanas";
  	 	regionsAmap[1] = "Couva";
  	 	regionsAmap[2] = "Chaguaramas";
  	 	regionsAmap[3] = "Mayaro";
  	 	regionsAmap[4] = "Penal";
  	 	regionsAmap[5] = "Point Fortin";
  	 	regionsAmap[6] = "Port of Spain";
  	 	regionsAmap[7] = "Princes Town";
  	 	regionsAmap[8] = "Siparia";
  	 	regionsAmap[9] = "San Fernando";
  	 	regionsAmap[10] = "Sangre Grande";
  	 	regionsAmap[11] = "San Juan";
  	 	regionsAmap[12] = "Tunapuna";
  	 	regionsAmap[13] = "Arima";
  	 	
  	 	//Array for Population
  	    int populationForRegions[] = new int[14]; //rows[]||columns[]

  	    populationForRegions[0] = 83692;
  	    populationForRegions[1] = 178828;
  	    populationForRegions[2] = 12966;
  	  	populationForRegions[3] = 35683;
  	  	populationForRegions[4] = 83699;
  		populationForRegions[5] = 20318;
  		populationForRegions[6] = 37522;
  		populationForRegions[7] = 102507;
  		populationForRegions[8] = 87075;
  		populationForRegions[9] = 48976;
  		populationForRegions[10] = 75863;
  		populationForRegions[11] = 157827;
  		populationForRegions[12] = 216293;
  		populationForRegions[13] = 33677;
  		
  	 	//2D Array for Districts with the first array indicating region that the district is in (second array) 
  	    String districts[][] = new String[14][16]; //rows[]||columns[]
  	    
  	    //Districts for use in XML
  	    
  	    //Chaguanas Districts
  	    districts[0][0] = "Felicity";
  	  	districts[0][1] = "Cunupia";
	  	districts[0][2] = "Enterprise";
	  	districts[0][3] = "Montrose";
	  	districts[0][4] = "Edinburgh";
	  	districts[0][5] = "Charlieville";
	  	districts[0][6] = "Caroni Savannah";
	  	
	  	//Couva Districts
	  	districts[1][0] = "Chickland/Mamoral";
	  	districts[1][1] = "Las Lomas/San Rafael";
	  	districts[1][2] = "Longdenville/Talparo";
	  	districts[1][3] = "Piparo/San Pedro/Tabaquite";
	  	districts[1][4] = "Caratal/Tortuga";
	  	districts[1][5] = "Gasparillo/Bonne Aventure";
	  	districts[1][6] = "Claxton Bay/Pointe-A-Pieree";
	  	districts[1][7] = "Balmain/Esperanza/Forres Park";
	  	districts[1][8] = "California/Point Lisas";
	  	districts[1][9] = "Perseverance/Waterloo";
	  	districts[1][10] = "St Mary's/Edinburg"; //Meant To Contain Single Quotes
	  	districts[1][11] = "Freeport/Calcutta";
	  	districts[1][12] = "Felicity/Calcutta/Mc Bean";
	  	
	  	//Chaguaramas Districts
	  	districts[2][0] = "Chaguaramas/Point Cumana";
	  	districts[2][1] = "Bagatelle/Blue Basin";
	  	districts[2][2] = "Covigne/Rich Plain";
	  	districts[2][3] = "Diamondvale";
	  	districts[2][4] = "Glencoe/Goodwood/LA Puerta";
	  	districts[2][5] = "Petit Valley/Cocorite";
	  	districts[2][6] = "Belle vue/Boissiere No 1";
	  	districts[2][9] = "St Lucien/Cameron Hill"; 
	  	districts[2][10] = "Morne Coco/Alyce Glen";
	  	districts[2][11] = "Moka/Boissiere No 2";
	  	
	  	//Mayaro Districts
	  	districts[3][0] = "Biche/Charuma";
	  	districts[3][1] = "Cocal/Mafeking";
	  	districts[3][2] = "Mayaro/Guayaguayare";
	  	districts[3][3] = "Rio Claro South/Cat's Hill"; //Meant To Contain Single Quotes
	  	districts[3][4] = "Rio Claro North";
	  	districts[3][5] = "Ecclesville";
	  	
	  	//Penal/Debe Districts
	  	districts[4][0] = "Quinam/Morne Diablo";
	  	districts[4][1] = "La Fortune";
	  	districts[4][2] = "Palmiste/Hermitage"; 
	  	districts[4][3] = "Debe East/L'Esperance/Union Hall"; //Meant To Contain Single Quotes
	  	districts[4][4] = "Bronte";
	  	districts[4][5] = "Barrackpore West";
	  	districts[4][6] = "Debe West";
	  	districts[4][7] = "Rochard/Barrackpore East";
	  	districts[4][8] = "Penal";
	  	
	  	//Point Fortin Districts
	  	districts[5][0] = "New Village";
	  	districts[5][1] = "Techier/Guapo";
	  	districts[5][2] = "Newlands/Machaica";
	  	districts[5][3] = "Egypt";
	  	districts[5][4] = "Cap De Ville/Fanny Village";
	  	districts[5][5] = "Hollywood";
	  	
	  	//Port of Spain Districts
	  	districts[6][0] = "Northern Port of Spain";
	  	districts[6][1] = "St James West";
	  	districts[6][2] = "Woodbrook";
	  	districts[6][3] = "Southern Port of Spain";
	  	districts[6][4] = "St Anns River South";
	  	districts[6][5] = "St Anns River Central";
	  	districts[6][6] = "St Anns River North";
	  	districts[6][7] = "Belmont";
	  	districts[6][8] = "East Dry River";
	  	
	  	//Princes Town Districts
	  	districts[7][0] = "Reform/Manahambre";
	  	districts[7][1] = "Corinth/Cedar Hill";
	  	districts[7][2] = "Hindustan/Indian Walk/St Mary's"; //Meant To Contain Single Quotes
	  	districts[7][3] = "Inverness/Princes Town";
	  	districts[7][4] = "Ben Lomond/Hardbargain/Williamsville";
	  	districts[7][5] = "Leguna/St Julien";
	  	districts[7][6] = "Fifth Company";
	  	districts[7][7] = "New Grant/Tableland";
	  	districts[7][8] = "Moruga";
	  	
	  	//Siparia Districts
	  	districts[8][0] = "Avocat/San Francique North";
	  	districts[8][1] = "Siparia East/San Francique South";
	  	districts[8][2] = "Siparia West/Fyzabad";
	  	districts[8][3] = "Palo Seco";
	  	districts[8][4] = "Mon Desir";
	  	districts[8][5] = "Otaheite/Rousillac";
	  	districts[8][6] = "Brighton/Vessigny";
	  	districts[8][7] = "Erin";
	  	districts[8][8] = "Cedros";
	  	
	  	//San Fernando Districts
	  	districts[9][0] = "Marabella";
	  	districts[9][1] = "Marabella South/Vistabella";
	  	districts[9][2] = "Cocoyea/Tarouba";
	  	districts[9][3] = "Mon Repos/Navet";
	  	districts[9][4] = "Pleasantville";
	  	districts[9][5] = "Les Efforts East/Cipero";
	  	districts[9][6] = "Springvale/Paradise";
	  	districts[9][7] = "Les Efforts West/La Romain";
	  	
	  	//Sangre Grandre
	  	districts[10][0] = "Manzanilla";
	  	districts[10][1] = "Toco/Fishing Pond North";
	  	districts[10][2] = "Valencia";
	  	districts[10][3] = "Sangre Grande";
	  	districts[10][4] = "Vega De Oropouche";
	  	districts[10][5] = "Cumuto/Tamana";
	  	districts[10][6] = "Sangre Grande South";
	  	districts[10][7] = "Toco/Fishing Pond South";
	  	
	  	//San Juan/Laventille Districts
	  	districts[11][0] = "Maracas Bay/Santa Cruz/La Fillette";
	  	districts[11][1] = "Aranguez/Warner Village";
	  	districts[11][2] = "Beetham/Picton";
	  	districts[11][3] = "Success/Trou Macaque";
	  	districts[11][4] = "Barataria";
	  	districts[11][5] = "Petit Bourg/Mount Lambert/Champs Fleurs";
	  	districts[11][6] = "San Juan";
	  	districts[11][7] = "Caledonia/Upper Malick";
	  	districts[11][8] = "Morvant";
	  	districts[11][9] = "St Barb's/Chinapoo"; //Meant To Contain Single Quotes
	  	districts[11][10] = "St Anns/Cascade/Mon Repos West";
	  	districts[11][11] = "Febau/Bourg/Mulatresse";
	  	
	  	//Tunapuna/Piarco Districts
	  	districts[12][0] = "Kelly Village/Warrenville";
	  	districts[12][1] = "St Augustine South/Piarco/St Helena";
	  	districts[12][2] = "Blanchisseuse/Santa Rosa";
	  	districts[12][3] = "Valsayn/St Joseph";
	  	districts[12][4] = "Curepe/Pasea";
	  	districts[12][5] = "Maracas/Santa Magarita";
	  	districts[12][6] = "Auzonville/Tunapuna";
	  	districts[12][7] = "Macoya/Trinicty";
	  	districts[12][8] = "Five Rivers/Lopinot";
	  	districts[12][9] = "Caura/Paradise/Tacarigua";
	  	districts[12][10] = "La Florissante/Cleaver";
	  	districts[12][11] = "Mausica/Maloney";
	  	districts[12][12] = "Bon Air/Arouca/Cane Farm";
	  	districts[12][13] = "D'Abadie/Carapo";    //Meant To Contain Single Quotes
	  	districts[12][14] = "Wallerfield/La Horquetta";
	  		
	  	//Arima Districts
	  	districts[13][0] = "Arima West/O'Meara";  //Meant To Contain Single Quotes
	  	districts[13][1] = "Tumpuna";
	  	districts[13][2] = "Malabar";
	  	districts[13][3] = "Arima"; 
	  	districts[13][4] = "Calvary";
	  	
	  	/**ARRAYS FOR FOR REGION, DISTRICT AND POPULATION END*/	
	  	
		//Taking in the map data and generating it to a new html file
		response.setContentType("text/html");
		PrintWriter out1 = response.getWriter();
		StringBuffer xmlData = new StringBuffer();
			
		out1.println("<!DOCTYPE HTML PUBLIC \"//W3C//DTD HTML 4.01 Transitional//EN\">");
		out1.println("<HTML>");
		out1.println("  <HEAD>");
		out1.println("  <TITLE>Trinidad Map for Diagnosis</TITLE>");
		out1.println("    <!--[if gte IE 7]>"
				+ "			<link type=\"text/css\" rel=\"stylesheet\" href=\"./css/loader.css\"></link>"
				+ "			<![endif]-->"
				+ "		  <!--[if !IE]>-->"
				+ "			<link type=\"text/css\" rel=\"stylesheet\" href=\"./css/loader-non-ie.css\"></link>"
				+ "		  <!--<![endif]-->");
		out1.println("    <script type=\"text/javascript\" language=\"javascript\" src=\"./js/AnyChart.js\"></script>");
		out1.println("	  <script type=\"text/javascript\" src=\"./js/tooltip.js\"></script>");
		out1.println("	  <script type=\"text/javascript\" language=\"JavaScript\">");
		out1.println("    	<!--");
		out1.println(" 			initToolTips();");
		out1.println("		//-->");
		out1.println("	  </script>");
		out1.println("  </HEAD>");
		out1.println("  <BODY id='bodyStyle'>");
		out1.println(" <table border=\"0\" align=\"center\" bgColor=\"#e7e7e7\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\">");
		out1.println(" <tr bgColor=\"#FFFFFF\">");
		out1.println(" 		<td background=\"./images/darkGrayLeft.gif\" height=\"21\" width=\"8\">&nbsp;</td>");
		out1.println(" 		<td class=\"slimDataHeader2\" align=\"center\" background=\"./images/darkGrayCenter.gif\"><font color=\"#FFFFFF\"><b class=\"spacing\">Trinidad Diagnosis Map</b></font></td>");
		out1.println(" 		<td background=\"./images/darkGrayRight.gif\" height=\"21\" width=\"12\">&nbsp;</td>");
		out1.println(" </tr>");	
		out1.println(" <tr bgColor=\"#e7e7e7\">");
		out1.println(" 		<td colspan=\"3\">");
		out1.println(" 			<table bgColor=\"#e7e7e7\" cellpadding=\"1\" width=\"100%\">");
		out1.println(" 				<tr>");
		out1.println(" 					<td colspan=\"3\">");
		out1.println(" 						<table width=\"100%\" cellpadding=\"4\" cellspacing=\"0\" align=\"center\">");
		out1.println(" 							<tr bgColor=\"#FFFFFF\">");
		out1.println(" 								<td align=\"left\"><img src=\"./images/famfamfamsilk/bigicons/reports.png\" /></td>");
		out1.println("								<td align=\"center\" width=\"100%\" style=\"margin-left: auto; margin-right: auto\"><p style=\"font-weight:bold; font-size: 15px; color: #006699; text-align: center\"><font color='BLACK'>Diagnosis Selected: </font>"+qsText+"<br/><font color='BLACK'>Date Range Selected: </font>"+dateRange+"<br/><font color='BLACK'>Total Population: </font>1,174,926</td>");
		out1.println("								<td align=\"right\"><a href=\"./displaymapfordiagnosis.jsp\"><img src=\"./images/famfamfamsilk/bigicons/arrow_left.png\" onMouseOver=\"toolTip('Back');\" onMouseOut=\"toolTip();\" border=\"0\" /></a></td>");
		out1.println("								<td align=\"right\"><a href=\"./chooseyourmap.jsp\"><img src=\"./images/famfamfamsilk/bigicons/arrow_up.png\" onMouseOver=\"toolTip('Back to Choose Your Map');\" onMouseOut=\"toolTip();\" border=\"0\" /></a></td>");
		out1.println(" 							</tr>");
		out1.println(" 						</table>");
		out1.println(" 					</td>");
		out1.println(" 				</tr>");
		out1.println(" 			</table>");
		out1.println(" 		</td>");
		out1.println(" </tr>");
		out1.println(" <tr>");
		out1.println(" 		<td colspan=\"3\" bgColor=\"white\" align=\"center\">");
		out1.println("	  		<script type=\"text/javascript\" language=\"javascript\">");
		out1.println("			//<![CDATA[");
		out1.println("			var map = new AnyChart('./swf/AnyChart.swf', './swf/Preloader.swf');");
		out1.println("			map.width = 950;");
		out1.println("			map.height = 660;");
		
		xmlData = this.writeMap(con, dateRange, estId, qucId, qsText, regions, regionsAmap, districts, populationForRegions, startDate, endDate);
		
		String xmlDataReplaced = xmlData.toString();
		xmlDataReplaced = xmlData.toString().replaceAll("\"", "'");
		
		out1.println("			map.setData(\"" + xmlDataReplaced + "\");");
		out1.println("			map.write();");
		out1.println("  		//]]>");
		out1.println("    	</script>");
		out1.println(" 		</td>");
		out1.println("	</tr>");
		out1.println("</table>");
		out1.println("  </BODY>");
		out1.println("</HTML>");
		
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
