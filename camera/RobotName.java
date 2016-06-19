package camera;

public class RobotName {
    
    public static String[] names;
    
    public static String NONE = "None";
    
    public static void setup() {
    	try {
    		String robotNames = PropertiesLoader.getValue("multipleObjectTracking.robots");
    		names = robotNames.split(",");
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

}
