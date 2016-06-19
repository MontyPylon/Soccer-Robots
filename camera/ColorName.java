package camera;

public class ColorName {
	
    public static String[] names;
    
    public static String NONE = "None";
    
    public static void setup() {
    	try {
    		String colorNames = PropertiesLoader.getValue("multipleObjectTracking.colors");
    		names = colorNames.split(",");
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	
    public static boolean isValidName(String name) {
    	for (int i=0; i<names.length; i++) {
    		if (name.equals(names[i])) {
    			return true;
    		}
    	}
    	return false;
    }
	
	/***
	
	// Notes     H        S       V
	// ===============================
	// Red     88,256  180,256  40,223
	// Green   33,100    0,256   0,256
	// Blue    99,132    0,167   0,206
	// Yellow   3, 43    0,256   0,256
	// Orange   0, 65   59,256 154,256
	// Pink   143,190   61,151 131,256
	// Purple 146,179   55,146  45,110
	// 
	**/
}
