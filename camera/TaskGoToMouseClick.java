package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskGoToMouseClick implements ITask, Const {
	
	Logger log = Logger.getLogger(TaskGoToMouseClick.class);
    public static final int dbg = 4;
    public static final String dname = "TGOTM";
	
	final int buffer = 30;
	
	double leftPower = 0.0;
	double rightPower = 0.0;

	//speed to go forward at
	double forwardPower = 20;
	double correction = 0.5;
	
	//double xD = NormalWindow.mouseX;
	//double yD = NormalWindow.mouseY;
	boolean doneTurning = false;
	double angleBuffer = 10;
	//speed to turn at
	double turningPower = 30;
	double angleSpeedCheck = 0.1;
	
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
			
			double xD = NormalWindow.mouseX;
			double yD = NormalWindow.mouseY;
			
			double desiredAngle = findDesiredAngle(x0, y0, xD, yD, a0);
			if(!doneTurning) {
				closeGapAngularVelocity(a0, desiredAngle, angleDiff, timeDiff, 150, robot);
			} else {
				goToPoint(x0, y0, xD, yD, a0, desiredAngle, robot);
			}
			commandToSend = formatCommand();
			if(dbg>1) log.info(dname + " final command going forwards: " + commandToSend);
			return commandToSend;
			
			//double desiredAngle = findDesiredAngle(x0, y0, xD, yD, a0);
			//goToPoint(x0, y0, xD, yD, a0, desiredAngle, robot);
			//commandToSend = formatCommand();
			//if(dbg>1) log.info(dname + " final command going forwards: " + commandToSend);
			//return commandToSend;
			//return "+000+000x";
			
			
		} catch (Exception e) {
			log.error("",e);
		}
		return "error";
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
 	
	private void goToPoint(double x, double y, double desiredX, double desiredY, double currentAngle, double desiredAngle, Robot robot) {
		
		if(dbg>1) log.info(dname + " Desired x: " + desiredX + ", desired y: " + desiredY);
		
		double xMinBuffer = desiredX - buffer;
		double xMaxBuffer = desiredX + buffer;
		double yMinBuffer = desiredY - buffer;
		double yMaxBuffer = desiredY + buffer;
		
		double leftAnswer = 0;
		double rightAnswer = 0;
		// Find out if going left or right is shorter
		if(currentAngle > desiredAngle) {
			leftAnswer = Math.abs(currentAngle - desiredAngle);
			rightAnswer = Math.abs(360 - (currentAngle - desiredAngle));
		} else if(currentAngle < desiredAngle) {
			leftAnswer = Math.abs((desiredAngle - 360) - currentAngle);
			rightAnswer = Math.abs(desiredAngle - currentAngle);
		}
		
		
		double diff = currentAngle - desiredAngle;
		boolean left = false;
		
		if(leftAnswer < rightAnswer) {
			// left is shorter
			left = true;
			diff = leftAnswer;
		} else if(rightAnswer < leftAnswer) {
			// right is shorter
			diff = rightAnswer;
		}
		
		double correctionPower = Math.abs(diff) * correction;
		if(correctionPower > 40) {
			correctionPower = 40;
		}
		
		if(currentAngle == desiredAngle) {
			// we are directly on the angle, go straight
			leftPower = forwardPower;
			rightPower = forwardPower;
			if(dbg>1) log.info(dname + " Go stright, angle: " + currentAngle + ", desiredAngle: " + desiredAngle);
		} else if(left) {
			//slightly to the right of straight path, so go left to correct
			leftPower = forwardPower - correctionPower;
			rightPower = forwardPower + correctionPower;
			if(dbg>1) log.info(dname + " Go left a little, angle: " + currentAngle + ", desiredAngle: " + desiredAngle);
			if(dbg>1) log.info(dname + " Correction: " + correctionPower + ", Left Power: " + leftPower + ", Right Power: " + rightPower);
		} else if(!left) {
			//slightly to the left of straight path, so go right to correct
			leftPower = forwardPower + correctionPower;
			rightPower = forwardPower - correctionPower;
			if(dbg>1) log.info(dname + " Go left a little, angle: " + currentAngle + ", desiredAngle: " + desiredAngle);
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
	
}
