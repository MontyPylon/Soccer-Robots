package camera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;
import org.opencv.core.Point;

public class NormalWindow implements ActionListener, Const, MouseListener {
	
	private static final Logger log = Logger.getLogger(NormalWindow.class);
    public static final int dbg = 0;
    public static final String dname = "NORMW";
	
	private JFrame frame;
	private Panel panel = new Panel();
	
	JButton stop;
	boolean stopProcess = false;
	
	JButton resume;
	
	JButton turnTest;
	boolean turnTestBool = false;
	boolean firstPressTestTurn = false;
	boolean justEndedTurnTest = false;
	boolean turnTestPrint = true;
	
	JButton turnToCenter;
	boolean turnToCenterBool = false;
	boolean firstPressTurnCenter = false;

	JButton goToCenter;
	boolean goToCenterBool = false;
	boolean firstPressGoToCenter = false;
	
	JButton connect1;
	JButton connect2;
	JButton connect3;
	JButton connect4;
	JButton connect5;
	JButton connect6;
	Color connectGreen = new Color(51, 214, 89);
	
	boolean connectState1 = true;
	boolean connectState2 = true;
	boolean connectState3 = true;
	boolean connectState4 = true;
	boolean connectState5 = true;
	boolean connectState6 = true;
	
	JButton disconnect1;
	JButton disconnect2;
	JButton disconnect3;
	JButton disconnect4;
	JButton disconnect5;
	JButton disconnect6;
	Color disconnectRed = new Color(227, 61, 78);
	
	ArrayList<JButton> conB = new ArrayList<JButton>();
	ArrayList<JButton> disB = new ArrayList<JButton>();
	
	List robotsToPaint = new ArrayList();
	Color background = new Color(88, 88, 88);
	Color batteryBackground = new Color(213, 220, 222);
	
	Color circleGreen = new Color(87, 199, 84);
	Color circleRed = new Color(227, 75, 75);
	Color circlePurple = new Color(196, 150, 242);
	
	int spaceBetweenObjects = 20;
	
