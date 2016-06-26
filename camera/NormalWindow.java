package camera;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.opencv.core.Point;

public class NormalWindow implements ActionListener, Const, MouseListener {
	
	private static final Logger log = Logger.getLogger(NormalWindow.class);
    public static final int dbg = 3;
    public static final String dname = "NORMW";
	private JFrame frame;
	private Panel panel = new Panel();
	List robotsToPaint = new ArrayList();
	int spaceBetweenButtons = 20;
	private Map clientIPMap = new HashMap(); 
	Map buttonMetaMap = new HashMap();
	Map connButtonsMap = new HashMap();
	Map discButtonsMap = new HashMap();
	public static double mouseX = CENTER_X;
	public static double mouseY = CENTER_Y;
	Color connectGreen = new Color(51, 214, 89);
	Color disconnectRed = new Color(227, 61, 78);

	public NormalWindow() {
		initialize();
	}

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
	
	public void createButton(String name, String actionCommand, ITask task) {
		int startX = IMAGE_WIDTH + (IMAGE_BUFFER_WIDTH * 2);
		int startY = (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
		int buttonHeight = 40;
	    int buttonWidth = (IMAGE_WIDTH - (spaceBetweenButtons * (5 - 1))) / (5);
		int add = spaceBetweenButtons + buttonWidth;
	    
		JButton jb = new JButton(name);
		
		ButtonMeta bm = new ButtonMeta(name, actionCommand, task);
		bm.theJButton = jb;
		
		int i = buttonMetaMap.size();
		jb.setBounds(startX + (add * i), startY, buttonWidth, buttonHeight);
		
		jb.setActionCommand(actionCommand);
		jb.addActionListener(this);
		frame.getContentPane().add(jb);
		
		if (!buttonMetaMap.containsKey(actionCommand)) {
		    buttonMetaMap.put(actionCommand, bm);
		} else {
			System.out.println("Duplicate button !!!! "+actionCommand);
		}
		
	}
	
	public void intializeButtons() {
		createButton("Stop", "disable", new TaskStop());
		createButton("Go To Center", "goCenter", new TaskGoToCenter());
		createButton("Turn Click", "turnMouse", new TaskTurnToMouseClick());
		createButton("Mouse Click", "goMouse", new TaskGoToMouseClick());
		connectDisconnectButtons();
	}
	
	private void connectDisconnectButtons() {
		
		double widthPanel = FRAME_WIDTH - IMAGE_WIDTH - (IMAGE_BUFFER_WIDTH * 3);
		double heightPanel = IMAGE_HEIGHT;
		double startX = IMAGE_WIDTH + (IMAGE_BUFFER_WIDTH * 2);
		double startY = IMAGE_BUFFER_HEIGHT;
		double xBuff = 20;
		double yBuff = 20;
		double tableWidth = (widthPanel - (xBuff * 2)) / 3;
		double tableHeight = (heightPanel - yBuff) / 2;
//		double rounded = 10;
		double buttonRatioHeight = .2;
		double xPos = startX;
		double yPos = startY + tableHeight - (tableHeight * buttonRatioHeight);
		double buttonWidth = tableWidth / 2;
		double buttonHeight = tableHeight * buttonRatioHeight;
		
		for(int a = 0; a < 6; a++) {
			JButton jb = new JButton("Connect");
			String actionCommand = "connect" + Integer.toString(a);
			ButtonMeta bm = new ButtonMeta("Connect", actionCommand, a);
			bm.theJButton = jb;
			jb.setBounds((int) (Math.round(xPos)), (int) Math.round(yPos), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
			if(a == 2) {
				xPos = startX;
				yPos += (tableHeight + yBuff);
			} else {
				xPos += (tableWidth + xBuff);
			}
			jb.setActionCommand(actionCommand);
			jb.addActionListener(this);
			jb.setBackground(Color.GRAY);
			jb.setForeground(Color.BLACK);
			jb.setEnabled(false);
			frame.getContentPane().add(jb);
			
			if (!connButtonsMap.containsKey(a)) {
				connButtonsMap.put(a, bm);
			} else {
				System.out.println("Duplicate button !!!! "+a);
			}
		}
		
		xPos = startX;
		yPos = startY + tableHeight - (tableHeight * buttonRatioHeight);
		
		for(int a = 0; a < 6; a++) {
			JButton jb = new JButton("Disconnect");
			String actionCommand = "disconnect" + Integer.toString(a);
			ButtonMeta bm = new ButtonMeta("Disconnect", actionCommand, a);
			bm.theJButton = jb;
			int sx = (int) (Math.round(xPos) + buttonWidth);
			jb.setBounds(sx, (int) Math.round(yPos), (int) Math.round(buttonWidth), (int) Math.round(buttonHeight));
			if(a == 2) {
				xPos = startX;
				yPos += (tableHeight + yBuff);
			} else {
				xPos += (tableWidth + xBuff);
			}
			jb.setActionCommand(actionCommand);
			jb.addActionListener(this);
			jb.setBackground(Color.GRAY);
			jb.setForeground(Color.WHITE);
			jb.setEnabled(false);
			frame.getContentPane().add(jb);
			
			if (!discButtonsMap.containsKey(a)) {
				discButtonsMap.put(a, bm);
			} else {
				System.out.println("Duplicate button !!!! "+a);
			}
		}
	}
	
	public void enableConnectButtons() {
		for (int i=0; i<RobotName.names.length; i++) {
			Iterator iter = connButtonsMap.keySet().iterator();
	    	while (iter.hasNext()) {
	    		int idNumber = (int)iter.next();	
	    		ButtonMeta bm = (ButtonMeta)connButtonsMap.get(idNumber);
		    	if(bm.idNumber == i) {
		    		bm.theJButton.setEnabled(true);
		    		bm.theJButton.setBackground(connectGreen);
		    	}
	    	}
		}
	}
	
	public Map getClientMap() {
		return clientIPMap;
	}

	public void actionPerformed(ActionEvent e) {
		
		Iterator iter = buttonMetaMap.keySet().iterator();
    	while (iter.hasNext()) {
    		String act = (String)iter.next();	
    		ButtonMeta bm = (ButtonMeta)buttonMetaMap.get(act);
	    	if (act.equals(e.getActionCommand())) {
		    	bm.pressed = true;
	    	} else {
	    		bm.pressed = false;
	    	}
    	}
    	
    	Iterator iter2 = connButtonsMap.keySet().iterator();
    	while (iter2.hasNext()) {
    		int idNumber = (int)iter2.next();	
    		ButtonMeta bmc = (ButtonMeta)connButtonsMap.get(idNumber);
    		if(bmc.actionCommand.equals(e.getActionCommand())) {
    			if(connectToRobot(bmc.idNumber)) {
    				bmc.theJButton.setBackground(Color.GRAY);
    				bmc.theJButton.setEnabled(false);
    				ButtonMeta bmd = (ButtonMeta)discButtonsMap.get(idNumber);
    				bmd.theJButton.setEnabled(true);
    				bmd.theJButton.setBackground(disconnectRed);
    			} else {
    				if (dbg>1) log.info(dname+" Could not connect.");
    			}
	    	}
    	}
    	
    	Iterator iter3 = discButtonsMap.keySet().iterator();
    	while (iter3.hasNext()) {
    		int idNumber = (int)iter3.next();	
    		ButtonMeta bmd = (ButtonMeta)discButtonsMap.get(idNumber);
    		if(bmd.actionCommand.equals(e.getActionCommand())) {
    			disconnectFromRobot(bmd.idNumber);
    	    	bmd.theJButton.setBackground(Color.GRAY);
    	    	bmd.theJButton.setEnabled(false);
    	    	ButtonMeta bmc = (ButtonMeta)connButtonsMap.get(idNumber);
    			bmc.theJButton.setEnabled(true);
    	    	bmc.theJButton.setBackground(connectGreen);
	    	}
    	}
	}
	
	public boolean connectToRobot(int robotID) {
		
		boolean didConnect = false;
		
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
			
			if (ip != null && portNum != null && i == robotID) {
				if (dbg>1) log.info(robotName+" attempting to connect...");
				didConnect = sc.init(ip, portNum);
				if (dbg>1) log.info(robotName+" ip="+ip+" port="+port);
				clientIPMap.put(robotName, sc);
			} else {
				if (dbg>1) log.info(dname+" No ip/port for "+robotName);
			}
		}
		return didConnect;
	}
	
	public void disconnectFromRobot(int robotID) {
		// Connect to only robot with correct robotID
		List<Robot> robots = new ArrayList<Robot>();
		for (int i=0; i<RobotName.names.length; i++) {
			String robotName = RobotName.names[i];
			Robot robot = new Robot(robotName);
			robots.add(robot);
			SocketClient sc = (SocketClient)clientIPMap.get(robotName);
			sc.close();
		}
		
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public Panel getPanel() {
		return panel;
	}

	public void sendRobots(List<Robot> robots) {
		robotsToPaint = robots;
	}

	public void drawWindowComponents(Graphics g) {
		
		DrawComponents draw = new DrawComponents(g, robotsToPaint);
		draw.drawField();
		draw.drawRedSquare();
		draw.drawRobotTables();
		draw.drawRobots();
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
		
		 if (dbg>2) log.info(dname+"field_min_x: " + field_min_x + ", field_max_x: " + field_max_x + ", field_min_y: " + field_min_y + ", field_max_y: " + field_max_y);
		
		//check if were we clicked is inside of this boundary
		if(x > field_min_x && x < field_max_x && y > field_min_y && y < field_max_y) {
			// we have clicked inside the field
			 if (dbg>2) log.info(dname+"CLICKED INSIDE THE FIELD");
			return true;
		} else {
			// we have clicked outside of the field
			 if (dbg>2) log.info(dname+"clicked outside of the field");
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
		
		// First check if the we are in the mouse click mode  
		ButtonMeta bm = (ButtonMeta)buttonMetaMap.get("goMouse");
        if (bm.pressed) {
        	// Check if we clicked inside of the field
    		if(checkInsideField(e.getX(), e.getY())) {
    			// Send command to go to point TODO
    			double field_min_x = IMAGE_BUFFER_WIDTH;
    			double field_min_y = (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
    			double field_max_x = PITCH_WIDTH + IMAGE_BUFFER_WIDTH;
    			double field_max_y = PITCH_HEIGHT + (IMAGE_BUFFER_HEIGHT * 2) + IMAGE_HEIGHT;
    			
    			mouseX = Math.round(((e.getX() -  field_min_x) / (double) PITCH_WIDTH) * (double) IMAGE_WIDTH);
    			mouseY = Math.round(((e.getY() - field_min_y) / (double) PITCH_HEIGHT) * (double) IMAGE_HEIGHT);
    			
    			System.out.println("adjusted x: " + mouseX + ", adjusted y: " + mouseY);
    		}
        }
	}

	@Override
	public void mouseReleased(MouseEvent e) {}
	

}