package camera;

import org.opencv.core.Scalar;

public class TObject {
	private int xPos;
	private int yPos;
	private int area;
	private String colourName;
	private Scalar hsvMin;
	private Scalar hsvMax;
	private Scalar colour;
	private int id;
	
	static int IDCOUNTER;
	
	public TObject(){
		id = IDCOUNTER++;
		xPos = 0;
		yPos = 0;
		colourName = ColorName.NONE;
		int hMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.hMin"));
		int sMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.sMin"));
		int vMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.vMin"));
		int hMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.hMax"));
		int sMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.sMax"));
		int vMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color.red.vMax"));
		setHsvMin(new Scalar(hMin, sMin, vMin));
		setHsvMax(new Scalar(hMax, sMax, vMax));
		setColour(new Scalar(0, 0, 255));
	}

	public TObject(String colourName){
		id = IDCOUNTER++;
		setColourName(colourName);
		int hMin = 0;
		int sMin = 0;
		int vMin = 0;
		int hMax = 0;
		int sMax = 0;
		int vMax = 0;
		
		xPos = 0;
		yPos = 0;
		hsvMax = new Scalar(0, 0, 0);
		hsvMin = new Scalar(0, 0, 0);
		colour = new Scalar(0, 0, 0);
		
		if (ColorName.isValidName(colourName)) {		
			String lowerColourName = colourName.toLowerCase();
			hMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".hMin"));
			sMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".sMin"));
			vMin = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".vMin"));
			hMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".hMax"));
			sMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".sMax"));
			vMax = new Integer(PropertiesLoader.getValue("multipleObjectTracking.color."+lowerColourName+".vMax"));
			setColour(new Scalar(0, 0, 255));		
		} 
		setHsvMin(new Scalar(hMin, sMin, vMin));
		setHsvMax(new Scalar(hMax, sMax, vMax));
	}

	public int getXPos() {
		return xPos;
	}

	public void setXPos(int xPos) {
		this.xPos = xPos;
	}

	public int getYPos() {
		return yPos;
	}

	public void setYPos(int yPos) {
		this.yPos = yPos;
	}

	public String getColourName() {
		return colourName;
	}

	public void setColourName(String colourName) {
		this.colourName = colourName;
	}

	public Scalar getHsvMin() {
		return hsvMin;
	}

	public void setHsvMin(Scalar hsvMin) {
		this.hsvMin = hsvMin;
	}

	public Scalar getHsvMax() {
		return hsvMax;
	}

	public void setHsvMax(Scalar hsvMax) {
		this.hsvMax = hsvMax;
	}

	public Scalar getColour() {
		return colour;
	}

	public void setColour(Scalar colour) {
		this.colour = colour;
	}

	public int getArea() {
		return area;
	}

	public void setArea(int area) {
		this.area = area;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		return id+":"+colourName+":"+xPos+":"+yPos+":"+area;
	}
	
}
