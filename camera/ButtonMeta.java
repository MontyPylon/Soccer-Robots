package camera;

import javax.swing.JButton;

public class ButtonMeta {

	String name;
	ITask task;
	String actionCommand;
	JButton theJButton;
	int idNumber;
	boolean pressed;
	
	public ButtonMeta(String name, String actionCommand, ITask task) {
		this.name = name;
		this.actionCommand = actionCommand;
		this.task = task;
	}
	
	public ButtonMeta(String name, String actionCommand, int idNumber) {
		this.name = name;
		this.actionCommand = actionCommand;
		this.idNumber = idNumber;
	}
}
