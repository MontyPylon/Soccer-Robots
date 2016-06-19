package camera;

import org.opencv.core.Point;

public class RobotHistory {
	
	int num;	
	long epochMs;
	Point centerPoint;
	double angle;
	String command;
	
	public RobotHistory(int num, long epochMs, Point centerPoint, double angle) {
		this.num = num;
		this.epochMs = epochMs;
		this.centerPoint = centerPoint;
		this.angle = angle;
	}
	
	public RobotHistory(int num, long epochMs, Point centerPoint, double angle, String command) {
		this.num = num;
		this.epochMs = epochMs;
		this.centerPoint = centerPoint;
		this.angle = angle;
		this.command = command;
	}
	
	public String toString() {
		return num+ " "+epochMs+" "+Util.printPoint(centerPoint)+" "+Util.printDouble(angle);
	}

}
