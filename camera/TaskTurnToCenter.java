package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskTurnToCenter implements Const {
	
	Logger log = Logger.getLogger(TaskTurnToCenter.class);
    public static final int dbg = 4;
    public static final String dname = "TTTCE";
    
    public static final String AAAAA = " !!!!!!!! ";

	double angleBuffer = 10;
	
	double leftPower = 0.0;
	double rightPower = 0.0;

	//speed to turn at
	double turningPower = 30;
	double angleSpeedCheck = 0.1;
	double forwardPower = 20;
	
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
			//closeGap(a0, desiredAngle);
			closeGapAngularVelocity(a0, desiredAngle, angleDiff, timeDiff, 150, robot);
			commandToSend = formatCommand();
			return commandToSend;
			
			
		} catch (Exception e) {
			log.error("",e);
		}
		return "error";
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
		
		/**
		//Check if we are in desired range
		if(angle < upperCheck && angle > lowerCheck) {
			//WE ARE IN TARGET ANGLE
			if (dbg>1) log.info(dname+" IN TARGET ANGLE!! toBeAngle: " + angle + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
			leftPower = 0;
			rightPower = 0;
			robot.taskTurnCenter = false;
			return;
		} else if(angleDiff != 0) {
			
			double angleSpeed = angleDiff / timeDiff;
			double predictedAngleDiff = angleSpeed * milliSecondDelay;
			
			/**
			if(Math.abs(diff1) < Math.abs(diff2)) {
				movesUntilTarget = (int) (Math.abs(diff1) / Math.abs(angleDiff));
				//add one bc casting an int always rounds down
				movesUntilTarget += 1;
			} else {
				movesUntilTarget = (int) (Math.abs(diff2) / Math.abs(angleDiff));
				//add one bc casting an int always rounds down
				movesUntilTarget += 1;
			}
			
			if(movesUntilTarget < 0) {
				if (dbg>1) log.info(dname+" GOING TO BE IN TARGET ANGLE!! movesUntilTarget: " + movesUntilTarget + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
			}
			
			if (dbg>1) log.info(dname+" Moves Until Reached Target Angle " + movesUntilTarget);
			
			if(movesUntilTarget < movesAhead) {
				//WE ARE IN TARGET ANGLE
				if (dbg>1) log.info(dname+" GOING TO BE IN TARGET ANGLE!! movesUntilTarget: " + movesUntilTarget + ", angle: " + angle + ", desiredAngle: " + desiredAngle);
				leftPower = 0;
				rightPower = 0;
				return;
			}
			
		}
		**/
		
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

	private void closeGap(double angle, double desiredAngle) {
		
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
		
		
		//Check if we are in desired range
		if(angle < upperCheck && angle > lowerCheck) {
			//WE ARE IN TARGET ANGLE
			if (dbg>1) log.info(dname+" IN TARGET ANGLE!! angle: " + angle + ", desiredAngle: " + desiredAngle);
			leftPower = 0;
			rightPower = 0;
			return;
		}
		
		double diff1 = angle - desiredAngle;
		//if pos, turn left. if neg, turn right.
		
		double diff2 = 360 - Math.abs(diff1);
		//other angle
		
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
