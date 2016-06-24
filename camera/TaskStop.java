package camera;

import org.opencv.core.Mat;

public class TaskStop implements ITask, Const {

	public String perform(Robot robot, Mat image) {
		String command = "+000+000x";
		return command;
	}

}
