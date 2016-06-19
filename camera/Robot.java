package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Point;

public class Robot {
	
	private static final Logger log = Logger.getLogger(Robot.class);
	
	public static final int FRONTLEFT = 0; 
	public static final int FRONTRIGHT = 1;
	public static final int BACKRIGHT = 2;
	public static final int BACKLEFT = 3;
	
	public static final int[] ALL_POSITIONS = new int[] { FRONTLEFT, FRONTRIGHT, BACKRIGHT, BACKLEFT};
	public static final String[] ALL_POSITION_NAMES = new String[] { "FRONT.LEFT", "FRONT.RIGHT", "BACK.RIGHT", "BACK.LEFT"};
	
	String name;
	String[] colourNames = new String[4];
	String ip;
	String port;
	TObjectPair[] pairs = new TObjectPair[3];
	
	TObject fl;
	TObject fr;
	TObject bl;
	TObject br;
	
	//Task task;
	Point centerPoint;
	double angle;
	
	//TEMP VARIABLES FOR TASKS
	boolean isForward;
	boolean switching;
	
	boolean firstTestTurn = true;
	boolean taskTestTurn = true;
	int timeSinceLastTurn = 0;
	
	boolean taskTurnCenter = true;
	
	boolean taskGoToCenter = true;
	//TEMP VARIABLES FOR TASKS
	
	int maxHist = 150;
	RobotHistory[] history = new RobotHistory[maxHist];
	int histNum = 0;
		
	public Robot(String name){	
		setName(name);
		if (RobotName.isValidName(name)) {	
			String lowerName = name.toLowerCase();
			for (int i=0; i<ALL_POSITION_NAMES.length; i++) {
				String posName = ALL_POSITION_NAMES[i];
				posName = posName.toLowerCase();
				colourNames[i] = PropertiesLoader.getValue("multipleObjectTracking.robot."+lowerName+"."+posName);
				if (!ColorName.isValidName(colourNames[i])) {
					throw new IllegalArgumentException("For robot : "+name+" , position ("+posName+"), colour : "+colourNames[i]+" is not a valid colour name");
				}
			}

			ip = PropertiesLoader.getValue("multipleObjectTracking.robot."+lowerName+".ip");
			port = PropertiesLoader.getValue("multipleObjectTracking.robot."+lowerName+".port");
			
		} else {
			throw new IllegalArgumentException("Not a valid robot name : "+name);
		}
	}
	
	public void addHistory(Point point, double angle) {
		histNum++;
		if (histNum >= maxHist) {
			histNum = 0;
		}
		RobotHistory rh = new RobotHistory(histNum, System.currentTimeMillis(), point, angle);
		// log.info("addHistory "+rh);
		history[histNum] = rh;
	}
	
	public void addHistory(Point point, double angle, String command) {
		histNum++;
		if (histNum >= maxHist) {
			histNum = 0;
		}
		RobotHistory rh = new RobotHistory(histNum, System.currentTimeMillis(), point, angle, command);
		// log.info("addHistory "+rh);
		history[histNum] = rh;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public String getPort() {
		return port;
	}
	
	public void setFL(TObject fl) {
		this.fl = fl;
	}
	
	public void setFR(TObject fr) {
		this.fr = fr;
	}
	
	public void setBL(TObject bl) {
		this.bl = bl;
	}
	
	public void setBR(TObject br) {
		this.br = br;
	}
	
	public TObject getFL() {
		return fl;
	}
	
	public TObject getFR() {
		return fr;
	}
	
	public TObject getBL() {
		return bl;
	}
	
	public TObject getBR() {
		return br;
	}

	public Point getCenterPoint(int delta) {
		RobotHistory rh = getHistory(delta);
		if (rh != null) {
			return rh.centerPoint;
		}
		return null;
	}

	public Double getAngle(int delta) {
		RobotHistory rh = getHistory(delta);
		if (rh != null) {
			return rh.angle;
		}
		return null;
	}
	
	public String getCommand(int delta) {
		RobotHistory rh = getHistory(delta);
		if (rh != null) {
			return rh.command;
		}
		return null;
	}
	
	public Long getEpochMs(int delta) {
		RobotHistory rh = getHistory(delta);
		if (rh != null) {
			return rh.epochMs;
		}
		return null;
	}
	
	RobotHistory getHistory(int delta) {
		int num = histNum - delta;
		if (num < 0) {
			num = history.length - 1;
		}
		RobotHistory rh = history[num];
		return rh;
	}
	
	public int getHistorySize() {
		
		return 0;
	}

	public String getColourName(int pos) {
		if (pos < 0 || pos > 3) throw new IllegalArgumentException("Invalid position : "+pos);
		return colourNames[pos];
	}
	
	public String getColourName(String posName) {
		if (posName == null) throw new IllegalArgumentException("Invalid position name : "+posName);
		for (int i=0; i<ALL_POSITION_NAMES.length; i++) {
			if (posName.equals(ALL_POSITION_NAMES[i])) {
				return colourNames[i];
			}
		}
		throw new IllegalArgumentException("Invalid position name : "+posName);
	}
}
