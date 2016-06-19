package camera;

import java.awt.Dimension;

public interface Const {
	
	
	//max number of objects to be detected in frame
	public static final int MAX_NUM_OBJECTS = 50;
	
	Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	
	// Webcam image properties
	public static final double imageRatio = 16 / 9d;
	public static final int IMAGE_RESOLUTION_WIDTH = 640;
	public static final int IMAGE_RESOLUTION_HEIGHT = 360;
	public static final int IMAGE_BUFFER_WIDTH = 20;
	public static final int IMAGE_BUFFER_HEIGHT = 20;
	
	// Frame properties
	public static final double widthRatio = .8d;
	public static final int FRAME_WIDTH = (int) (screen.width * widthRatio);
	public static final int FRAME_HEIGHT = (int) (FRAME_WIDTH / imageRatio);
	
	// Red square ratios for field:
	public static final double xR = 0.13286713286713286d;
	public static final double yR = 0.1044776119402985d;
	public static final double xR1 = 0.8853146853146853d;
	public static final double yR1 = 0.9776119402985075d;
	
	// Color filter for only inside the red square
	public static final double MIN_X_FILTER = (xR * IMAGE_RESOLUTION_WIDTH);
	public static final double MAX_X_FILTER = (xR1 * IMAGE_RESOLUTION_WIDTH);
	public static final double MIN_Y_FILTER = (yR * IMAGE_RESOLUTION_HEIGHT);
	public static final double MAX_Y_FILTER = (yR1 * IMAGE_RESOLUTION_HEIGHT);
	
	// Math for positioning image and pitch with correct ratios
	public static final double workspace = FRAME_HEIGHT - (IMAGE_BUFFER_HEIGHT * 3);
	public static final double RESOLUTION_RATIO = ((double) IMAGE_RESOLUTION_WIDTH / (double) IMAGE_RESOLUTION_HEIGHT);
	public static final double PITCH_RATIO = (MAX_X_FILTER - MIN_X_FILTER) / (MAX_Y_FILTER - MIN_Y_FILTER);
	public static final double BOX_WIDTH = workspace / ((1 / RESOLUTION_RATIO) + (1 / PITCH_RATIO));
	public static final double IMAGE_BOX_HEIGHT = BOX_WIDTH / RESOLUTION_RATIO;
	public static final double PITCH_BOX_HEIGHT = BOX_WIDTH / PITCH_RATIO;
	
	// Sizes of boxes that contain image and pitch
	public static final int IMAGE_HEIGHT = (int) Math.round(IMAGE_BOX_HEIGHT);
	public static final int IMAGE_WIDTH = (int) Math.round(BOX_WIDTH);
	public static final int PITCH_HEIGHT = (int) Math.round(PITCH_BOX_HEIGHT);
	public static final int PITCH_WIDTH = IMAGE_WIDTH;
	
	// Distances between the colors (not diagonals)
	public static final int ROBOT_COLOR_BOX_WIDTH = 31;
	public static final int ROBOT_COLOR_BOX_HEIGHT = 31;
	
	public static final int MIN_OBJECT_AREA = 100;
	public static final int MAX_OBJECT_AREA = 1000;
	
	public static final int MIN_ADJACENT_DIST = 20; //35
	public static final int MAX_ADJACENT_DIST = 35; //55
	
	public static final int MIN_OPPOSITE_DIST = 35; //55
	public static final int MAX_OPPOSITE_DIST = 50; //75
	
	public static final int DIST_TYPE_ADJACENT = 1;
	public static final int DIST_TYPE_OPPOSITE = 2;
	
	public static final int CENTER_X = (int) (((MAX_X_FILTER - MIN_X_FILTER) / 2) + MIN_X_FILTER);
	public static final int CENTER_Y = (int) (((MAX_Y_FILTER - MIN_Y_FILTER) / 2) + MIN_Y_FILTER);
}