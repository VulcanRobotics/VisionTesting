package proj_vision_testing;

import edu.wpi.first.wpilibj.networktables.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Rect;

/**
* @author kscholl
* 
* A GRIP runner is a convenient wrapper object to make it easy to run vision
* pipelines from robot code.
*/

public class Main {
	@SuppressWarnings("unused")
	private static double centerX = 0.0;
	private final static Object imgLock = new Object();

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String current_lib_path = System.getProperty("java.library.path");
		String native_lib  = Core.NATIVE_LIBRARY_NAME;
		System.out.println("java.library.path is located at " + current_lib_path);
		System.out.println("NATIVE_LIBRARY_NAME is " + native_lib);
		
		String os = System.getProperty("os.name");
		System.out.println("os: " + os);
		
		if (os == "Mac OS X") {
			System.out.println("Mac OS X");
		} else {
			//System.loadLibrary("/home/pi/vision/");
		}
		
		// Connect NetworkTables, and get access to the publishing table
		NetworkTable.setClientMode();
		NetworkTable.setTeam(1218);
		NetworkTable.initialize();
		NetworkTable table;
		table = NetworkTable.getTable("vision");
		
		GripPipeline pipeline = new GripPipeline();
		
		// IP Camera
		String cameraName   = "vision_camera0";
		String cameraIP     = "http://10.12.18.10/mjpg/video.mjpg";
		String newCameraIP  = "http://10.12.18.11/mjpg/video.mjpg";
		String newCameraIP0 = "http://10.12.18.11/view/viewer_index.shtml?id=18";
		
		// MAC OS X (system camera): use VideoCapture(1);
		VideoCapture cap = new VideoCapture();
		int CAM_TO_USE = 0;
		int BACKUP_CAM = 1;
		
		
//		CameraServer cameraServer = new CameraServer();
//		cameraServer.addServer(newCameraIP);

		Mat matFrame = new Mat();
		
		/** 
		 * Try connecting to an ip camera
		 * If unsuccessful, try usb ports
		 */
		System.out.println("");
		System.out.println("Attempting to grab the IP Camera from: " + newCameraIP);
		if (cap.open(newCameraIP)) {
			//if (cap.open(newCameraIP + ".mjpg")) {
			System.out.println("Camera opened from " + newCameraIP);
		} else {
			System.out.println("Error:  No camera found at the given address. Trying USB ports...");
			boolean opened = false;
			if (opened == false) {
				for (int i = 0; i < 5; i++) {
					System.out.println("port: " + i);
					if (cap.open(i)) {
						System.out.println("Camera opened from port " + i);
						break;
					}
				}
			} else {
				System.out.println("Multiple Errors:  No IP Camera found at the given address. No USB cameras found in ports 0-4.");
			}
//			
//			if (cap.open(CAM_TO_USE)) {
//				System.out.println("Camera opened from port " + CAM_TO_USE);
//			} else if (cap.open(BACKUP_CAM)) {
//				System.out.println("Camera opened from port " + BACKUP_CAM);
//			}
		}

		// if the camera is opened
		if (cap.isOpened()) {
			boolean keepProcessing = true;
			while (keepProcessing) {
				// grab the next frame from video source
				cap.grab();

				// decode and return the grabbed video frame
				cap.retrieve(matFrame);

				// get the camera's frames per seconds
				int CV_CAP_PROP_FPS = 5;
				double fps = cap.get(CV_CAP_PROP_FPS);
				//System.out.println("Frames per second: " + fps);
				table.putNumber("fps", fps);
				
				// if the frame is valid (not end of video for example)
				if (!(matFrame.empty())) {
					// *** any additional processing here *** 
					pipeline.process(matFrame);
					if (!pipeline.filterContoursOutput().isEmpty()) {
						//System.out.println("filterContours is not empty");
						Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
						table.putNumber("x", r.x);
						table.putNumber("y", r.y);
						synchronized (imgLock) {
							centerX = r.x + (r.width / 2);
						}
						System.out.println("r: " + r);
						System.out.println("r.x: " + r.x);
						System.out.println("r.y: " + r.y);
						System.out.println("(" + r.x + ", " + r.y + ")");
					} else {
						System.out.println("filterContours is empty");
					}
					
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else { 
					keepProcessing = false;
				}
			}
		} else {
			System.out.println("error cannot open any capture source -- exiting");
		}
	}
}

