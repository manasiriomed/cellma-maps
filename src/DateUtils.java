

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public Date createDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year,month-1,day);
        return new Date(c.getTimeInMillis());       
    }
    
    public java.sql.Date createSQLDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year,month-1,day);
        return new java.sql.Date(c.getTimeInMillis());      
    }
    
    
    
    
    
    public java.sql.Date createSQLDate(String inDate) {
    	/** expects inDate in dd/mm/yyyy format */
    	
		int date = 0;
		int month = 0;
		int year = 0;
		date = new Integer(inDate.substring(0,2)).intValue();
		month = new Integer(inDate.substring(3,5)).intValue();
		year = new Integer(inDate.substring(6)).intValue();    	
    	
        Calendar c = Calendar.getInstance();
        c.set(year,month-1,date);
        return new java.sql.Date(c.getTimeInMillis());      
    }    
    
    
    public java.sql.Date createCurrentSQLDate() {
        Calendar c = Calendar.getInstance();
        return new java.sql.Date(c.getTimeInMillis());      
    }
    
    public java.sql.Time createCurrentSQLTime() {
        Calendar c = Calendar.getInstance();
        return new java.sql.Time(c.getTimeInMillis());      
    }    
    
    public java.sql.Timestamp createCurrentTimestamp() {
        Calendar c = Calendar.getInstance();
        return new java.sql.Timestamp(c.getTimeInMillis());         
    }

    public java.sql.Timestamp convertToTimestamp(String inDate, String inTime) {
    	/** expects inDate in dd/mm/yyyy format and inTime in hh:mm:ss */
        Calendar c = Calendar.getInstance();
        
		int date = 0;
		int month = 0;
		int year = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		
		date = new Integer(inDate.substring(0,2)).intValue();
		month = new Integer(inDate.substring(3,5)).intValue();
		year = new Integer(inDate.substring(6)).intValue();    	
    	
		if(inTime != null && inTime.length()>0){
			hour = new Integer(inTime.substring(0,2)).intValue();
			minute = new Integer(inTime.substring(3,5)).intValue();
			second = new Integer(inTime.substring(6)).intValue();  
			c.set(year, month-1, date, hour, minute, second);
		}
		else{
			
			c.set(year,month-1,date,hour,minute,second);
	    }
		       
        return new java.sql.Timestamp(c.getTimeInMillis());         
    }

    public java.sql.Date convertTimestampToDate(String timeStamp) {
    	/** expects timeStamp in yyyy-mm-dd hh:mm:ss */
		int date = 0;
		int month = 0;
		int year = 0;
		int hour = 0;
		int minute = 0;
		int second = 0;
		
		year = new Integer(timeStamp.substring(0,4)).intValue();    	
		month = new Integer(timeStamp.substring(5,7)).intValue();
		date = new Integer(timeStamp.substring(8,10)).intValue();
    	
        Calendar c = Calendar.getInstance();
        c.set(year,month-1,date);
        return new java.sql.Date(c.getTimeInMillis());		       
    }

    public String convert(String fromFormat, String toFormat, String date){
	    //calling the trim function as some of the substrings here do not have
	    //end position hence if you have space at the end of the date without the
	    //trim the date returned would not be accepted by the database and hence
	    // 0000-00-00 would be entered in the date field in the table.
	    date = date.trim();
	    String convertedString = "";
	    String temp = "";
	    if (fromFormat.equals("dd/mm/yyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(6) + date.substring(3,5) + date.substring(0,2);
	    }	    
	
	    if (fromFormat.equals("yyyy/mm/dd") && toFormat.equals("yyyymmdd")) {
	        temp = date.substring(0,4) + date.substring(5,7) + date.substring(8);
	    }    
	    
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(0,4) + date.substring(5,7) + date.substring(8);
	    }
	    
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("dd mm yyyy")) {
	    	temp = date.substring(8) + " " + date.substring(5,7) + " " + date.substring(0,4);
		}	    
	    
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("dd mm yy")) {
	    	temp = date.substring(8) + " " + date.substring(5,7) + " " + date.substring(2,4);
		}	    
	
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("ddmmyyyy")) {
	    	temp = date.substring(8) + date.substring(5,7) + date.substring(0,4);
		}	
	    
	    if (fromFormat.equals("yyyy mm dd") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(0,4) + date.substring(5,7) + date.substring(8);
	    }
	
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("dd/mm/yyyy")) {
	      temp = date.substring(8) + "/" + date.substring(5,7) + "/" + date.substring(0,4);
	    }
	
	    if (fromFormat.equals("yyyymmdd") && toFormat.equals("dd/mm/yyyy")) {
	      temp = date.substring(6) + "/" + date.substring(4,6) + "/" + date.substring(0,4);
	    }
	
	    if (fromFormat.equals("dd/mm/yyyy") && toFormat.equals("ddmmyyyy")) {
	      temp = date.substring(0,2) + date.substring(3,5) + date.substring(6);
	    }
	
	    //new added incase pas import needed them
	    if (fromFormat.equals("ddmmyyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(4) + date.substring(2,4) + date.substring(0,2);
	    }
	
	    if (fromFormat.equals("mmddyyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(4) + date.substring(0,2) + date.substring(2,4);
	    }
	
	    if (fromFormat.equals("mm/dd/yyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(6) + date.substring(0,2) + date.substring(3,5);
	    }
	
	    if (fromFormat.equals("mm-dd-yyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(6) + date.substring(0,2) + date.substring(3,5);
	    }
	
	    if (fromFormat.equals("dd-mm-yyyy") && toFormat.equals("yyyymmdd")) {
	      temp = date.substring(6) + date.substring(3,5) + date.substring(0,2);
	    }
	    
	    //new added for correct formatting in output of tiled labels
	    if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("dd-mm-yyyy")) {
	        	temp = date.substring(8) + "-" + date.substring(5,7) + "-" + date.substring(0,4);
	    }
	    
	    if (fromFormat.equals("dd/mm/yyyy") && toFormat.equals("yyyy-mm-dd")) {
	    	temp = date.substring(6) + "-" + date.substring(3,5) + "-" + date.substring(0,2);
	    }
	   
	    convertedString = temp;
	    return convertedString;
    }
    
    public String convertToSimpleDateFormat(String fromFormat, String toFormat, Date date){
    	
    	String convertedString = "";
	    String temp = "";
    	if (fromFormat.equals("yyyy-mm-dd") && toFormat.equals("dd MMMM yyyy")) {
			SimpleDateFormat format = new SimpleDateFormat();
 	    	format = new SimpleDateFormat(toFormat, Locale.ENGLISH);
 	    	temp = format.format(date);
    	}
    	convertedString = temp;
 	    return convertedString;
    }
    /*
     * Calculate DOB
     */
    public Integer calcAge(Date dateOfBirth, Date dateToCalcTo) {
    	int daysAge = 0;
    	int yearsAge = 0;
    	
    	Integer iAge;
    	
    	if (dateToCalcTo.compareTo(dateOfBirth) > 0) {
    		// currentDate is after patDob
    		long patDobDateMillisecondsSince1970 = dateOfBirth.getTime();
    		long currentDateMillisecondsSince1970 = dateToCalcTo.getTime();
    		long timeAgeInMilliSeconds = currentDateMillisecondsSince1970 - patDobDateMillisecondsSince1970;
    		
    		daysAge = (int)(timeAgeInMilliSeconds/86400000);
			yearsAge = (int)(daysAge/365);
    	}
    	
    	iAge = new Integer(yearsAge);
    	
    	return iAge;
    }
    
    public Integer calcAgeMonths(Date dateOfBirth, Date dateToCalcTo) {
    	int daysAge = 0;
    	int monthsAge = 0;
    	
    	Integer iAge;
    	
    	if (dateToCalcTo.compareTo(dateOfBirth) > 0) {
    		// currentDate is after patDob
    		long patDobDateMillisecondsSince1970 = dateOfBirth.getTime();
    		long currentDateMillisecondsSince1970 = dateToCalcTo.getTime();
    		long timeAgeInMilliSeconds = currentDateMillisecondsSince1970 - patDobDateMillisecondsSince1970;
    		
    		daysAge = (int)(timeAgeInMilliSeconds/86400000);
    		monthsAge = (int)(daysAge/30);
    	}
    	
    	iAge = new Integer(monthsAge);
    	
    	return iAge;
    }    
    
   public Integer daysDifference(java.util.Date startDate, java.util.Date endDate) {
	   int daysDifference = 0;
	   Integer difference;
	   
	   if(endDate.compareTo(startDate) > 0) {
		   long startDateMilliSecondsSince1970 = startDate.getTime();
		   long endDateMilliSecondsSince1970 = endDate.getTime();
		   long timeDiffInMilliSeconds = endDateMilliSecondsSince1970 - startDateMilliSecondsSince1970;
		   
		   daysDifference = (int)(timeDiffInMilliSeconds/86400000);
		   
	   }
	   difference = new Integer(daysDifference);
	   
	   return difference;
   }
   
   public String createCurrentDateDDslashMMslashYYYY(){
	   return this.convert("yyyy-mm-dd", "dd/mm/yyyy", this.createCurrentSQLDate().toString());
   }
    
}
