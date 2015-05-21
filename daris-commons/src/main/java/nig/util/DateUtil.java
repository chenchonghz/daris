package nig.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class DateUtil {

	/*
	 * Convert date to MF format dd-MMM-yyyy HH:mm:ss (time part optional)
	 * 
	 */
	public static String formatDate (java.util.Date date, Boolean withTime, Boolean setTimeToNull) throws Throwable {
		// Format the new date in MF date format\
		if (withTime) {
			if (setTimeToNull) {
				return formatDate(date, "dd-MMM-yyyy 00:00:00");
			} else {
				return formatDate(date, "dd-MMM-yyyy HH:mm:ss");
			}
		} else {
			return formatDate(date, "dd-MMM-yyyy");
		}
	}

	/*
	 * Convert date to MF format dd-MMM-yyyy HH:mm:ss
	 * 
	 */
	public static String formatDate (java.util.Date date, String pattern) throws Throwable {
		// Format the new date in MF date format
		SimpleDateFormat formatterOut = new SimpleDateFormat (pattern);
		return formatterOut.format(date);
	}

	/**
	 * Convert date string between given  patterns
	 * 
	 * @param dateIn
	 * @param patternIn
	 * @param patternOut
	 * @return
	 * @throws Throwable
	 */
	public static String convertDateString  (String dateIn, String patternIn, String patternOut) throws Throwable  { 
		//
		SimpleDateFormat formatterIn = new SimpleDateFormat (patternIn);

		try {
			Date date = formatterIn.parse(dateIn);
			SimpleDateFormat formatterOut = new SimpleDateFormat (patternOut);
			return formatterOut.format(date);
		} catch (Throwable ex) {
			throw new RuntimeException ("The date string " + dateIn + " is not in an expected format");
		}
	}

	/**
	 * Convert date string with pattern to date
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 * @throws Throwable
	 */
	public static Date dateFromString (String date, String pattern) throws Throwable {
		SimpleDateFormat formatter = new SimpleDateFormat (pattern);

		try {
			return formatter.parse(date);
		} catch (Throwable ex) {
			throw new RuntimeException ("The date string " + date + " is not in an expected format");
		}
	}

	/**
	 * Get today's date in various formats 
	 * style=0 -> ddMMyyyy
	 * style=1 -> YYYY-MM-dd
	 * style=2 -> dd-MMM-YYY
	 */
	public static String  todaysDate(int style) throws Throwable {
		Date date = new Date();
		SimpleDateFormat formatterOut = null;
		//
		if (style==0) {
			formatterOut = new SimpleDateFormat ("ddMMMyyyy");
		} else if (style==1) {
			formatterOut = new SimpleDateFormat ("yyyy-MM-dd");
		} else if (style==2) {
			formatterOut = new SimpleDateFormat ("dd-MMM-yyyy");
		} else {
			throw new RuntimeException("Invalid date style");
		}
		return formatterOut.format(date);
	}

	/**
	 * Get date in days (i.e. 365*year + day_of_year)
	 * 
	 * @param date
	 * @return
	 * @throws Throwable
	 */
	public static Integer dateInDays (Date date) throws Throwable {
		if (date==null) return null;
		//
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int day = c.get(Calendar.DAY_OF_YEAR);
		int year = c.get(Calendar.YEAR);
		//
		Integer d = year*365 + day;		

		return d;
	}


	/**
	 * Get today's time in format DD-MMM-YYYY-HH:MM:SS
	 */
	public static String  todaysTime () throws Throwable {
		Date date = new Date();
		SimpleDateFormat formatterOut = null;
		formatterOut = new SimpleDateFormat ("dd-MMM-yyyy-HH:mm:ss");
		return formatterOut.format(date);
	}


	/**
	 * Compare two date of birth dates.  Ignore any part of the date apart from YYYY-MM-DD
	 * 
	 * @param dob1
	 * @param dob2
	 * @return
	 * @throws Throwable
	 */
	public static boolean areDatesOfBirthEqual (Date dob1, Date dob2) throws Throwable {
		String d1 = formatDate (dob1, "yyyy-MM-dd");
		String d2 = formatDate (dob2, "yyyy-MM-dd");
		return (d1.equals(d2));
	}
}
