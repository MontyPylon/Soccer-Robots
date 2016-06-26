package camera;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class SocketClient {
	
	Logger log = Logger.getLogger(SocketClient.class);
    public static final int dbg = 0;
    public static final String dname = "SOCCL";
	
	Socket clientSocket;
	DataOutputStream os;
	DataInputStream is;
	
	public static void main(String args[]) {
		SocketClient sc = new SocketClient();
        //sc.test1();
	}
	
	public boolean init(String ip, int port) {
		try {
			clientSocket = new Socket(ip, port);
			os = new DataOutputStream(clientSocket.getOutputStream());
			is = new DataInputStream(clientSocket.getInputStream());
 			return true;
		} catch (Exception e) {
			if(dbg > 0) log.error("",e);
			return false;
		} 
	}
	
	public void sendAction(String s) {
		try {
			// log.info(dname+" Client sending [" + s+"]");
			
			os.writeUTF(s);
			//os.flush();
			if (dbg>1) log.info(dname+" Client sent [" + s + "]");
			
            //String resp = is.readUTF();
			//if (dbg>1) log.info(dname+" Client got [" + resp+"]");
			
			//rs = ResultSet.parse(resp);
 
		} catch (Exception e) {
			log.error("",e);
		}
	}
	
	public void close() {
		try {
			os.close();
			is.close();
			clientSocket.close(); 
 			
		} catch (Exception e) {
			log.error("",e);
		} 
		
	} 
}
			