	private Map clientMapNormalWindow = new HashMap(); 

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NormalWindow window = new NormalWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NormalWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("Normal");
		intializeButtons();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.panel.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		frame.getContentPane().add(this.panel);
		frame.pack();
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.getContentPane().addMouseListener(this);
	}
	
	public void intializeButtons() {
		int numberOfButtons = 5;
		int buttonHeight = 40;
		int i = 0;
		int buttonWidth = (IMAGE_WIDTH - (spaceBetweenObjects * (numberOfButtons - 1))) / (numberOfButtons);
		int startX = IMAGE_WIDTH + (IMAGE_BUFFER_WIDTH * 2);
		int startY = (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
		int add = spaceBetweenObjects + buttonWidth;
		
		stop = new JButton("Stop");
		stop.setBounds(startX, startY, buttonWidth, buttonHeight);
		if (dbg>1) log.info(dname+" Set stop button at pos 680");
		stop.setActionCommand("disable");
		stop.addActionListener(this);
		frame.getContentPane().add(stop);
		i++;
		
		resume = new JButton("Resume");
		resume.setBounds(startX + (add * i), startY, buttonWidth, buttonHeight);
		resume.setActionCommand("enable");
		resume.addActionListener(this);
		frame.getContentPane().add(resume);
		i++;
		
		turnTest = new JButton("Turn Test");
		turnTest.setBounds(startX + (add * i), startY, buttonWidth, buttonHeight);
		turnTest.setActionCommand("turnTest");
		turnTest.addActionListener(this);
		frame.getContentPane().add(turnTest);
		i++;
		
		turnToCenter = new JButton("Turn to Center");
		turnToCenter.setBounds(startX + (add * i), startY, buttonWidth, buttonHeight);
		turnToCenter.setActionCommand("turnCenter");
		turnToCenter.addActionListener(this);
		frame.getContentPane().add(turnToCenter);
		i++;
		
		goToCenter = new JButton("Go to Center");
		goToCenter.setBounds(startX + (add * i), startY, buttonWidth, buttonHeight);
		goToCenter.setActionCommand("goCenter");
		goToCenter.addActionListener(this);
		frame.getContentPane().add(goToCenter);
		i++;
		
		connectDisconnectButtons();
	}
	
	private void connectDisconnectButtons() {
		double widthPanel = FRAME_WIDTH - IMAGE_WIDTH - (IMAGE_BUFFER_WIDTH * 3);
		double heightPanel = IMAGE_HEIGHT;
		double startX = IMAGE_WIDTH + (IMAGE_BUFFER_WIDTH * 2);
		double startY = IMAGE_BUFFER_HEIGHT;
		
		// Robot table properties
		double xBuff = 20;
		double yBuff = 20;
		double tableWidth = (widthPanel - (xBuff * 2)) / 3;
		double tableHeight = (heightPanel - yBuff) / 2;
		double xPos = startX;
		double yPos = startY;
		double rounded = 10;
		
		// Button properties
		double buttonRatioHeight = .2;
		double buttonStartY = startY + tableHeight - (tableHeight * buttonRatioHeight);
		double buttonWidth = tableWidth / 2;
		double buttonHeight = tableHeight * buttonRatioHeight;
		
		connect1 = new JButton("Connect");
		connect1.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect1.setActionCommand("connect1");
		connect1.addActionListener(this);
		connect1.setBackground(Color.GRAY);
		connect1.setForeground(Color.BLACK);
		connect1.setEnabled(false);
		frame.getContentPane().add(connect1);
		
		disconnect1 = new JButton("Disconnect");
		disconnect1.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect1.setActionCommand("disconnect1");
		disconnect1.addActionListener(this);
		disconnect1.setBackground(Color.GRAY);
		disconnect1.setForeground(Color.WHITE);
		disconnect1.setEnabled(false);
		frame.getContentPane().add(disconnect1);
		
		xPos += (tableWidth + xBuff);
		
		connect2 = new JButton("Connect");
		connect2.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect2.setActionCommand("connect2");
		connect2.addActionListener(this);
		connect2.setBackground(Color.GRAY);
		connect2.setForeground(Color.BLACK);
		connect2.setEnabled(false);
		frame.getContentPane().add(connect2);
		
		disconnect2 = new JButton("Disconnect");
		disconnect2.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect2.setActionCommand("disconnect2");
		disconnect2.addActionListener(this);
		disconnect2.setBackground(Color.GRAY);
		disconnect2.setForeground(Color.WHITE);
		disconnect2.setEnabled(false);
		frame.getContentPane().add(disconnect2);
		
		xPos += (tableWidth + xBuff);
		
		connect3 = new JButton("Connect");
		connect3.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect3.setActionCommand("connect3");
		connect3.addActionListener(this);
		connect3.setBackground(Color.GRAY);
		connect3.setForeground(Color.BLACK);
		connect3.setEnabled(false);
		frame.getContentPane().add(connect3);
		
		disconnect3 = new JButton("Disconnect");
		disconnect3.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect3.setActionCommand("disconnect3");
		disconnect3.addActionListener(this);
		disconnect3.setBackground(Color.GRAY);
		disconnect3.setForeground(Color.WHITE);
		disconnect3.setEnabled(false);
		frame.getContentPane().add(disconnect3);
		
		buttonStartY += (tableHeight + yBuff);
		xPos = startX;
		
		connect4 = new JButton("Connect");
		connect4.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect4.setActionCommand("connect4");
		connect4.addActionListener(this);
		connect4.setBackground(Color.GRAY);
		connect4.setForeground(Color.BLACK);
		connect4.setEnabled(false);
		frame.getContentPane().add(connect4);
		
		disconnect4 = new JButton("Disconnect");
		disconnect4.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect4.setActionCommand("disconnect4");
		disconnect4.addActionListener(this);
		disconnect4.setBackground(Color.GRAY);
		disconnect4.setForeground(Color.WHITE);
		disconnect4.setEnabled(false);
		frame.getContentPane().add(disconnect4);
		
		xPos += (tableWidth + xBuff);
		
		connect5 = new JButton("Connect");
		connect5.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect5.setActionCommand("connect5");
		connect5.addActionListener(this);
		connect5.setBackground(Color.GRAY);
		connect5.setForeground(Color.BLACK);
		connect5.setEnabled(false);
		frame.getContentPane().add(connect5);
		
		disconnect5 = new JButton("Disconnect");
		disconnect5.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect5.setActionCommand("disconnect5");
		disconnect5.addActionListener(this);
		disconnect5.setBackground(Color.GRAY);
		disconnect5.setForeground(Color.WHITE);
		disconnect5.setEnabled(false);
		frame.getContentPane().add(disconnect5);
		
		xPos += (tableWidth + xBuff);
		
		connect6 = new JButton("Connect");
		connect6.setBounds((int) Math.round(xPos), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		connect6.setActionCommand("connect6");
		connect6.addActionListener(this);
		connect6.setBackground(Color.GRAY);
		connect6.setForeground(Color.BLACK);
		connect6.setEnabled(false);
		frame.getContentPane().add(connect6);
		
		disconnect6 = new JButton("Disconnect");
		disconnect6.setBounds((int) (Math.round(xPos) + buttonWidth), (int) Math.round(buttonStartY), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
		disconnect6.setActionCommand("disconnect6");
		disconnect6.addActionListener(this);
		disconnect6.setBackground(Color.GRAY);
		disconnect6.setForeground(Color.WHITE);
		disconnect6.setEnabled(false);
		frame.getContentPane().add(disconnect6);
		
		conB.add(connect1);
		conB.add(connect2);
		conB.add(connect3);
		conB.add(connect4);
		conB.add(connect5);
		conB.add(connect6);
		disB.add(disconnect1);
		disB.add(disconnect2);
		disB.add(disconnect3);
		disB.add(disconnect4);
		disB.add(disconnect5);
		disB.add(disconnect6);
		
	}
	
	public void enableConnectButtons() {
		for (int i=0; i<RobotName.names.length; i++) {
			conB.get(i).setEnabled(true);
			conB.get(i).setBackground(connectGreen);
		}
	}
	
	public Map getClientMap() {
		return clientMapNormalWindow;
	}

	public void actionPerformed(ActionEvent e) {
	    if("disable".equals(e.getActionCommand())) {
	        //Stop all processes
	    	if (dbg>1) log.info(dname+" STOP BUTTON ALL PROCESSES PRESSED");
	    	stopProcess = true;
	    	//set other processes to false;
	    	turnToCenterBool = false;
	    	turnTestBool = false;
	    	goToCenterBool = false;
	    } else if("enable".equals(e.getActionCommand())) {
	    	if (dbg>1) log.info(dname+" RESUME BUTTON ALL PROCESSES PRESSED");
	    	stopProcess = false;
	    } else if("turnTest".equals(e.getActionCommand())) {
	    	if(!turnTestBool) {
	    		firstPressTestTurn = true;
	    	}
	    	turnTestBool = true;
	    } else if("turnCenter".equals(e.getActionCommand())) {
	    	if(!turnToCenterBool) {
	    		firstPressTurnCenter = true;
	    	}
	    	turnToCenterBool = true;
	    } else if("goCenter".equals(e.getActionCommand())) {
	    	if(!goToCenterBool) {
	    		firstPressGoToCenter = true;
	    	}
	    	goToCenterBool = true;
	    }
	    
	    if("connect1".equals(e.getActionCommand())) {
	    	System.out.println("connect1");
	    	
	    	// Connect to corresponding robot
	    	connectToRobot(1);
	    	
	    	connect1.setBackground(Color.GRAY);
	    	connect1.setEnabled(false);
	    	disconnect1.setEnabled(true);
	    	disconnect1.setBackground(disconnectRed);
	    	connectState1 = false;
	    } else if("disconnect1".equals(e.getActionCommand())) {
	    	System.out.println("disconnect1");
	    	
	    	// Disconnect to corresponding robot
	    	disconnectFromRobot(1);
	    	
	    	disconnect1.setBackground(Color.GRAY);
	    	disconnect1.setEnabled(false);
			connect1.setEnabled(true);
	    	connect1.setBackground(connectGreen);
	    	connectState1 = true;
	    }
	    
	}
	
	public void connectToRobot(int robotID) {
		// Connect to only robot with correct robotID
		List<Robot> robots = new ArrayList<Robot>();
		for (int i=0; i<RobotName.names.length; i++) {
			String robotName = RobotName.names[i];
			Robot robot = new Robot(robotName);
			robots.add(robot);
			
			SocketClient sc = new SocketClient();
			
			String ip = robot.getIp();
			String port = robot.getPort();
			Integer portNum = null;
			try { 
				portNum = Integer.parseInt(port); 
			} catch (Exception e) {
				
			}
			
			if (ip != null && portNum != null && i == (robotID - 1)) {
				if (dbg>1) log.info(robotName+" attempting to connect...");
				sc.init(ip, portNum);
				if (dbg>1) log.info(robotName+" ip="+ip+" port="+port);
				clientMapNormalWindow.put(robotName, sc);
			} else {
				if (dbg>1) log.info(dname+" No ip/port for "+robotName);
			}
			
		}
	}
	
	public void disconnectFromRobot(int robotID) {
		// TODO disconnect from only one robot
		/**
		// Connect to only robot with correct robotID
		List<Robot> robots = new ArrayList<Robot>();
		for (int i=0; i<RobotName.names.length; i++) {
			String robotName = RobotName.names[i];
			Robot robot = new Robot(robotName);
			robots.add(robot);
			
			SocketClient sc = new SocketClient();
			
			String ip = robot.getIp();
			String port = robot.getPort();
			Integer portNum = null;
			try { 
				portNum = Integer.parseInt(port); 
			} catch (Exception e) {
				
			}
			
			if (ip != null && portNum != null && i == (robotID - 1)) {
				//sc.init(ip, portNum);
				sc.close();
				//if (dbg>1) log.info(robotName+" ip="+ip+" port="+port);
				//clientMapNormalWindow.put(robotName, sc);
				//clientMapNormalWindow.remove(robotName);
			} else {
				if (dbg>1) log.info(dname+" No ip/port for "+robotName);
			}
			
		}
		**/
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Panel getPanel() {
		return panel;
	}

	public void sendRobots(List<Robot> poss2Robots) {
		robotsToPaint = poss2Robots;
	}
	
	private double scaleWidthOfCameraToField(double xPos) {
		double widthCamField = MAX_X_FILTER - MIN_X_FILTER;
		double actualXPos = xPos - MIN_X_FILTER;
		double cam = actualXPos / widthCamField;
		double paint = cam * PITCH_WIDTH;
		return paint;
	}
	
	private double scaleHeightOfCameraToField(double yPos) {
		double heightCamField = MAX_Y_FILTER - MIN_Y_FILTER;
		double actualYPos = yPos - MIN_Y_FILTER;
		double cam = actualYPos / heightCamField;
		double paint = cam * PITCH_HEIGHT;
		return paint;
	}

	public void drawComponents(Graphics g) {
		drawField(g);
		drawRedSquare(g);
		if(robotsToPaint.size() > 0) {
			for (int i=0; i<robotsToPaint.size(); i++) {
				Robot robot = (Robot)robotsToPaint.get(i);	
				drawRobots(g, robot);
			}
		}
		drawRobotTables(g);
	}
	
	private void drawRobotTables(Graphics g) {
		
		double widthPanel = FRAME_WIDTH - IMAGE_WIDTH - (IMAGE_BUFFER_WIDTH * 3);
		double heightPanel = IMAGE_HEIGHT;
		double startX = IMAGE_WIDTH + (IMAGE_BUFFER_WIDTH * 2);
		double startY = IMAGE_BUFFER_HEIGHT;
		
		// Robot table properties
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double xBuff = 20;
		double yBuff = 20;
		double tableWidth = (widthPanel - (xBuff * 2)) / 3;
		double tableHeight = (heightPanel - yBuff) / 2;
		double xPos = startX;
		double yPos = startY;
		double rounded = 10;
		
		// Button properties
		double buttonRatioHeight = .2;
		double buttonStartY = startY + tableHeight - (tableHeight * buttonRatioHeight);
		double buttonWidth = tableWidth / 2;
		double buttonHeight = tableHeight * buttonRatioHeight;
		
		// Robot shell properties
		double shellWidth = tableWidth * .35;
		double shellHeight = shellWidth * (11.4 / 11.4); // TODO ROBOT RATIO CONFIG FILE
		double start = tableWidth * .04;
		double roundShell = 10;
		
		// Name text properties
		Font font = new Font("Ariel", Font.ITALIC, 14);
	    g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		double fontHeight = fm.getHeight();
		double textYBuffer = 15;
		double extraRoom = shellHeight - ((fontHeight * 1.5) + (textYBuffer * 2));
		double spaceTop = extraRoom / 2;
		
		String nameText = "Name: ";
		double nameX = shellWidth + (start * 2);
		double nameY = start + (fontHeight / 2) + spaceTop;
		double realNameX = fm.stringWidth(nameText) + nameX;
		
		String ipText = "IP: ";
		double ipX = shellWidth + (start * 2);
		double ipY = nameY + (fontHeight / 2) + textYBuffer;
		double realIPX = fm.stringWidth(ipText) + ipX;
		
		String portText = "Port: ";
		double portX = shellWidth + (start * 2);
		double portY = ipY + (fontHeight / 2) + textYBuffer;
		double realPortX = fm.stringWidth(portText) + portX;
		
		// Battery bar properties
		double barX = start;
		double barY = (start * 2) + shellHeight;
		double barWidth = tableWidth - (start * 2);
		double barHeight = tableHeight - shellHeight - (start * 3) - buttonHeight;
		
		/**
		//max we have is six robots on the field
		for(int a = 0; a < 2; a++) {
			for(int b = 0; b < 3; b++) {
				// Outer box
				g2d.setColor(background);
				RoundRectangle2D outerBox = new RoundRectangle2D.Float((int) xPos, (int) yPos, (int) tableWidth, (int) tableHeight, (int) rounded, (int) rounded);
			    g2d.fill(outerBox);
			    
			    // Draw robot shell
			    g2d.setColor(Color.BLACK);
				RoundRectangle2D robotShell = new RoundRectangle2D.Float((int) (xPos + Math.round(start)), (int) (yPos + Math.round(start)), (int) Math.round(shellWidth), (int) Math.round(shellHeight), (int) roundShell, (int) roundShell);
			    g2d.fill(robotShell);
			    
			    // Draw Text
			    g2d.setColor(Color.WHITE);
			    g2d.drawString(nameText, (int) (xPos + Math.round(nameX)), (int) (yPos + Math.round(nameY)));
			    g2d.drawString(ipText, (int) (xPos + Math.round(ipX)), (int) (yPos + Math.round(ipY)));
			    g2d.drawString(portText, (int) (xPos + Math.round(portX)), (int) (yPos + Math.round(portY)));
			    
			    // Draw battery bar
			    g2d.setColor(batteryBackground);
				RoundRectangle2D batteryBar = new RoundRectangle2D.Float((int) (xPos + Math.round(barX)), (int) (yPos + Math.round(barY)), (int) Math.round(barWidth), (int) Math.round(barHeight), (int) roundShell, (int) roundShell);
			    g2d.fill(batteryBar);
			    
				xPos += (tableWidth + xBuff);
			}
			yPos += (tableHeight + yBuff);
			buttonStartY += (tableHeight + yBuff);
			xPos = startX;
		}
		**/
		
		// Reset positions and new font
		xPos = startX;
		yPos = startY;
		int numberOfRobots = 0;
		
		//List<Robot> robots = new ArrayList<Robot>();
		for (int i=0; i<RobotName.names.length; i++) {
			// Only display first 6 robots in config file
			if(i > 6) break;
			String robotName = RobotName.names[i];
			Robot robot = new Robot(robotName);
			
			String fLeftColor = robot.getColourName(Robot.FRONTLEFT);
			String fRightColor = robot.getColourName(Robot.FRONTRIGHT);
			String bLeftColor = robot.getColourName(Robot.BACKLEFT);
			String bRightColor = robot.getColourName(Robot.BACKRIGHT);
			
			String ip = robot.getIp();
			String port = robot.getPort();
			Integer portNum = null;
			try { 
				portNum = Integer.parseInt(port); 
			} catch (Exception e) {}
			
			//TODO CREATE A ROBOT DRAWING METHOD, ENTER PARAMENTERS: SIZE AND LOCATION
			
			 /*****************************************************************************/
		    // Outer box
			g2d.setColor(background);
			RoundRectangle2D outerBox = new RoundRectangle2D.Float((int) xPos, (int) yPos, (int) tableWidth, (int) tableHeight, (int) rounded, (int) rounded);
		    g2d.fill(outerBox);
		    
		    // Draw robot shell
		    g2d.setColor(Color.BLACK);
			RoundRectangle2D robotShell = new RoundRectangle2D.Float((int) (xPos + Math.round(start)), (int) (yPos + Math.round(start)), (int) Math.round(shellWidth), (int) Math.round(shellHeight), (int) roundShell, (int) roundShell);
		    g2d.fill(robotShell);
		    
		    // Draw Text
		    g2d.setColor(Color.WHITE);
		    g2d.drawString(nameText, (int) (xPos + Math.round(nameX)), (int) (yPos + Math.round(nameY)));
		    g2d.drawString(ipText, (int) (xPos + Math.round(ipX)), (int) (yPos + Math.round(ipY)));
		    g2d.drawString(portText, (int) (xPos + Math.round(portX)), (int) (yPos + Math.round(portY)));
		    
		    // Draw battery bar
		    g2d.setColor(batteryBackground);
			RoundRectangle2D batteryBar = new RoundRectangle2D.Float((int) (xPos + Math.round(barX)), (int) (yPos + Math.round(barY)), (int) Math.round(barWidth), (int) Math.round(barHeight), (int) roundShell, (int) roundShell);
		    g2d.fill(batteryBar);		    		    		    		 
		    /*****************************************************************************/
		    
		    font = new Font("Ariel", Font.BOLD, 14);
			g2d.setFont(font);

			// Draw in text elements
			g2d.setColor(Color.WHITE);
		    g2d.drawString(robotName, (int) (xPos + Math.round(realNameX)), (int) (yPos + Math.round(nameY)));
		    g2d.drawString(ip, (int) (xPos + Math.round(realIPX)), (int) (yPos + Math.round(ipY)));
		    g2d.drawString(port, (int) (xPos + Math.round(realPortX)), (int) (yPos + Math.round(portY)));
			
		    // Color properties
		    double circleDiaRatio = 3.4 / 11.4; //TODO PARAMARATIZE THESE VALUES FOR ROBOT RATIOS
		    double circleDia = circleDiaRatio * shellWidth;
		    double offsetRatio = 2.2 / 11.4;
		    double offset = offsetRatio * shellWidth;
		    double x = xPos + Math.round(start);
		    double y = yPos + Math.round(start);
		    double cx;
		    double cy;
		    
		    // Draw circle colors
		    // Top Left
		    g2d.setColor(getColor(fLeftColor));
		    cx = (x) + offset - (circleDia / 2);
		    cy = (y) + offset - (circleDia / 2);
		    Ellipse2D circle1 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDia), Math.round(circleDia));
		    g2d.fill(circle1);
		    
		    // Back Left
		    g2d.setColor(getColor(bLeftColor));
		    cy = (y) + shellHeight - (offset + (circleDia / 2));
		    Ellipse2D circle2 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDia), Math.round(circleDia));
		    g2d.fill(circle2);
		    
		    // Front Right
		    g2d.setColor(getColor(fRightColor));
		    cx = (x) + shellWidth - (offset + (circleDia / 2));
		    cy = (y) + offset - (circleDia / 2);
		    Ellipse2D circle3 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDia), Math.round(circleDia));
		    g2d.fill(circle3);
		    
		    // Back Right
		    g2d.setColor(getColor(bRightColor));
		    cx = (x) + shellWidth - (offset + (circleDia / 2));
		    cy = (y) + shellHeight - (offset + (circleDia / 2));
		    Ellipse2D circle4 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDia), Math.round(circleDia));
		    g2d.fill(circle4);
		    
			xPos += (tableWidth + xBuff);
			if(i > 2)  {
				yPos += (tableHeight + yBuff);
				xPos = startX;
			}
			numberOfRobots++;
		}
		
		xPos = startX;
		yPos = startY;
		
		// Grayed out boxes
		for(int a = 0; a < 6; a++) {
			if(a >= numberOfRobots) {
				// Outer box
				g2d.setColor(Color.GRAY);
				RoundRectangle2D outerBox = new RoundRectangle2D.Float((int) xPos, (int) yPos, (int) tableWidth, (int) tableHeight, (int) rounded, (int) rounded);
				g2d.fill(outerBox);
	
				// Draw robot shell
				g2d.setColor(Color.BLACK);
				RoundRectangle2D robotShell = new RoundRectangle2D.Float((int) (xPos + Math.round(start)), (int) (yPos + Math.round(start)), (int) Math.round(shellWidth), (int) Math.round(shellHeight), (int) roundShell, (int) roundShell);
				//g2d.draw(robotShell);
	
				// Draw Text
				//g2d.setColor(Color.WHITE);
				//g2d.drawString(nameText, (int) (xPos + Math.round(nameX)), (int) (yPos + Math.round(nameY)));
				//g2d.drawString(ipText, (int) (xPos + Math.round(ipX)), (int) (yPos + Math.round(ipY)));
				//g2d.drawString(portText, (int) (xPos + Math.round(portX)), (int) (yPos + Math.round(portY)));
	
				// Draw battery bar
				g2d.setColor(batteryBackground);
				RoundRectangle2D batteryBar = new RoundRectangle2D.Float((int) (xPos + Math.round(barX)), (int) (yPos + Math.round(barY)), (int) Math.round(barWidth), (int) Math.round(barHeight), (int) roundShell, (int) roundShell);
				//g2d.draw(batteryBar);	
			}

			xPos += (tableWidth + xBuff);
			if(a == 2) {
				yPos += (tableHeight + yBuff);
				xPos = startX;
			}
		}
		
		
	}

	public void drawField(Graphics g) {
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//int x = IMAGE_WIDTH + IMAGE_BUFFER_WIDTH * 2;
		int x = IMAGE_BUFFER_WIDTH;
		int y = (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
		Color fieldGreen = new Color(18, 163, 71);
		int lineWidth = 4;
		double fieldWidth = PITCH_WIDTH - (lineWidth * 2);
		double fieldHeight = PITCH_HEIGHT - (lineWidth * 2);
		
		//outline, background, and outside white line
		g2d.setColor(Color.BLACK);
		g2d.fillRoundRect(x, y, PITCH_WIDTH, PITCH_HEIGHT, 10, 10);
		g2d.setColor(Color.WHITE);
		g2d.fillRoundRect(x + 1, y + 1, PITCH_WIDTH - 2, PITCH_HEIGHT - 2, 10, 10);
		g2d.setColor(fieldGreen);
		g2d.fillRoundRect(x + lineWidth, y + lineWidth, PITCH_WIDTH - (lineWidth * 2), PITCH_HEIGHT - (lineWidth * 2), 10, 10);
		
		//18 yard box left
		double eighteenHeightRatio = 17 / 9.5;
		double eighteenWidthRatio = 25.5 / 5;
		g2d.setColor(Color.WHITE);
		double eighteenHeight = fieldHeight / eighteenHeightRatio;
		double eighteenWidth = fieldWidth / eighteenWidthRatio;
		double y0 = ((fieldHeight - eighteenHeight) / 2) + lineWidth + y;
		g2d.fillRect(x + lineWidth, (int) y0, (int) eighteenWidth, (int) eighteenHeight);
		g2d.setColor(fieldGreen);
		g2d.fillRect(x + lineWidth, (int) y0 + lineWidth, (int) eighteenWidth - lineWidth, (int) eighteenHeight - (lineWidth * 2));
		
		//18 yard box right
		g2d.setColor(Color.WHITE);
		double x0 = (fieldWidth - eighteenWidth) + lineWidth + x;
		if(((int) x0 + (int) eighteenWidth) < (x + PITCH_WIDTH - lineWidth)) {
			//1 OFF OF THE EDGE
			x0 += 1; // TODO REPLACE ALL WITH MATH.ROUND
		}
		g2d.fillRect((int) x0, (int) y0, (int) eighteenWidth, (int) eighteenHeight);
		g2d.setColor(fieldGreen);
		g2d.fillRect((int) x0 + lineWidth, (int) y0 + lineWidth, (int) eighteenWidth - lineWidth, (int) eighteenHeight - (lineWidth * 2));
		
		//6 yard box left
		double sixHeightRatio = 17 / 4;
		double sixWidthRatio = 25.5 / 2;
		g2d.setColor(Color.WHITE);
		double sixHeight = fieldHeight / sixHeightRatio;
		double sixWidth = fieldWidth / sixWidthRatio;
		y0 = ((fieldHeight - sixHeight) / 2) + lineWidth + y;
		g2d.fillRect(x + lineWidth, (int) y0, (int) sixWidth, (int) sixHeight);
		g2d.setColor(fieldGreen);
		g2d.fillRect(x + lineWidth, (int) y0 + lineWidth, (int) sixWidth - lineWidth, (int) sixHeight - (lineWidth * 2));
		
		//6 yard box right
		g2d.setColor(Color.WHITE);
		x0 = (fieldWidth - sixWidth) + lineWidth + x;
		if(((int) x0 + (int) sixWidth) < (x + PITCH_WIDTH - lineWidth)) {
			//1 OFF OF THE EDGE
			x0 += 1;
		}
		g2d.fillRect((int) x0, (int) y0, (int) sixWidth, (int) sixHeight);
		g2d.setColor(fieldGreen);
		g2d.fillRect((int) x0 + lineWidth, (int) y0 + lineWidth, (int) sixWidth - lineWidth, (int) sixHeight - (lineWidth * 2));
		
		//center circle
		g2d.setColor(Color.WHITE);
		double circleRatio = 17 / 4.5;
		double circleDiameter = fieldHeight / circleRatio;
		x0 = (fieldWidth / 2) - (circleDiameter / 2) + x + lineWidth;
		y0 = (fieldHeight / 2) - (circleDiameter / 2) + y + lineWidth;
		g2d.fillOval((int) x0, (int) y0, (int) circleDiameter, (int) circleDiameter);
		g2d.setColor(fieldGreen);
		g2d.fillOval((int) x0 + lineWidth, (int) y0 + lineWidth, (int) circleDiameter - (lineWidth * 2), (int) circleDiameter - (lineWidth * 2));
		
		//center line
		g2d.setColor(Color.WHITE);
		x0 = (fieldWidth / 2) - (lineWidth / 2) + lineWidth + x;
		y0 = y + lineWidth;
		g2d.fillRect((int) x0, (int) y0, lineWidth, (int) fieldHeight);
		g2d.dispose();
	}

	public void drawRedSquare(Graphics g) {
		int x = (int) (xR * IMAGE_WIDTH);
		int y = (int) (yR * IMAGE_HEIGHT);
		int x1 = (int) (xR1 * IMAGE_WIDTH);
		int y1 = (int) (yR1 * IMAGE_HEIGHT);
		
		g.setColor(Color.RED);
		g.drawRect(x, y, x1 - x, y1 - y);
		//System.out.println("xR: " + xR + ", yR: " + yR + ", xR1: " + xR1 + ", yR1: " + yR1);
		//System.out.println("Frame width: " + FRAME_WIDTH);
		//System.out.println("BOX_WIDTH: " + BOX_WIDTH + ", IMAGE_BOX_HEIGHT: " + IMAGE_BOX_HEIGHT + ", PITCH_BOX_WIDTH: " + PITCH_BOX_HEIGHT + ", PITCH_RATIO: " + PITCH_RATIO);
	}
	
	public void drawRobots(Graphics g, Robot robot) {
		
		if (dbg>2) log.info(dname+" Start of new data: --------------------------------------------------------");
		
		//Robot angle
		double robotAngle = robot.getAngle(0);
		
		// Robot center x and y
		Point center = robot.getCenterPoint(0);
		double robotImageCenterX = center.x;
		double robotImageCenterY = center.y;
		
		if (dbg>2) log.info(dname+" robotImageCenterX: " + robotImageCenterX + " , robotImageCenterY: " + robotImageCenterY);
		
		// Robot colors
		String fLeftColor = robot.getColourName(Robot.FRONTLEFT);
		String fRightColor = robot.getColourName(Robot.FRONTRIGHT);
		String bLeftColor = robot.getColourName(Robot.BACKLEFT);
		String bRightColor = robot.getColourName(Robot.BACKRIGHT);

		// Initialize graphics and turn on antialiasing to smoothen pixels
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Take robot center position, then convert it into the window coordinate system
	    double robotCenterWindowImageX = ((robotImageCenterX / IMAGE_RESOLUTION_WIDTH) * IMAGE_WIDTH) + IMAGE_BUFFER_WIDTH;
	    double robotCenterWindowImageY = ((robotImageCenterY / IMAGE_RESOLUTION_HEIGHT) * IMAGE_HEIGHT) + IMAGE_BUFFER_HEIGHT;
	    
	    if (dbg>2) log.info(dname+" robotWindowImageX: " + robotCenterWindowImageX + " , robotWindowImageY: " + robotCenterWindowImageY);
	    
	    // TODO ROBOT RATIO CONFIG FILE
	    
	    // Small white square that connects all the colors together on the camera image
	    g2d.setColor(Color.WHITE);
	    Rectangle rect1 = new Rectangle((int) (robotCenterWindowImageX - (ROBOT_COLOR_BOX_WIDTH / 2)), (int) (robotCenterWindowImageY - (ROBOT_COLOR_BOX_HEIGHT / 2)), (int) ROBOT_COLOR_BOX_WIDTH, (int) ROBOT_COLOR_BOX_HEIGHT);
	    g2d.rotate(Math.toRadians(robotAngle + 90), robotCenterWindowImageX, robotCenterWindowImageY);
	    g2d.draw(rect1);
	    g2d.dispose();
	    
	    // Blue outline on webcam image
	    g2d = (Graphics2D)g.create();
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setColor(Color.BLUE);
	    
	    // Add width to the white square we drew earlier to encompass the entire robot (estimated for now)
	    double widthBuffer = (11d / 31d) * ROBOT_COLOR_BOX_WIDTH;
	    double heightbuffer = (11d / 31d) * ROBOT_COLOR_BOX_HEIGHT;
	    
	    // How much to round the edges of the blue outline
	    double rounded = 8;
	    
	    // Add the width & height buffer to the width & height we're going to draw
	    double blueOutlineWidth = ROBOT_COLOR_BOX_WIDTH + (widthBuffer * 2);
	    double blueOutlineHeight = ROBOT_COLOR_BOX_HEIGHT + (heightbuffer * 2);
	    
	    // Subtract half of the width and height of the rectangle to get top left corner for drawing
	    double robotBlueOutlineTopLeftX = (robotCenterWindowImageX - ((blueOutlineWidth) / 2));
	    double robotBlueOutlineTopLeftY = (robotCenterWindowImageY - ((blueOutlineHeight) / 2));
	    
	    // Draw the blue outline
	    RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float((int) robotBlueOutlineTopLeftX, (int) robotBlueOutlineTopLeftY, (int) blueOutlineWidth, (int) blueOutlineHeight, (int) rounded, (int) rounded);
	    g2d.rotate(Math.toRadians(robotAngle + 90), robotCenterWindowImageX, robotCenterWindowImageY);
	    g2d.draw(roundedRectangle);
	    g2d.dispose();
	    
	    if (dbg>2) log.info(dname+" robotBlueOutlineTopLeftX: " + robotBlueOutlineTopLeftX + " , robotBlueOutlineTopLeftY: " + robotBlueOutlineTopLeftY);
	    
	    // Black body of robot on field
	    g2d = (Graphics2D)g.create();
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setColor(Color.BLACK);
	    
	    // Top left x and y of the field
	    double fieldX = IMAGE_BUFFER_WIDTH;
		double fieldY = IMAGE_HEIGHT + (IMAGE_BUFFER_HEIGHT * 2);
		
		// Adjusted width and height of the robot for the size of the pitch
	    double robotWidthOnField = (blueOutlineWidth / (MAX_X_FILTER - MIN_X_FILTER)) * PITCH_WIDTH;
	    double robotHeightOnField = (blueOutlineHeight / (MAX_Y_FILTER - MIN_Y_FILTER)) * PITCH_HEIGHT;
	    
	    if (dbg>2) log.info(dname+" robotFieldWidth: " + robotWidthOnField + " , robotFieldHeight: " + robotHeightOnField);
	    
	    // Scale the center x and y of the robot to the pitch
	    double robotOnFieldTopLeftX = scaleWidthOfCameraToField(robotImageCenterX) + fieldX - (robotWidthOnField / 2);
	    double robotOnFieldTopLeftY = scaleHeightOfCameraToField(robotImageCenterY) + fieldY - (robotHeightOnField / 2);
	    
	    // Draw the robot on the pitch
	    RoundRectangle2D roundedRectangle2 = new RoundRectangle2D.Float((int) robotOnFieldTopLeftX, (int) robotOnFieldTopLeftY, (int) robotWidthOnField, (int) robotHeightOnField, (int) rounded, (int) rounded);
	    g2d.rotate(Math.toRadians(robotAngle + 90), robotOnFieldTopLeftX + (roundedRectangle2.getWidth() / 2), robotOnFieldTopLeftY + (roundedRectangle2.getHeight() / 2));
	    g2d.fill(roundedRectangle2);
	    
	    // Circles of color on the robot
	    double circleOffsetRatioWidth = 2.3 / 11.4; //TODO ROBOT RATIO CONFIG FILE
	    double circleOffsetRatioHeight = 2.3 / 11.4;
	    double circleDiaRatioWidth = 3.5 / 11.4;
	    double circleDiaRatioHeight = 3.5 / 11.4;
	    double circleOffsetWidth = circleOffsetRatioWidth * robotWidthOnField;
	    double circleOffsetHeight = circleOffsetRatioHeight * robotHeightOnField;
	    double circleOffsetAvg = (circleOffsetWidth + circleOffsetHeight) / 2;
	    double circleDiaWidth = circleDiaRatioWidth * robotWidthOnField;
	    double circleDiaHeight = circleDiaRatioHeight * robotHeightOnField;
	    double circleDiaAvg = (circleDiaWidth + circleDiaHeight) / 2;
	    
	    // Start position for circles
	    double cx;
	    double cy;
	    
	    // Top Right
	    //g2d.setColor(getColor(fRightColor));
	    g2d.setColor(getColor(bRightColor));
	    cx = (robotOnFieldTopLeftX) + circleOffsetAvg - (circleDiaAvg / 2);
	    cy = (robotOnFieldTopLeftY) + circleOffsetAvg - (circleDiaAvg / 2);
	    Ellipse2D circle1 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDiaAvg), Math.round(circleDiaAvg));
	    g2d.fill(circle1);
	    
	    // Top Left
	    //g2d.setColor(getColor(fLeftColor));
	    g2d.setColor(getColor(fRightColor));
	    cy = (robotOnFieldTopLeftY) + robotHeightOnField - (circleOffsetAvg + (circleDiaAvg / 2));
	    Ellipse2D circle2 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDiaAvg), Math.round(circleDiaAvg));
	    g2d.fill(circle2);
	    
	    // Bottom Right
	    //g2d.setColor(getColor(bRightColor));
	    g2d.setColor(getColor(bLeftColor));
	    cx = (robotOnFieldTopLeftX) + robotWidthOnField - (circleOffsetAvg + (circleDiaAvg / 2));
	    cy = (robotOnFieldTopLeftY) + circleOffsetAvg - (circleDiaAvg / 2);
	    Ellipse2D circle3 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDiaAvg), Math.round(circleDiaAvg));
	    g2d.fill(circle3);
	    
	    // Bottom Left
	    //g2d.setColor(getColor(bLeftColor));
	    g2d.setColor(getColor(fLeftColor));
	    cx = (robotOnFieldTopLeftX) + robotWidthOnField - (circleOffsetAvg + (circleDiaAvg / 2));
	    cy = (robotOnFieldTopLeftY) + robotHeightOnField - (circleOffsetAvg + (circleDiaAvg / 2));
	    Ellipse2D circle4 = new Ellipse2D.Float(Math.round(cx), Math.round(cy), Math.round(circleDiaAvg), Math.round(circleDiaAvg));
	    g2d.fill(circle4);
	    g2d.dispose();
	}

	private Color getColor(String colorString) {
		if(colorString.equals("green")) {
			return circleGreen;
		} else if(colorString.equals("red")) {
			return circleRed;
		} else if(colorString.equals("purple")) {
			return circlePurple;
		} else {
			return Color.WHITE;
		}
	}
	
	public boolean checkInsideField(int x, int y) {
		// Field top left corner and bottom right corner coordinates
		int field_min_x = IMAGE_BUFFER_WIDTH;
		int field_min_y = (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
		int field_max_x = PITCH_WIDTH + IMAGE_BUFFER_WIDTH;
		int field_max_y = PITCH_HEIGHT + (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
		
		// Width of the robot on the field
		int robotWidthOnField = 73; // TODO ROBOT CONFIG FILE
		
		// Make sure we don't click somewhere the robot cannot reach because of it's width
		field_min_x += (robotWidthOnField / 2);
		field_max_x -= (robotWidthOnField / 2);
		field_min_y += (robotWidthOnField / 2);
		field_max_y -= (robotWidthOnField / 2);
		
		System.out.println("field_min_x: " + field_min_x + ", field_max_x: " + field_max_x + ", field_min_y: " + field_min_y + ", field_max_y: " + field_max_y);
		
		//check if were we clicked is inside of this boundary
		if(x > field_min_x && x < field_max_x && y > field_min_y && y < field_max_y) {
			// we have clicked inside the field
			System.out.println("CLICKED INSIDE THE FIELD");
			return true;
		} else {
			// we have clicked outside of the field
			System.out.println("clicked outside of the field");
			return false;
		}
		
		

	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("x: " + e.getX() + ", y: " + e.getY());
		
		// Check if we clicked inside of the field
		if(checkInsideField(e.getX(), e.getY())) {
			// Send command to go to point TODO
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {}
}