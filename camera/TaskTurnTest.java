package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskTurnTest implements Const {
	
	Logger log = Logger.getLogger(TaskTurnTest.class);
    public static final int dbg = 0;
    public static final String dname = "TASTT";
    
    String commandToSend = null;
    
    double leftPower = 0.0;
	double rightPower = 0.0;
	double turningPower = 100;
	
 	public String perform(Robot robot, Mat image, long timeToTurn, String dir) {
		try {
			
			// --------------------------------------------------- NOW
			// where is robot ?
			Point centerPoint0 = robot.getCenterPoint(0);
			double x0 = centerPoint0.x;
			double y0 = centerPoint0.y;
			
			// which direction is robot pointing ?
			Double a0 = robot.getAngle(0);
			
			// epoch ms now
			long t0 = robot.getEpochMs(0);
			
			// --------------------------------------------------- One frame ago
			// where was robot one frame ago ?
			Point centerPoint1 = robot.getCenterPoint(1);
			double x1 = 0;
			double y1 = 0;
			double a1 = 0;
			long t1 = 0;
			double xDiff = 0;
			double yDiff = 0;
			double angleDiff = 0;
			long timeDiff = 0; 
			double xSpeed = 0;
			double ySpeed = 0;
			double angleSpeed = 0;
			
			if (centerPoint1 != null) {
			    x1 = centerPoint1.x;
			    y1 = centerPoint1.y;
						
				// which direction was robot pointing one frame ago ?
				a1 = robot.getAngle(1);
				
				// epoch ms one frame ago
				t1 = robot.getEpochMs(1);
				
				// --------------------------------------------------- Difference
				xDiff = x0 - x1;
				yDiff = y0 - y1;
				angleDiff = a0 - a1;
				
				// epoch ms diff
				timeDiff = t0 - t1;
				// speeds
				xSpeed = Util.divide(xDiff , new Double(timeDiff));
				ySpeed = Util.divide(yDiff , new Double(timeDiff));
				angleSpeed = angleDiff / timeDiff;
			}
			
			if (dbg>1) log.info(dname+" !!!!!!!! perform1 " +
					" c0="+Util.printPoint(centerPoint0) + 
					" c1="+Util.printPoint(centerPoint1) + 
		            " a0="+Util.printDouble(a0) + 
		            " a1="+Util.printDouble(a1) +
                    " t0="+Util.printTime(t0) + 
                    " t1="+Util.printTime(t1));
			
			if (dbg>1) log.info(dname+" !!!!!!!! perform2 " +
					" xd="+Util.printDouble(xDiff) + 
					" yd="+Util.printDouble(yDiff) + 
		            " ad="+Util.printDouble(angleDiff) + 
		            " td="+timeDiff +
                    " xs="+Util.printDouble(xSpeed) + 
                    " ys="+Util.printDouble(ySpeed) + 
                    " as="+Util.printDouble(angleSpeed));

			turnFor(robot, timeToTurn, timeDiff, dir, angleSpeed, angleDiff);
			commandToSend = formatCommand();
			return commandToSend;
			
		} catch (Exception e) {
			log.error("",e);
		} 
		return "error";
	}

	private void turnFor(Robot robot, long timeToTurn, long timeDiff, String dir, double angSpeed, double angDiff) {
		if(robot.firstTestTurn) {
			robot.timeSinceLastTurn = 0;
			robot.firstTestTurn = false;
		}
		
		robot.timeSinceLastTurn += timeDiff;
		if (dbg>2) log.info(dname+" Time since start = " + robot.timeSinceLastTurn + " added timeDiff: " + timeDiff);
		
		if(robot.timeSinceLastTurn > timeToTurn) {
			leftPower = 0;
			rightPower = 0;
			robot.taskTestTurn = false;
			//double angAccel = angSpeed / timeDiff;
			//if (dbg==0) log.info(dname+" CODE: 1997- time delay = " + robot.timeSinceLastTurn + ", Ang Speed = " + angSpeed + ", delta angle = " + angDiff + ", angular accel = " + angAccel);
		} else {
			turn(dir);
		}
	}

	private void turn(String dir) {
		if(dir.equals("right") || dir.equals("Right")) {
			leftPower = turningPower;
			rightPower = -turningPower;
			if (dbg>2) log.info(dname+" Turning Right");
		} else if(dir.equals("left") || dir.equals("Left")) {
			leftPower = -turningPower;
			rightPower = turningPower;
			if (dbg>2) log.info(dname+" Turning Left");
		}
	}
	
private String formatCommand() {
		
		String leftCommand = "";
		double leftScaled = (leftPower / 100) * 255;
		int leftInt = (int) leftScaled;
		
		String rightCommand = "";
		double rightScaled = (rightPower / 100) * 255;
		int rightInt = (int) rightScaled;
		
		if(leftInt < 0) {
			leftCommand += "-";
		} else {
			leftCommand += "+";
		}

		if(Math.abs(leftInt) < 100 && Math.abs(leftInt) > 10) {
			leftCommand += "0" + Math.abs(leftInt);
		} else if(Math.abs(leftInt) < 10 && Math.abs(leftInt) > 0) {
			leftCommand += "00" + Math.abs(leftInt);
		} else if(Math.abs(leftInt) == 0) {
			leftCommand += "000";
		} else {
			leftCommand += Math.abs(leftInt);
		}
		
		if(rightInt < 0) {
			rightCommand += "-";
		} else {
			rightCommand += "+";
		}

		if(Math.abs(rightInt) < 100 && Math.abs(rightInt) > 10) {
			rightCommand += "0" + Math.abs(rightInt);
		} else if(Math.abs(rightInt) < 10 && Math.abs(rightInt) > 0) {
			rightCommand += "00" + Math.abs(rightInt);
		} else if(Math.abs(rightInt) == 0) {
			rightCommand += "000";
		} else {
			rightCommand += Math.abs(rightInt);
		}
		
		String finalCommand = leftCommand + rightCommand + "x";
		
		return finalCommand;
	}
	
}
