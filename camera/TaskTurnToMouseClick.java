package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskTurnToMouseClick implements ITask {
	
	Logger log = Logger.getLogger(TaskTurnToMouseClick.class);
    public static final int dbg = 4;
    public static final String dname = "TTTMC";
    String commandToSend;
	double leftPower = 0.0;
	double rightPower = 0.0;
	double angleBuffer = 10;
	double forwardPower = 100;
	double turningPower = 30;
	double angleSpeedCheck = 0.1;
	boolean doneTurning = false;
    // current
	double x;
	double y;
	double a;
	long t;
	// previous
	double px;
	double py;
	double pa;
	long pt;
	// delta
	double dx;
	double dy;
	double da;
	long dt;
	// velocity
	double vx;
	double vy;
	double wa;

	public void processData(Robot robot) {
		Point centerPoint0 = robot.getCenterPoint(0);
		x = centerPoint0.x;
		y = centerPoint0.y;
		a = robot.getAngle(0);
		t = robot.getEpochMs(0);
		Point centerPoint1 = robot.getCenterPoint(1);
		if (centerPoint1 != null) {
		    px = centerPoint1.x;
		    py = centerPoint1.y;
			pa = robot.getAngle(1);
			pt = robot.getEpochMs(1);
			dx = x - px;
			dy = y - py;
			
			if(Math.abs(a - pa) > 180) {
				//reached overflow angle
				if(pa > a) {
					//a1 is in 350s, a0 just reached 0s
					pa -= 360;
					if (dbg>1) log.info(dname+" overflow angle zone 1, angle: " + pa + ", newAngle: " + a);
				} else if(a > pa) {
					//a0 is in 0s, a1 just reached 350s
					a += 360;
					if (dbg>1) log.info(dname+" overflow angle zone 2, angle: " + pa + ", newAngle: " + a);
				}
			}
			da = a - pa;
			dt = t - pt;
			vx = Util.divide(dx , new Double(dt));
			vy = Util.divide(dy , new Double(dt));
			wa = da / dt;
		}
		
		if (dbg>1) log.info(dname+" !!!!!!!! perform1 " +
				" c0="+Util.printPoint(centerPoint0) + 
				" c1="+Util.printPoint(centerPoint1) + 
	            " a0="+Util.printDouble(a) + 
	            " a1="+Util.printDouble(pa) +
                " t0="+Util.printTime(t) + 
                " t1="+Util.printTime(pt));
		
		if (dbg>1) log.info(dname+" !!!!!!!! perform2 " +
				" xd="+Util.printDouble(dx) + 
				" yd="+Util.printDouble(dy) + 
	            " ad="+Util.printDouble(da) + 
	            " td="+dt +
                " xs="+Util.printDouble(vx) + 
                " ys="+Util.printDouble(vy) + 
                " as="+Util.printDouble(wa));
	}
	
	public String perform(Robot robot, Mat image) {
		processData(robot);
		
		double mx = NormalWindow.mouseX;
		double my = NormalWindow.mouseY;
		
		double desiredAngle = findDesiredAngle(x, y, mx, my, a);
		closeGapAngularVelocity(a, desiredAngle, da, dt, 150, robot);
		commandToSend = formatCommand();
		return "";
	}
	
	private void closeGapAngularVelocity(double angle, double desiredAngle, double angleDiff, double timeDiff, int milliSecondDelay, Robot robot) {
		
		//Check overlaps in case 359 and 1 are compared
		double upperCheck = (desiredAngle + angleBuffer);
		if(angle > (360 - angleBuffer) && desiredAngle < (angle - 360 + angleBuffer)) {
			upperCheck += 360;
			if (dbg>1) log.info(dname+" IN UPPER CHECK OVERLAP ZONE. angle: " + angle + ", desired angle: " + desiredAngle);
		}
		double lowerCheck = (desiredAngle - angleBuffer);
		if(angle < angleBuffer && desiredAngle > (angle + 360 - angleBuffer)) {
			lowerCheck -= 360;
			if (dbg>1) log.info(dname+" IN LOWER CHECK OVERLAP ZONE. angle: " + angle + ", desired angle: " + desiredAngle);
		}
		
		if (dbg>1) log.info(dname+" upperCheck: " + upperCheck + ", lowerCheck: " + lowerCheck);
		
		//double toBeAngle = (angleDiff * movesAhead) + angle;
		
		double diff1 = angle - desiredAngle;
		//if pos, turn left. if neg, turn right.
		
		double diff2 = 360 - Math.abs(diff1);
		//other angle
		
		//int movesUntilTarget;
		double angleSpeed = angleDiff / timeDiff;
		double predictedAngleDiff = angle + (angleSpeed * milliSecondDelay);
		
		if (dbg>1) log.info(dname+" PREDICTED ANGLE: " + predictedAngleDiff + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
		
		if(predictedAngleDiff < upperCheck && predictedAngleDiff > lowerCheck) {
			//WE ARE GOING TO BE IN TARGET ANGLE
			if (dbg>1) log.info(dname+" GOING TO BE IN TARGET ANGLE!! toBeAngle: " + predictedAngleDiff + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
			leftPower = forwardPower;
			rightPower = forwardPower;
			robot.taskTurnCenter = false;
			return;
		}
		
		if(angle < upperCheck && angle > lowerCheck) {
			//WE ARE IN TARGET ANGLE
			if (dbg>1) log.info(dname+" IN TARGET ANGLE!! toBeAngle: " + angle + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
			leftPower = forwardPower;
			rightPower = forwardPower;
			robot.taskTurnCenter = false;
			doneTurning = true;
			return;
		}
		
		if(angleSpeed < -angleSpeedCheck) {
			
			if(Math.abs(predictedAngleDiff - upperCheck) > 180) {
				if (dbg>1) log.info(dname+" SOMETHING FUNKY IS GOING ON HERE");
				lowerCheck -= 360;
			}
			
			if(predictedAngleDiff < lowerCheck) {
				//WE ARE GOING TO BE IN TARGET ANGLE
				if (dbg>1) log.info(dname+" predicted angle is under the bounds");
				if (dbg>1) log.info(dname+" GOING TO BE IN TARGET ANGLE!! toBeAngle: " + predictedAngleDiff + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
				leftPower = forwardPower;
				rightPower = forwardPower;
				robot.taskTurnCenter = false;
				return;
			}
		} else if(angleSpeed > angleSpeedCheck) {
			
			if(Math.abs(predictedAngleDiff - upperCheck) > 180) {
				if (dbg>1) log.info(dname+" SOMETHING FUNKY IS GOING ON HERE");
				upperCheck += 360;
			}
			
			if(predictedAngleDiff > upperCheck) {
				//WE ARE GOING TO BE IN TARGET ANGLE
				if (dbg>1) log.info(dname+" predicted angle is over the bounds");
				if (dbg>1) log.info(dname+" GOING TO BE IN TARGET ANGLE!! toBeAngle: " + predictedAngleDiff + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
				leftPower = forwardPower;
				rightPower = forwardPower;
				robot.taskTurnCenter = false;
				return;
			}
		}
		
		if(Math.abs(diff1) < Math.abs(diff2)) {
			//check pos or neg
			if(diff1 > 0) {
				//turn left
				if (dbg>1) log.info(dname+" turn left -- angle: " + angle + ", desiredAngle: " + desiredAngle);
				turnLeft();
			} else {
				//turn right
				if (dbg>1) log.info(dname+" turn right -- angle: " + angle + ", desiredAngle: " + desiredAngle);
				turnRight();
			}
		} else {
			//do opposite of diff1
			if(diff1 > 0) {
				//turn right
				if (dbg>1) log.info(dname+" turn right -- angle: " + angle + ", desiredAngle: " + desiredAngle);
				turnRight();
			} else {
				//turn left
				if (dbg>1) log.info(dname+" turn left -- angle: " + angle + ", desiredAngle: " + desiredAngle);
				turnLeft();
			}
		}
	}
	
	private void turnLeft() {
		leftPower = -turningPower;
		rightPower = turningPower;
	}
	
	private void turnRight() {
		leftPower = turningPower;
		rightPower = -turningPower;
	}
	
	private double findDesiredAngle(double xPos, double yPos, double desiredX, double desiredY, double curAngle) {
		
		double xDiff = xPos - desiredX;
		double yDiff = yPos - desiredY;
		double yOverX = yDiff / xDiff;
		double radAngle = Math.atan(yOverX);
		double trueAngle = Math.toDegrees(radAngle);
		
		if(yPos < desiredY && xPos > desiredX) {
			//Quadrant I
			trueAngle += 360;
			if (dbg>1) log.info(dname+" QUAD I");
		} else if(yPos > desiredY && xPos < desiredX) {
			//Quadrant III
			trueAngle += 180;
			if (dbg>1) log.info(dname+" QUAD III");
		} else if(yPos < desiredY && xPos < desiredX) {
			//Quadrant II
			trueAngle += 180;
			if (dbg>1) log.info(dname+" QUAD II");
		}
		
		if (dbg>1) log.info(dname+" xDiff: " + xDiff + ", yDiff: " + yDiff + ", trueAngle: " + trueAngle);
		
		
		return trueAngle;
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

}
