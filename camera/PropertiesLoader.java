package camera;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
	
    private static Properties props;  
    private static String propsFilename = "multipleObjectTracking.properties";
  
    static {
    	init();    	
    }
    
    protected static void init(){  
    	props = new Properties();
            
    	try{  
        	FileInputStream in = new FileInputStream(propsFilename);
    		props.load(in);  
    		in.close();  
    	} catch (IOException e) {
    		System.err.println("Failed to get properties.");
    		e.printStackTrace();
    	}
    	
    }
	
    public static String getValue(String key){  
    	return (String)props.getProperty(key);  
    }
}
