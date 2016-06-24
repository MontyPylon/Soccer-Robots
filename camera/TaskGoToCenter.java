package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskGoToCenter implements ITask, Const {
	
	Logger log = Logger.getLogger(TaskGoToCenter.class);
    public static final int dbg = 4;
    public static final String dname = "TGOTC";
    
    public static final String AAAAA = " !!!!!!!! ";
	
	final int buffer = 30;
	
	double leftPower = 0.0;
	double rightPower = 0.0;

	//speed to go forward at
	double forwardPower = 20;
	double correction = 0.5;
	
	String commandToSend;
	String previousCommand;
	
 	public String perform(Robot robot, Mat image) {
		try {
			
			// --------------------------------------------------- NOW
			// where is robot ?
			Point centerPoint0 = robot.getCenterPoint(0);
			double x0 = centerPoint0.x;
			double y0 = centerPoint0.y;
			
			// which direction is robot pointing ?
			double a0 = robot.getAngle(0);
			
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
				
				if(Math.abs(a0 - a1) > 180) {
					//reached overflow angle
					if(a1 > a0) {
						//a1 is in 350s, a0 just reached 0s
						a1 -= 360;
						if (dbg>1) log.info(dname+" overflow angle zone 1, angle: " + a1 + ", newAngle: " + a0);
					} else if(a0 > a1) {
						//a0 is in 0s, a1 just reached 350s
						a0 += 360;
						if (dbg>1) log.info(dname+" overflow angle zone 2, angle: " + a1 + ", newAngle: " + a0);
					}
				}
				angleDiff = a0 - a1;
				
				// epoch ms diff
				timeDiff = t0 - t1;
				
				// how fast is robot travelling (line speed) ?
				
				// how fast is robot turning (rotation speed) ?
				
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

			// PERFORM MAIN CODE HERE
			double desiredAngle = findDesiredAngle(x0, y0, a0);
			goToCenter(x0, y0, a0, desiredAngle, robot);
			commandToSend = formatCommand();
			if(dbg>1) log.info(dname + " final command going forwards: " + commandToSend);
			return commandToSend;
			
			
		} catch (Exception e) {
			log.error("",e);
		}
		return "error";
	}

	private void goToCenter(double x, double y, double angle, double desiredAngle, Robot robot) {
		double xMinBuffer = CENTER_X - buffer;
		double xMaxBuffer = CENTER_X + buffer;
		double yMinBuffer = CENTER_Y - buffer;
		double yMaxBuffer = CENTER_Y + buffer;
		
		//slightly to the left of straight path
		double diff = angle - desiredAngle;
		double correctionPower = Math.abs(diff) * correction;
		
		if(correctionPower > 40) {
			correctionPower = 40;
		}
		
		if(angle > desiredAngle) {
			//slightly to the right of straight path, so go left to correct
			leftPower = forwardPower - correctionPower;
			rightPower = forwardPower + correctionPower;
			if(dbg>1) log.info(dname + " Go left a little, angle: " + angle + ", desiredAngle: " + desiredAngle);
			if(dbg>1) log.info(dname + " Correction: " + correctionPower + ", Left Power: " + leftPower + ", Right Power: " + rightPower);
		} else if(angle < desiredAngle) {
			//slightly to the left of straight path, so go right to correct
			leftPower = forwardPower + correctionPower;
			rightPower = forwardPower - correctionPower;
			if(dbg>1) log.info(dname + " Go left a little, angle: " + angle + ", desiredAngle: " + desiredAngle);
			if(dbg>1) log.info(dname + " Correction: " + correctionPower + ", Left Power: " + leftPower + ", Right Power: " + rightPower);
		}
		
		if(x > xMinBuffer && x < xMaxBuffer) {
			if(dbg>1) log.info(dname + " IN DESIRED X ZONE");
		}
		
		if(y > yMinBuffer && y < yMaxBuffer) {
			if(dbg>1) log.info(dname + " IN DESIRED Y ZONE");
		}
		
		if((x > xMinBuffer && x < xMaxBuffer) && (y > yMinBuffer && y < yMaxBuffer)) {
			if(dbg>1) log.info(dname + " IN BOTH DESIRED ZONE");
			leftPower = 0;
			rightPower = 0;
			robot.taskGoToCenter = false;
		}
	}

	private String formatCommand() {
		
		String leftCommand = "";
		double leftScaled = (leftPower / 100) * 255;
		int leftInt = (int) leftScaled;
		
		if(leftInt > 255) {
			leftInt = 255;
		} else if(leftInt < -255) {
			leftInt = -255;
		}
		
		String rightCommand = "";
		double rightScaled = (rightPower / 100) * 255;
		int rightInt = (int) rightScaled;
		
		if(rightInt > 255) {
			rightInt = 255;
		} else if(rightInt < -255) {
			rightInt = -255;
		}
		
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

	private double findDesiredAngle(double xPos, double yPos, double curAngle) {
		
		double xDiff = xPos - CENTER_X;
		double yDiff = yPos - CENTER_Y;
		double yOverX = yDiff / xDiff;
		double radAngle = Math.atan(yOverX);
		double trueAngle = Math.toDegrees(radAngle);
		
		if(yPos < CENTER_Y && xPos > CENTER_X) {
			//Quadrant I
			trueAngle += 360;
			if (dbg>1) log.info(dname+" QUAD I");
		} else if(yPos > CENTER_Y && xPos < CENTER_X) {
			//Quadrant III
			trueAngle += 180;
			if (dbg>1) log.info(dname+" QUAD III");
		} else if(yPos < CENTER_Y && xPos < CENTER_X) {
			//Quadrant II
			trueAngle += 180;
			if (dbg>1) log.info(dname+" QUAD II");
		}
		
		if (dbg>1) log.info(dname+" xDiff: " + xDiff + ", yDiff: " + yDiff + ", trueAngle: " + trueAngle);
		
		
		return trueAngle;
	}
	
}
