package camera;

import org.opencv.core.Mat;

public interface ITask {
 	public String perform(Robot robot, Mat image);
}
