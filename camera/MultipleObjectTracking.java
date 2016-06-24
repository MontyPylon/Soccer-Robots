package camera;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class MultipleObjectTracking implements Const {
	
	private static final Logger log = Logger.getLogger(MultipleObjectTracking.class);
    public static final int dbg = 0;
    public static final String dname = "MOBJT";
	private final boolean calibrationMode = new Boolean(PropertiesLoader.getValue("multipleObjectTracking.calibrationMode"));
	private CalibrationWindow calibrationWindow = new CalibrationWindow();
	private NormalWindow normalWindow;
	private JFrame frameCamera;
	private JFrame frameThreshold;
	private Panel panelCamera = new Panel();
	private Panel panelThreshold = new Panel();
	private Map clientMap = new HashMap();  // key is robotname, val = socketclient obj
	double minArea = 10000000L;
	double maxArea = 0;
	String previousCommand = null;
	boolean drawRobot = false;
	
	public static void main(String[] args) {
		MultipleObjectTracking tracker = new MultipleObjectTracking();		
		try {
			tracker.startTracking();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("",e);
		}
	}

	public void startTracking() throws Exception {
		if (dbg>1) log.info(dname+" MultipleObjectTracking start");
		
		// Load OpenCV libraries
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		// Create normal frame or calibration frame
		mountFrames();
		
		// Matrices for image processing.
		Mat image = new Mat();
		Mat thresholdedImage = new Mat();
		Mat hsvImage = new Mat();
		
		// Opens camera capture flow.
		VideoCapture capture = null;
		capture = openCameraFlow(capture);
		
		if (capture == null){
			throw new Exception("Could not connect to camera.");
		} else {
			// Set resolution height and width
			capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, IMAGE_RESOLUTION_WIDTH);
			capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, IMAGE_RESOLUTION_HEIGHT);
		}
		
		// Captures one image, for starting the process.
		try{
			capture.read(image);
		} catch (Exception e) {
			throw new Exception("Could not read from camera. Maybe the URL is not correct.");
		}
		
		// Set size of the frames
		setFramesSizes(image);
		initializeFrames();
		
		//normalWindow.enableConnectButtons();
		// setup colornames
		ColorName.setup();
		
		// setup robotnames
		RobotName.setup();
		
		normalWindow.enableConnectButtons();
		
		// setup robots
		List<Robot> robots = new ArrayList<Robot>();
		for (int i=0; i<RobotName.names.length; i++) {
			String robotName = RobotName.names[i];
			Robot robot = new Robot(robotName);
			robots.add(robot);
			/**
			SocketClient sc = new SocketClient();
			String ip = robot.getIp();
			String port = robot.getPort();
			Integer portNum = null;
			try { 
				portNum = Integer.parseInt(port); 
			} catch (Exception e) {
			
			}
			**/
			// Don't connect here for now
			/**
			if (ip != null && portNum != null) {
				sc.init(ip, portNum);           
				if (dbg>1) log.info(robotName+" ip="+ip+" port="+port);
				clientMap.put(robotName, sc);
			} else {
				if (dbg>1) log.info(dname+" No ip/port for "+robotName);
			}
			**/
		}
		
		int frameNum = 0;
		
		if (capture.isOpened()) {
			while (true) {
				capture.read(image);
				
				if (!image.empty()) {
					Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
		
					if (calibrationMode){
						thresholdedImage = processImage(hsvImage
								, new Scalar(calibrationWindow.getMinHValue(), calibrationWindow.getMinSValue(), calibrationWindow.getMinVValue())
								, new Scalar(calibrationWindow.getMaxHValue(), calibrationWindow.getMaxSValue(), calibrationWindow.getMaxVValue()));						
						List<TObject> tobjs = trackFilteredObject(null, thresholdedImage, image);
						if (dbg>1) log.info(dname+" # Found = "+tobjs.size());
						updateFrames(image, thresholdedImage);
					} else {
						
						frameNum++;
						
						// Create a list of colors from config file
						ArrayList<TObject> tobjs = new ArrayList<TObject>();
						for (int i=0; i<ColorName.names.length; i++) {
							String colorName = ColorName.names[i];
							if (ColorName.isValidName(colorName)) {
						        TObject tobj = new TObject(colorName);
						        tobjs.add(tobj);
							}
						}
						
						ArrayList<TObject> foundTobjs = new ArrayList<TObject>();
						for (TObject tobj : tobjs){
							thresholdedImage = processImage(hsvImage, tobj.getHsvMin(), tobj.getHsvMax());
							List<TObject> colTobjs = trackFilteredObject(tobj, thresholdedImage, image);
							foundTobjs.addAll(colTobjs);
							updateFrames(image, thresholdedImage);
						}

						int foundNum = 0;
						StringBuffer sb = new StringBuffer();
						for (TObject tobj : foundTobjs) {
							foundNum++;
							sb.append("#"+foundNum+"="+tobj+" ");
						}						
						if (dbg>3) log.info(dname+" ***** "+ sb.toString()+" ******");
						
						clientMap = normalWindow.getClientMap();
						handleTrackedObjects(foundTobjs, robots, image);
						
					}
					
				} else {
					throw new Exception("Could not read camera image.");
				}
				
				
			}
			
		} else {
			throw new Exception("Could not read from camera.");
		}
		
	}
	
	private void initializeFrames() {
		if(!calibrationMode) {
			//normalWindow.enableConnectButtons();
		}
	}

	private VideoCapture openCameraFlow(VideoCapture capture) {
		String imagesource = PropertiesLoader.getValue("multipleObjectTracking.imagesource");
		if (imagesource.equalsIgnoreCase("webcam")){
			capture = new VideoCapture(0);
		} else {
			if (imagesource.equalsIgnoreCase("ipcam")){
				String ipcamAddress = PropertiesLoader.getValue("multipleObjectTracking.imagesource.ipcam.address");
		    	capture = new VideoCapture(ipcamAddress);
			}
		}
		return capture;
	}

	private Mat processImage(Mat hsvImage, Scalar hsvMin, Scalar hsvMax){
		Mat thresholdedImage = new Mat();
		Core.inRange(hsvImage, hsvMin , hsvMax, thresholdedImage);
		morphOps(thresholdedImage);
		
		return thresholdedImage;
	}

	private void updateFrames(Mat image, Mat thresholdedImage) {
		setPanelsImages(image, thresholdedImage);
		repaintFrames();
	}

	private void mountFrames() {
		if (calibrationMode){
			createTrackingFrame();
			frameThreshold = createFrame("Threshold", panelThreshold);
			frameCamera = createFrame("Camera", panelCamera);
		} else {
			normalWindow = new NormalWindow();
			normalWindow.getPanel().setWindow(normalWindow);
		}
	}

	private void createTrackingFrame() {
		calibrationWindow.getFrame().setVisible(true);
	}
	
	private JFrame createFrame(String frameName, Panel panel){
	// private JFrame (String frameName, Panel panel){
		JFrame frame = new JFrame(frameName); 
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setBounds(0, 0, frame.getWidth(), frame.getHeight());		
		frame.setContentPane(panel);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		
		return frame;
	}

	private void setFramesSizes(Mat image) {
		if (calibrationMode){
			frameThreshold.setSize(IMAGE_WIDTH + 35, IMAGE_HEIGHT + 60);
			frameCamera.setSize(IMAGE_WIDTH + 35, IMAGE_HEIGHT + 60);
		}
	}

	private void repaintFrames() {
		if (calibrationMode){
			frameThreshold.repaint();
			frameCamera.repaint();
		} else {
			normalWindow.getFrame().repaint();
			//normalWindow.getPanel().repaint();
		}
		
	}

	private void setPanelsImages(Mat image, Mat thresholdedImage) {
		panelCamera.setImageWithMat(image);

		if (calibrationMode){
			panelThreshold.setImageWithMat(thresholdedImage);
		} else {
			//normalWindow.setPanel(panelCamera);
			normalWindow.getPanel().setImageWithMat(image);
		}
		
	}

	private void morphOps(Mat thresh) {	
		//create structuring element that will be used to "dilate" and "erode" image.
		//the element chosen here is a 3px by 3px rectangle
	
		
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		//dilate with larger element so make sure object is nicely visible
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
	
		Imgproc.erode(thresh, thresh, erodeElement);
		Imgproc.erode(thresh, thresh, erodeElement);
	
		Imgproc.dilate(thresh, thresh, dilateElement);
		Imgproc.dilate(thresh, thresh, dilateElement);
	}

	private List<TObject> trackFilteredObject(TObject theObj, Mat threshold, Mat image) {
		List<TObject> tobjs = new ArrayList<TObject>();
	
		Mat temp = new Mat();
		threshold.copyTo(temp);
		
		// The two variables below are the return of "findContours" processing.
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		
		// find contours of filtered image using openCV findContours function		
		Imgproc.findContours(temp, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// use moments method to find our filtered object
		boolean objectFound = false;
			
		if (contours.size() > 0) {
			int numObjects = contours.size();
	
			//if number of objects greater than MAX_NUM_OBJECTS we have a noisy filter
			if (numObjects < MAX_NUM_OBJECTS) {
	
				for (int i=0; i< contours.size(); i++){
					Moments moment = Imgproc.moments(contours.get(i));
					double area = moment.get_m00();
                    
					if (dbg>3) log.info(dname+" area of object = "+area);
					
					if (area > MIN_OBJECT_AREA && area < MAX_OBJECT_AREA) {
						TObject tobj = new TObject();
						tobj.setXPos((int)(moment.get_m10() / area));
						tobj.setYPos((int)(moment.get_m01() / area));
						tobj.setArea((int)area);
						
						if (theObj != null){
							tobj.setColourName(theObj.getColourName());
							tobj.setColour(theObj.getColour());
						}
						
				     	if (area > maxArea) {
				     		maxArea = area;
				     	}
				     	if (area < minArea) {
				     		minArea = area;
				     	}
				     	
				     	if(tobj.getXPos() < MAX_X_FILTER && tobj.getXPos() > MIN_X_FILTER && 
				     			tobj.getYPos() < MAX_Y_FILTER && tobj.getYPos() > MIN_Y_FILTER) {
				     		tobjs.add(tobj);
							objectFound = true;
				     	} else {
				     		tobj = null;
				     		objectFound = false;
				     	}
				     	
			     		// if (dbg>1) log.info(dname+" min area = "+minArea+", max area = "+maxArea);
				     	// if (dbg>1) log.info(dname+" inside range : area = "+area);
						
					} else {
						// if (dbg>1) log.info(dname+" outside range : area = "+area);
						objectFound = false;
					}
	
				}
	
				//let user know you found an object
				if (objectFound) {
					//draw object location on screen
					drawObject(tobjs, image);
				}
				
				if (theObj != null) {
				    // if (dbg>1) log.info(dname+" Colour = "+theObj.getType().name()+" # objs = "+tobjs.size());
				}
	
			} else {
				Core.putText(image, "TOO MUCH NOISE! ADJUST FILTER", new Point(0, 50), 1, 2, new Scalar(0, 0, 255), 2);
			}
	
		}
		return tobjs;
	
	}
	
	

	private void drawObject(List<TObject> theObjs, Mat image) {

		for (int i = 0; i < theObjs.size(); i++) {
			TObject theObj = theObjs.get(i);

			Core.circle(image, 
					new Point(theObj.getXPos(), 
							  theObj.getYPos()), 
					8, 
					new Scalar(0, 0, 255));
			/**
			Core.putText(image, 
					theObj.getXPos() + " , " + theObj.getYPos(), 
					new Point(theObj.getXPos(), 
							  theObj.getYPos() + 20), 
					1, 
					1, 
					new Scalar(0, 255, 0));
			Core.putText(image, 
					theObj.getColourName(), 
					new Point(theObj.getXPos(),
					          theObj.getYPos() - 30), 
					1, 
					2, 
					theObj.getColour());
					**/
		}
	}
	
	void handleTrackedObjects(List<TObject> tobjs, List<Robot> robots, Mat image) {
		try {
		    // for each robot, find a set of 4 tobjs that match the desired colors
			// and are within the required distances from each other
			
			// build hashmap by colourName
			
			Map tobjMap = new HashMap();
			for (TObject tobj : tobjs) {
				String colName = tobj.getColourName();
				List rows = (List)tobjMap.get(colName);
				if (rows == null) {
					rows = new ArrayList();
					tobjMap.put(colName, rows);
				}
				rows.add(tobj);
			}
			
			// test 1 - is there at least 1 instance of each of the 4 colors for each robot
		    List possibleRobots = new ArrayList();
		    
		    {
			
				int numRobot = 0;				
				for (Robot robot : robots) {
					numRobot++;
					
					boolean possible = true;
					for (int i=0; i<3; i++) {
						String col = robot.getColourName(i);
						int num = getNumWithColor(tobjMap,col);
						if (num == 0) {
							possible = false;
							break;
						}
					}
					
					if (possible) {
					    possibleRobots.add(robot);
					}
					
				}		
			
		    }
		    
		    if (dbg>3) log.info(dname+" # possible robots = "+possibleRobots.size());
			
			// test 2
			
		    List poss2Robots = new ArrayList();
		    {			
				for (int i=0; i<possibleRobots.size(); i++) {
					
					Robot robot = (Robot)possibleRobots.get(i);
					
					String fLeftCol = robot.getColourName(Robot.FRONTLEFT);
					List fLeftRows = (List)tobjMap.get(fLeftCol);
					String fRightCol = robot.getColourName(Robot.FRONTRIGHT);
					List fRightRows = (List)tobjMap.get(fRightCol);
					String bRightCol = robot.getColourName(Robot.BACKRIGHT);
					List bRightRows = (List)tobjMap.get(bRightCol);
					String bLeftCol = robot.getColourName(Robot.BACKLEFT);
					List bLeftRows = (List)tobjMap.get(bLeftCol);
					
					if (dbg>3) log.info(dname+" fl="+fLeftCol+" fr="+fRightCol+" br="+bRightCol+" bl="+bLeftCol);
					if (dbg>3) log.info(dname+" fl="+fLeftRows+" fr="+fRightRows+" br="+bRightRows+" bl="+bLeftRows);
					
					if (dbg>3) log.info(dname+" FRONT fl fr");
					List frontPairs = findPairs(fLeftRows, fRightRows, DIST_TYPE_ADJACENT, TObjectPair.FRONTLEFT_FRONTRIGHT, fLeftCol, fRightCol);
					if (dbg>3) log.info(dname+" DIAG fl br");
					List diagPairs = findPairs(fLeftRows, bRightRows, DIST_TYPE_OPPOSITE, TObjectPair.FRONTLEFT_BACKRIGHT, fLeftCol, bRightCol);
					if (dbg>3) log.info(dname+" VERT fl bl");
					List vertPairs = findPairs(fLeftRows, bLeftRows, DIST_TYPE_ADJACENT, TObjectPair.FRONTLEFT_BACKLEFT, fLeftCol, bLeftCol);	
					if (dbg>3) log.info(dname+" BACK bl br");
					List backPairs = findPairs(bLeftRows, bRightRows, DIST_TYPE_ADJACENT, TObjectPair.BACKLEFT_BACKRIGHT, bLeftCol, bRightCol);
					if (dbg>3) log.info(dname+" DIAG fr bl");
					List otherDiagPairs = findPairs(bLeftRows, fRightRows, DIST_TYPE_OPPOSITE, TObjectPair.FRONTRIGHT_BACKLEFT, bLeftCol, fRightCol);
					if (dbg>3) log.info(dname+" VERT fr br");
					List otherVertPairs = findPairs(bRightRows, fRightRows, DIST_TYPE_ADJACENT, TObjectPair.FRONTRIGHT_BACKRIGHT, bRightCol, fRightCol);
					
					if (dbg>3) log.info(dname+" fr="+frontPairs.size()+" di="+diagPairs.size()+" ve="+vertPairs.size()+" "+
					          "ba="+backPairs.size()+" od="+otherDiagPairs.size()+" ov="+otherVertPairs.size()+" ");
					
					// front to diag common
					List matchAFrontPairs = new ArrayList();
					List matchADiagPairs = new ArrayList();
                    findCommonPairsOneOne(frontPairs, diagPairs, matchAFrontPairs, matchADiagPairs);
                    
                    // front to vert common
					List matchBFrontPairs = new ArrayList();
					List matchBVertPairs = new ArrayList();
                    findCommonPairsOneOne(matchAFrontPairs, vertPairs, matchBFrontPairs, matchBVertPairs);
                    
                    // back to vert
					List matchCBackPairs = new ArrayList();
					List matchCVertPairs = new ArrayList();
                    findCommonPairsOneTwo(backPairs, matchBVertPairs, matchCBackPairs, matchCVertPairs);
                    
                    // back to other diag
					List matchDBackPairs = new ArrayList();
					List matchDOtherDiagPairs = new ArrayList();
					findCommonPairsOneOne(matchCBackPairs, otherDiagPairs, matchDBackPairs, matchDOtherDiagPairs);
					
                    // back to other vert
					List matchEBackPairs = new ArrayList();
					List matchEOtherVertPairs = new ArrayList();
					findCommonPairsTwoOne(matchDBackPairs, otherVertPairs, matchEBackPairs, matchEOtherVertPairs);
                    					
					if (matchEBackPairs.size() == 1 && matchEOtherVertPairs.size() == 1) {
						robot.pairs[TObjectPair.FRONTLEFT_FRONTRIGHT] = (TObjectPair)frontPairs.get(0);
						robot.pairs[TObjectPair.FRONTLEFT_BACKRIGHT] = (TObjectPair)diagPairs.get(0);
						robot.pairs[TObjectPair.FRONTLEFT_BACKLEFT] = (TObjectPair)vertPairs.get(0);
						poss2Robots.add(robot);
					} else {
                        if (dbg>1) log.info(dname+" !!!! TEST2 : not 1 match for robot : "+robot.name+" #matchesA = "+matchEBackPairs.size()+", #matchesB = "+matchEOtherVertPairs.size());
		
					}
				}
		    }
		    
		    if (dbg>3) log.info(dname+" # poss2 robots = "+poss2Robots.size());
		    {		
		    	
		    	if (poss2Robots.size() == 0) {
		    		if (dbg>1) log.info(dname+" !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! no robots found");
		    		if(previousCommand == null) {
		    			SocketClient sc = (SocketClient)clientMap.get("jeremy");
		    			if(sc != null) {
		    				sc.sendAction("+000+000x");
		    				previousCommand = "+000+000x";
		    			}
		    		} else if(!previousCommand.equals("+000+000x")) {
		    			SocketClient sc = (SocketClient)clientMap.get("jeremy");
		    			if(sc != null) {
		    				sc.sendAction("+000+000x");
		    				previousCommand = "+000+000x";
		    			}
		    		}
		    	}
		    	
				for (int i=0; i<poss2Robots.size(); i++) {
					
					Robot robot = (Robot)poss2Robots.get(i);				
					TObjectPair frontPair = robot.pairs[TObjectPair.FRONTLEFT_FRONTRIGHT];
					
					TObject fLeft = frontPair.t1;
					robot.setFL(fLeft);
					TObject fRight = frontPair.t2;
					robot.setFR(fRight);
					TObjectPair diagPair = robot.pairs[TObjectPair.FRONTLEFT_BACKRIGHT];
					TObject bRight = diagPair.t2;
					robot.setBL(bRight);
					TObjectPair vertPair = robot.pairs[TObjectPair.FRONTLEFT_BACKLEFT];
					TObject bLeft = vertPair.t2;
					robot.setBR(bLeft);
					
					if (dbg>3) log.info(dname+" ................. Robot "+robot.name+" : FrontLeft = "+fLeft+ ", FrontRight = "+fRight+" , BackRight = "+bRight+", BackLeft = "+bLeft);
					
					int minX = minValue(fLeft.getXPos(), fRight.getXPos(), bRight.getXPos(), bLeft.getXPos());
					int maxX = maxValue(fLeft.getXPos(), fRight.getXPos(), bRight.getXPos(), bLeft.getXPos());
					
					int minY = minValue(fLeft.getYPos(), fRight.getYPos(), bRight.getYPos(), bLeft.getYPos());
					int maxY = maxValue(fLeft.getYPos(), fRight.getYPos(), bRight.getYPos(), bLeft.getYPos());
					
					int diffX = maxX - minX;
					int diffY = maxY - minY;
					
					int centerX = minX + (diffX / 2);
					int centerY = minY + (diffY / 2);
					Point centerPoint = new Point(centerX, centerY);
					
					//Point fLeftPoint = new Point(fLeft.getXPos(), fLeft.getYPos());
					//Point fRightPoint = new Point(fRight.getXPos(), fRight.getYPos());
					Point bLeftPoint = new Point(bLeft.getXPos(), bLeft.getYPos());
					Point bRightPoint = new Point(bRight.getXPos(), bRight.getYPos());

					double angle = calcRotationAngleInDegrees(bLeftPoint, bRightPoint);
					String angleStr = Util.printDouble(angle);
					
					//FOUND ROBOT
					if (dbg>1) log.info(dname+" "+robot.name+" C="+centerX+":"+centerY+", A="+angleStr);
					// log.info(dname+" Robot="+robot.name+" : F="+fLeft+","+fRight+",B="+bRight+","+bLeft+" C="+centerX+":"+centerY+", A="+angleStr);
					
					//send parameters to Normal windows to draw robot on next frame
					//normalWindow.giveRobotPosAndAngle(centerX, centerY, angle, robot);
					
					SocketClient sc = (SocketClient)clientMap.get(robot.name);
					
					// Add the robots history
					robot.addHistory(centerPoint, angle);
					
					if(sc != null) {
						
						//TaskAvoidEdgeJeremy task = new TaskAvoidEdgeJeremy();
						//TaskStraightLineAvoidEdge task = new TaskStraightLineAvoidEdge();
						//TaskGoToCenter task = new TaskGoToCenter();
						//String command = task.perform(robot, image, 0);
						//String realCommand = task.perform(robot, image, 1);
						//robot.addHistory(centerPoint, angle, realCommand);
						
						String command = null;
						
				    	Iterator iter = normalWindow.buttonMetaMap.keySet().iterator();  
				    	while (iter.hasNext()) {
				    		String act = (String)iter.next();	
				    		ButtonMeta bm = (ButtonMeta)normalWindow.buttonMetaMap.get(act);
                            if (bm.pressed) {
                            	ITask task = bm.task;
                            	command = task.perform(robot, image);
                            }
				    	}
						
						
						/**
						if(normalWindow.stopProcess) {
							command = "+000+000x";
							if (dbg>1) log.info(dname+" Stop button is on...");
						}
						
						if(normalWindow.justEndedTurnTest) {
							if(!normalWindow.turnTestPrint) {
								double timeLog0 = robot.getEpochMs(0);
								double timeLog1 = robot.getEpochMs(3);
								double timeDiff = timeLog1 - timeLog0;
								double timePrevLog = robot.getEpochMs(4);
								double timePrevDiff = timeLog1 - timePrevLog;
	
								double angleLog0 = robot.getAngle(0);
								double angleLog1 = robot.getAngle(3);
								double anglePrevLog = robot.getAngle(4);
								double angleDiff = angleLog1 - angleLog0;
								double anglePreviousDiff = angleLog1 - anglePrevLog;
								
								double angleSpeedPrevLog = anglePreviousDiff / timePrevDiff;
								double angleSpeedLog = angleDiff / timeDiff;
								double angleSpeedDiff = angleSpeedPrevLog - angleSpeedLog;
								double angleAccel = angleSpeedDiff / timeDiff;
								//if (dbg==0) log.info(dname+" CODE: 1996- angleLog0/currentAngle = " + angleLog0 + ", angleLog1 = " + angleLog1 + ", timePrevDiff = " + timePrevDiff + ", timePrevLog = " + timePrevLog);
								// + ", angular accel = " + angleAccel
								if (dbg==0) log.info(dname+" CODE: 1997- time delay = " + robot.timeSinceLastTurn + ", Ang Speed = " + angleSpeedPrevLog + ", delta angle = " + angleDiff);
								double predictedLoc = angleSpeedPrevLog * 150;
								if (dbg==0) log.info(dname+" CODE: 1998- predictedLoc = " + predictedLoc + ", difference: " + (Math.abs(angleDiff) - predictedLoc));
								normalWindow.justEndedTurnTest = false;
							} else {
								normalWindow.turnTestPrint = false;
							}
						}
						
						if(normalWindow.turnTestBool) {		//STATIONARY TURN TESTING
							if(normalWindow.firstPressTestTurn) {
								robot.firstTestTurn = true;
								robot.taskTestTurn = true;
								normalWindow.firstPressTestTurn = false;
								normalWindow.stopProcess = false;
								if (dbg>1) log.info(dname+" STARTING A NEW TURN ------- STARTING A NEW TURN");
							}
							
							TaskTurnTest task = new TaskTurnTest();
							command = task.perform(robot, image, 600, "right");
							//robot.addHistory(centerPoint, angle, command);
							
							if(!robot.taskTestTurn) {
								command = "+000+000x";
								sc.sendAction(command);
								normalWindow.turnTestBool = false;
								//stopProcess = true;
								normalWindow.justEndedTurnTest = true;
								normalWindow.turnTestPrint = true;
							}
						} else if(normalWindow.turnToCenterBool) {		//TURN TOWARDS THE CENTER
							if(normalWindow.firstPressTurnCenter) {
								robot.taskTurnCenter = true;
								normalWindow.firstPressTurnCenter = false;
								normalWindow.stopProcess = false;
								if (dbg>1) log.info(dname+" START TO GO TO CENTER ------- START TO GO TO CENTER");
							}
							
							TaskTurnToCenter task = new TaskTurnToCenter();
							command = task.perform(robot, image);
							//robot.addHistory(centerPoint, angle, command);
							
							if(!robot.taskTurnCenter) {
								//command = "+000+000x";
								//sc.sendAction(command);
								normalWindow.turnToCenterBool = false;
								normalWindow.goToCenterBool = true;
								normalWindow.firstPressGoToCenter = true;
								//stopProcess = true;
							}
						} else if(normalWindow.goToCenterBool) {		//GO TOWARDS THE CENTER
							if(normalWindow.firstPressGoToCenter) {
								robot.taskGoToCenter = true;
								normalWindow.firstPressGoToCenter = false;
								normalWindow.stopProcess = false;
								if (dbg>1) log.info(dname+" START TO GO TO CENTER ------- START TO GO TO CENTER");
							}
							
							TaskGoToCenter task = new TaskGoToCenter();
							command = task.perform(robot, image);
							//robot.addHistory(centerPoint, angle, command);
							
							if(!robot.taskGoToCenter) {
								command = "+000+000x";
								sc.sendAction(command);
								normalWindow.goToCenterBool = false;
								//stopProcess = true;
							}
						}
						**/
						
						if(command != null) {
							if((previousCommand != null && !previousCommand.equals(command)) || previousCommand == null) {
								if(command.equals("+000+000x")) {
									sc.sendAction(command);
									if (dbg>1) log.info(dname+" STOP ---------------------------------------------------------------------------------------------------------------------- STOP");
								} else if(!command.equals("error") && !command.equals("same")) {
									sc.sendAction(command);
									if (dbg>1) log.info(dname+" command to send: "+command);
									if (dbg>1) log.info(dname+" New ------------------------------------------- New");
								}
							} else{
								if (dbg>1) log.info(dname+" OLD command ------------------------------------------- OLD command");
							}
							
						}
						
						previousCommand = command;
					}
				}
				
			    normalWindow.sendRobots(poss2Robots);
		    }
			
		} catch (Exception e) {
			log.error("",e);
		}
	}
	
	void sleep(long a) throws Exception {
	    Thread.sleep(a);
	}
	
	int minValue(int a, int b, int c, int d) {
		int m = a;
		if (b < m) m = b;
		if (c < m) m = c;
		if (d < m) m = d;
		return m;
	}
	
	int maxValue(int a, int b, int c, int d) {
		int m = a;
		if (b > m) m = b;
		if (c > m) m = c;
		if (d > m) m = d;
		return m;
	}
	
	int getNumWithColor(Map tobjMap, String colName) {
		if (tobjMap == null) return 0;
		if (colName == null) return 0;
		List rows = (List)tobjMap.get(colName);
		if (rows == null) return 0;
		return rows.size();
	}
	
    int distanceBetween(TObject a, TObject b) {
    	int dx = a.getXPos() - b.getXPos();
    	int dy = a.getYPos() - b.getYPos();
    	int squareDist = (dx * dx) + (dy * dy);
    	int dist = (int)Math.sqrt((double)squareDist);
    	return dist;
    }
    
    boolean inRange(int a, int loVal, int hiVal) {
    	if (a >= loVal && a <= hiVal) return true;
    	return false;
    }
    
    List findPairs(List rows1, List rows2, int distType, int pairType, String col1, String col2) {
    	if (distType == DIST_TYPE_ADJACENT) {
    		return findAdjacentPairs(rows1, rows2, pairType, col1, col2);
    	} else if (distType == DIST_TYPE_OPPOSITE) {
    		return findOppositePairs(rows1, rows2, pairType, col1, col2);
    	}
    	return null;
    }
    
    List findAdjacentPairs(List rows1, List rows2, int pairType, String col1, String col2) {
        return findPairs(rows1, rows2, pairType, col1, col2, MIN_ADJACENT_DIST, MAX_ADJACENT_DIST);
    }
    
    List findOppositePairs(List rows1, List rows2, int pairType, String col1, String col2) {
    	return findPairs(rows1, rows2, pairType, col1, col2, MIN_OPPOSITE_DIST, MAX_OPPOSITE_DIST);
    }
    
    List findPairs(List rows1, List rows2, int pairType, String col1, String col2, int loVal, int hiVal) {
		List pairs = new ArrayList();
    	if (rows1 == null) return pairs;
    	if (rows2 == null) return pairs;
    	if (col1 == null) return pairs;
    	if (col2 == null) return pairs;
		for (int j=0; j<rows1.size(); j++) {
			TObject tobj1 = (TObject)rows1.get(j);
			String tcol1 = tobj1.getColourName();
			if (tcol1.equals(col1) || tcol1.equals(col2)) {
				for (int k=0; k<rows2.size(); k++) {
					TObject tobj2 = (TObject)rows2.get(k);
					String tcol2 = tobj2.getColourName();
					//if (!tcol2.equals(tcol1)) {
						if (tcol2.equals(col1) || tcol2.equals(col2)) {
							int dist = distanceBetween(tobj1, tobj2);
							if (inRange(dist, loVal, hiVal)) {
								pairs.add(new TObjectPair(tobj1, tobj2, pairType));
								if (dbg>3) log.info(dname+" Dist t1="+tobj1+" t2="+tobj2+" dist="+dist+", inrange lo="+loVal+" hi="+hiVal);
							} else {
								if (dbg>3) log.info(dname+" Dist t1="+tobj1+" t2="+tobj2+" dist="+dist+", outrange lo="+loVal+" hi="+hiVal+" ---------------------------------------------");
							}
						}
					//}
				}
			}
		}
		return pairs; 
    }
    
    void findCommonPairsOneOne(List rows1, List rows2, List outRows1, List outRows2) {
		for (int j=0; j<rows1.size(); j++) {
			TObjectPair p1 = (TObjectPair)rows1.get(j);
			for (int k=0; k<rows2.size(); k++) {
				TObjectPair p2 = (TObjectPair)rows2.get(k);
				if (p1.t1.getId() == p2.t1.getId()) {
					outRows1.add(p1);
					outRows2.add(p2);
				}
			}
		}
    }
    
    void findCommonPairsTwoTwo(List rows1, List rows2, List outRows1, List outRows2) {
		for (int j=0; j<rows1.size(); j++) {
			TObjectPair p1 = (TObjectPair)rows1.get(j);
			for (int k=0; k<rows2.size(); k++) {
				TObjectPair p2 = (TObjectPair)rows2.get(k);
				if (p1.t2.getId() == p2.t2.getId()) {
					outRows1.add(p1);
					outRows2.add(p2);
				}
			}
		}
    }
    
    void findCommonPairsOneTwo(List rows1, List rows2, List outRows1, List outRows2) {
		for (int j=0; j<rows1.size(); j++) {
			TObjectPair p1 = (TObjectPair)rows1.get(j);
			for (int k=0; k<rows2.size(); k++) {
				TObjectPair p2 = (TObjectPair)rows2.get(k);
				if (p1.t1.getId() == p2.t2.getId()) {
					outRows1.add(p1);
					outRows2.add(p2);
				}
			}
		}
    }
    
    void findCommonPairsTwoOne(List rows1, List rows2, List outRows1, List outRows2) {
 		for (int j=0; j<rows1.size(); j++) {
 			TObjectPair p1 = (TObjectPair)rows1.get(j);
 			for (int k=0; k<rows2.size(); k++) {
 				TObjectPair p2 = (TObjectPair)rows2.get(k);
 				if (p1.t2.getId() == p2.t1.getId()) {
 					outRows1.add(p1);
 					outRows2.add(p2);
 				}
 			}
 		}
     }
    
    public double calcRotationAngleInDegrees(Point centerPt, Point targetPt)
    {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.  
        // NOTE: By preserving Y and X param order to atan2,  we are expecting 
        // a CLOCKWISE angle direction.  
        double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);

        // rotate the theta angle clockwise by 90 degrees 
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.  
        // subtracting would rotate it counter-clockwise
        theta += Math.PI/2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

}