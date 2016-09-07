package camera;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ProcessImage {
	
	private Mat processImage(Mat hsvImage, Scalar hsvMin, Scalar hsvMax){
		Mat thresholdedImage = new Mat();
		Core.inRange(hsvImage, hsvMin , hsvMax, thresholdedImage);
		morphOps(thresholdedImage);
		
		return thresholdedImage;
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
	/**
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
	/**
		}
	}
	**/
}
