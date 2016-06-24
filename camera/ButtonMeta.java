package camera;

import javax.swing.JButton;

public class ButtonMeta {

	String name;
	
	ITask task;
	
	String actionCommand;
	
	JButton theJButton;
	
	boolean pressed;
	
	public ButtonMeta(String name, String actionCommand, ITask task) {
		this.name = name;
		this.actionCommand = actionCommand;
		this.task = task;
	}
}
