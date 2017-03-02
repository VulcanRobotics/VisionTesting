package proj_vision_testing;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.first.wpilibj.vision.VisionThread;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;


import org.opencv.core.Rect;
//import com.ctre.CANTalon;



/**
 * @author kscholl
 * 
 * A GRIP runner is a convenient wrapper object to make it easy to run vision
 * pipelines from robot code.
 */

public class Main {
	
	private static NetworkTable networkTable;
	
	private static VisionThread visionThread;
	@SuppressWarnings("unused")
	private static double centerX = 0.0;
	
	private final static Object imgLock = new Object();
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		//System.setProperty("java.library.path", "/Users/kscholl/eclipse_library_plugins/libraries");
		String current_lib_path = System.getProperty("java.library.path");
		String native_lib  = Core.NATIVE_LIBRARY_NAME;
		System.out.println("java.library.path is located at " + current_lib_path);
		System.out.println("NATIVE_LIBRARY_NAME is " + native_lib);
		
		//System.loadLibrary("cscore");
		
		// Loads our OpenCV library. This MUST be included
		//System.loadLibrary("opencv_java320");
		//System.loadLibrary("opencv-320");
		//System.loadLibrary("opencv");
	    
	    // Connect NetworkTables, and get access to the publishing table
	    NetworkTable.setClientMode();
	    // Team number here
	    NetworkTable.setTeam(1218);
	    NetworkTable.initialize();
	    
	    GripPipeline pipeline = new GripPipeline();
	    
	    
	    // MARK Camera injection into GripPipeline
	    Mat source = new Mat();
        Mat output = new Mat();
        
        Mat frame  = new Mat();
        Mat frame0 = new Mat();
        
	    System.out.println("Frame Obtained");
	    System.out.println("Captured Frame Width " + frame.width());
        
        System.out.println("Frame Obtained");
	    System.out.println("Captured Frame Width " + frame.width());
	    System.out.println("source: " + source);
	    
		
	 // All Mats and Lists should be stored outside the loop 
	 // to avoid allocations, as they are expensive to create
	 	Mat inputImage = new Mat();
	 	Mat hsv = new Mat();
	 	
	    visionThread = new VisionThread(camera0, pipeline, p -> {
	        System.out.println("visonThread ran");
	    	if (!pipeline.filterContoursOutput().isEmpty()) {
	            Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
	            networkTable.putNumber("x", r.x);
	            networkTable.putNumber("y", r.y);
	            synchronized (imgLock) {
	                centerX = r.x + (r.width / 2);
	            }
	        }
	    });
	    visionThread.start();
	    
	    
	    
	    
	    
	    
	    // MARK Post Processing
		/** This creates a CvSource to use. This will take 
		 * in a Mat image that has had OpenCV operations
		 
		// operations
		CvSource imageSource = new CvSource("CV Image Source", VideoMode.PixelFormat.kMJPEG, 640, 480, 30);
		MjpegServer cvStream = new MjpegServer("CV Image Stream", 1186);
		cvStream.setSource(imageSource);
		
		// Infinitely process image
		while (true) {
			// Grab a frame. If it has a frame time of 0, there was an error.
			// Just skip and continue
			long frameTime = cvsink.grabFrame(inputImage);
			if (frameTime == 0) continue;
			
			// Below is where you would do your OpenCV operations on the provided image
			// The sample below just changes color source to HSV
			Imgproc.cvtColor(inputImage, hsv, Imgproc.COLOR_BGR2HSV);
			
			// Here is where you would write a processed image that you want to restream.
			// This will most likely be a marked up image of what the camera sees
			// For now, we are just going to stream the HSV image
		    System.out.println("imgLock process");
			networkTable.putValue("myContoursReport", imgLock);
			pipeline.filterContoursOutput();
			imageSource.putFrame(hsv);
		}
	}
	
	@SuppressWarnings("unused")
	private static HttpCamera setHttpCamera(String cameraName, MjpegServer server) {
		// Start by grabbing the camera from NetworkTables
		NetworkTable publishingTable = NetworkTable.getTable("CameraPublisher");
		// Wait for robot to connect. Allow this to be attempted indefinitely
		while (true) {
			try {
				if (publishingTable.getSubTables().size() > 0) {
					break;
				}
				Thread.sleep(500);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		HttpCamera camera = null;
		if (!publishingTable.containsSubTable(cameraName)) {
			return null;
		}
		ITable cameraTable = publishingTable.getSubTable(cameraName);
		String[] urls = cameraTable.getStringArray("streams", null);
		if (urls == null) {
			return null;
		}
		ArrayList<String> fixedUrls = new ArrayList<String>();
		for (String url : urls) {
			if (url.startsWith("mjpg")) {
				fixedUrls.add(url.split(":", 2)[1]);
			}
		}
		camera = new HttpCamera("CoprocessorCamera", fixedUrls.toArray(new String[0]));
		server.setSource(camera);
		return camera;
	}
	
	
	@SuppressWarnings("unused")
	private static UsbCamera setUsbCamera(int cameraId, MjpegServer server) {
		// This gets the image from a USB camera
		// Usually this will be on device 0, but there are other overloads
		// that can be used
		UsbCamera camera = new UsbCamera("CoprocessorCamera", cameraId);
		server.setSource(camera);
		return camera;
	}*/
	
}
