package org.rosuda.JGR.util;

import java.io.*;
import java.util.*;

/**
 *  ErrorMSG
 * 
 * 	print error stacktrace to JGRError.log in the property "user.dir"
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

public class ErrorMsg {

    /**
     * Create new ErrorMsg which will be appended to JGRError.log file.
     * @param e Exception to add
     */
    public ErrorMsg(Exception e) {
        String curDir = System.getProperty("user.dir");
        String filename = curDir + "/JGRError.log";
        String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep",
            "Oct", "Nov", "Dec"};
        //e.printStackTrace();
        String error = "--------------------------------------\n\n";
        Calendar cal = new GregorianCalendar();

        // Get the components of the time
        int hour12 = cal.get(Calendar.HOUR); // 0..11
        int hour24 = cal.get(Calendar.HOUR_OF_DAY); // 0..23
        int min = cal.get(Calendar.MINUTE); // 0..59
        int sec = cal.get(Calendar.SECOND); // 0..59
        int ms = cal.get(Calendar.MILLISECOND); // 0..999
        int ampm = cal.get(Calendar.AM_PM);
        int era = cal.get(Calendar.ERA); // 0=BC, 1=AD
        int year = cal.get(Calendar.YEAR); // 2002
        int month = cal.get(Calendar.MONTH); // 0=Jan, 1=Feb, ...
        int day = cal.get(Calendar.DAY_OF_MONTH); // 1...
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, ...

        error += day + "." + months[month] + "." + year + "  " + hour24 + ":" +
            min + "\n\n";
        error += "Message : " + e.getMessage() + "\n\n";
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
                filename, true)));
            out.write(error);
            out.flush();
            e.printStackTrace(out);
            out.flush();
            out.write("\n\n--------------------------------------\n\n");
            out.flush();
            out.close();
        }
        catch (IOException err) {
            err.printStackTrace();
        }
    }
}