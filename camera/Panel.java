package camera;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.opencv.core.Mat;

public class Panel extends JPanel implements Const {
	private static final long serialVersionUID = 4913353253704963163L;

	private BufferedImage image;
	private final boolean calibrationMode = new Boolean(PropertiesLoader.getValue("multipleObjectTracking.calibrationMode"));
	NormalWindow w;

	public Panel() {
		super();
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage newImage) {
		image = newImage;
	}
	
	public void setWindow(NormalWindow n) {
		w = n;
	}
	
	public void setImageWithMat(Mat newimage) {
		image = ImageManipulations.matToBufferedImage(newimage);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
        if (image != null){
        	if(calibrationMode) {
        		//g.drawImage(image, 10, 10, image.getWidth(), image.getHeight(), this);
        		g.drawImage(image, 10, 10, IMAGE_WIDTH, IMAGE_HEIGHT, this);
        	} else {
        		g.drawImage(image, IMAGE_BUFFER_WIDTH, IMAGE_BUFFER_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT, this);
        		w.drawWindowComponents(g);
        	}
        }
        
	}
}
