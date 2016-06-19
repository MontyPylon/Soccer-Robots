package camera;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.opencv.core.Point;

public class Util {
	
	// 2 digits before decimal point and 2 digits after 
	public static final NumberFormat numFormat = new DecimalFormat("0.00"); 
	// just the hours and smaller
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");

	public static String getStackTrace(Throwable aThrowable) {
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    aThrowable.printStackTrace(printWriter);
	    return result.toString();
    }	
	
	public static String printPoint(Point a) {
		if (a==null) return null;
		return a.x+","+a.y;
	}
	
	public static String printDouble(Double d) {
		if (d==null) return null;
		return numFormat.format(d);
	}
	
	public static String printTime(Long a) {
		if (a==null) return null;
		return dateFormat.format(a);
	}
	
	public static Double divide(Double a, Double b) {
		if (a==null) return null;
		if (b==null) return null;
		if (a==0) return 0D;
		return a / b;
	}
	
	public static boolean isXDec(Double a0) {
		if ( (a0 >=0 && a0 <=90) || (a0>=270 && a0 <=360)) {
			return true;
		}
		return false;
	}
	
	public static boolean isXInc(Double a0) {
		if ( (a0 >=0 && a0 <=90) || (a0>=270 && a0 <=360)) {
			return false;
		}
		return true;
	}
	
	public static boolean isYDec(Double a0) {
		if ( (a0 >=0 && a0 <=90) || (a0>=90 && a0 <=180)) {
			return true;
		}
		return false;
	}
	
	public static boolean isYInc(Double a0) {
		if ( (a0 >=0 && a0 <=90) || (a0>=90 && a0 <=180)) {
			return false;
		}
		return true;
	}
}
