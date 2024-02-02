package com.sbi.oem.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static Date convertISTtoUTC(Date fromDate) {
		try {
			long longDate=fromDate.getTime();
			longDate=longDate-19800000;
			Date updatedDate=new Date(longDate);
			return updatedDate;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}
		
	}
	
	public static Date convertUTCTOIST(Date fromDate) {
		long longDate=fromDate.getTime();
		longDate=longDate+19800000;
		Date updatedDate=new Date(longDate);
		return updatedDate;
	}
	
	public static Date convertDateToNigh12AM(Date recommendDate) throws ParseException {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = date.format(recommendDate);
		SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateStrings = dateString + " 23:59:00";
		Date updatedRecommendedDate = time.parse(dateStrings);
		return updatedRecommendedDate;
	}
}
