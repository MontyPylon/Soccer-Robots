package camera;

public class TObjectPair {
    
	public static int FRONTLEFT_FRONTRIGHT = 0;
	public static int FRONTLEFT_BACKRIGHT = 1;
	public static int FRONTLEFT_BACKLEFT = 2;
	public static int FRONTRIGHT_BACKLEFT = 3;
	public static int FRONTRIGHT_BACKRIGHT = 4;
	public static int BACKLEFT_BACKRIGHT = 5;
	
	TObject t1;
    TObject t2;
     
    int type;
     
    public TObjectPair(TObject t1, TObject t2, int type) {
    	this.t1 = t1;
    	this.t2 = t2;
    	this.type = type;
    }
    
    
}
