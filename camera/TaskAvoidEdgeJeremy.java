package camera;

import org.apache.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Point;

public class TaskAvoidEdgeJeremy implements Const {
	
	Logger log = Logger.getLogger(TaskAvoidEdgeJeremy.class);
    public static final int dbg = 4;
    public static final String dname = "TAAEJ";
    
    public static final String AAAAA = " !!!!!!!! ";
	
    // ------------ fixed -------------------
	final int buffer = 30;
	final int xMin = 0; // + buffer;
	final int xMax = 0; // - buffer;
	final int yMin = 0; // + buffer;
	final int yMax = 0; // - buffer;
	
	// ------------ state ----------------
	//boolean isForward;
	
	//Robot robot;
	//Mat image;
	
	public void setRobot(Robot robot1) {
		//this.robot = robot1;
	}
	
	public void setImage(Mat image1) {
		//this.image = image1;
	}
	
 	public String perform(Robot robot, Mat image) {
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

			if (centerPoint1 == null) {
				// its the first frame
				if (x0 < xMin) {
					if (dbg>1) log.info(dname+AAAAA+"START x0 ("+x0+") less than xmin ("+xMin+") - NEED RESET");
				} else if (x0 > xMax) {
					if (dbg>1) log.info(dname+AAAAA+"START x0 ("+x0+") more than xmin ("+xMin+") - NEED RESET");
				} else if (y0 < yMin) {
					if (dbg>1) log.info(dname+AAAAA+"START y0 ("+y0+") less than ymin ("+yMin+") - NEED RESET");
				} else if (y0 > yMax) {
					if (dbg>1) log.info(dname+AAAAA+"START y0 ("+y0+") more than ymin ("+yMin+") - NEED RESET");
				} else {
					if (dbg>1) log.info(dname+AAAAA+"START, inField, Forward");
					robot.isForward = true;
					robot.switching = false;
					return forward();
					//return "forward";
				}
			}
			
			if(!robot.switching) {
				if (x0 < xMin) {
					if (dbg>1) log.info(dname+AAAAA+" ("+x0+") less than xMIN ("+xMin+")");
					switchDirection(robot);
				} else if (x0 > xMax) {
					if (dbg>1) log.info(dname+AAAAA+" ("+x0+") greater than xMAX ("+xMax+")");
					switchDirection(robot);
				} else if (y0 < yMin) {
					if (dbg>1) log.info(dname+AAAAA+" ("+y0+") less than yMIN ("+yMin+")");
					switchDirection(robot);
				} else if (y0 > yMax) {
					if (dbg>1) log.info(dname+AAAAA+" ("+y0+") less than yMAX ("+yMax+")");
					switchDirection(robot);
				}
			} else if(robot.switching) {
				if(x0 > (xMin + buffer) && x0 < (xMax - buffer) && y0 > (yMin + buffer) && y0 < (yMax - buffer)) {
					robot.switching = false;
				}
			}
			
			if(robot.isForward) {
				if (dbg>1) log.info(dname+AAAAA+"Going FORWARD");
				return forward();
			} else {
				if (dbg>1) log.info(dname+AAAAA+"Going BACKWARD");
				return backward();
			}
			
			
		} catch (Exception e) {
			log.error("",e);
		}
		return "error";
	}
 	
 	public String forward() {
		return "ax";
 	}
 	
 	public String backward() {
		return "hx";
 	}
 	
 	public void switchDirection(Robot robot) {
 		robot.switching = true;
 		
 		if(robot.isForward) {
 			robot.isForward = false;
		} else {
			robot.isForward = true;
		}
 	}
	
}
