package proj_vision_testing;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.networktables.*;
import edu.wpi.first.wpilibj.tables.*;
import edu.wpi.first.wpilibj.vision.VisionThread;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.*;

import javax.swing.JFrame;

import org.opencv.core.Rect;
//import com.ctre.CANTalon;
import org.opencv.core.Size;

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

		// Connect NetworkTables, and get access to the publishing table
		NetworkTable.setClientMode();
		// Team number here
		NetworkTable.setTeam(1218);
		NetworkTable.initialize();
		NetworkTable table;
		table = NetworkTable.getTable("fps");

		GripPipeline pipeline = new GripPipeline();
		
		
		// IP Camera
		String cameraName   = "vision_camera0";
		String cameraIP     = "http://10.12.18.10/mjpg/video.mjpg";
		String newCameraIP     = "http://10.12.18.11/mjpg/video.mjpg";

		// MAC OS X: use VideoCapture(1);
		VideoCapture camera0 = new VideoCapture(0);
		VideoCapture cap = new VideoCapture();
		VideoCapture camera;
		int cameraInput;
		//int CAM_TO_USE = 0;
		int CAM_TO_USE = 10;
		int BACKUP_CAM = 1;
		
		Mat matFrame = new Mat();
		
		try{
			cap.open(newCameraIP);
			System.out.println("Camera opened from " + newCameraIP);
		} catch (Exception e3) {
			System.out.println("No IPCamera found");
		}
		
		
		try {
			cap.open(CAM_TO_USE);
			System.out.println("Camera opened from port " + CAM_TO_USE);
		} catch (Exception e1) {
			System.out.println("No webcam in port 0 found");

			try{
				cap.open(BACKUP_CAM);
				System.out.println("Camera opened from port " + BACKUP_CAM);
			} catch (Exception e2) {
				System.out.println("No webcam in port 1 found");
				
				try{
					cap.open(newCameraIP);
					System.out.println("Camera opened from " + newCameraIP);
				} catch (Exception e3) {
					System.out.println("No IPCamera found");
				}
			}
		}
		
		// if the camera is opened
	    if (cap.isOpened()) {
	        boolean keepProcessing = true;
	        while (keepProcessing) {
	            // grab the next frame from video source
	            cap.grab();
	            
	            // decode and return the grabbed video frame
	            cap.retrieve(matFrame);
	            
	            // get the frames per seconds of the camera
	            int CV_CAP_PROP_FPS = 5;
				double fps = cap.get(CV_CAP_PROP_FPS);
				//System.out.println("Frames per second: " + fps);
				table.putNumber("fps", fps);
	            
	            // if the frame is valid (not end of video for example)
	            if (!(matFrame.empty())) {
	                // *** to any processing here*** 
	            	pipeline.process(matFrame);
	            	if (!pipeline.filterContoursOutput().isEmpty()) {
	            		System.out.println("filterContours is not empty");
	            		Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
	            		table.putNumber("x", r.x);
	            		table.putNumber("y", r.y);
	            		synchronized (imgLock) {
	            			centerX = r.x + (r.width / 2);
	            		}
	            		System.out.println("Rect r: " + r);
	            		System.out.println("r.x: " + r.x);
	            		System.out.println("r.y: " + r.y);
	            	} else {
	            		//System.out.println("filterContours is empty");
	            	}
	            	
	            	while (!cap.isOpened()) {
	            		if (!pipeline.filterContoursOutput().isEmpty()) {
		            		Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
		            		networkTable.putNumber("x", r.x);
		            		networkTable.putNumber("y", r.y);
		            		synchronized (imgLock) {
		            			centerX = r.x + (r.width / 2);
		            		}
		            		System.out.println("Rect r: " + r);
		            		System.out.println("r.x: " + r.x);
		            		System.out.println("r.y: " + r.y);
		            	}
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
	        System.out.println("error cannot open any capture source - exiting");
	    }


		
		/*if((camera = new VideoCapture(1)).grab()) {
			//VideoCapture camera = new VideoCapture(1);
			System.out.println("image feed captured from port 1");
			try {
				System.out.println("sleeping");
				Thread.sleep(2000);
				System.out.println("awaking");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if ((camera = new VideoCapture(0)).grab()) {
			camera.release();
			System.out.println("image feed captured from port 0");

			try {
				System.out.println("sleeping");
				Thread.sleep(2000);
				System.out.println("awaking");

				if (camera0.isOpened()) {
					System.out.println("camera0 is opened");
					Mat image = null;

					if (camera0.grab()) {
						System.out.println("camera0 has been grabbed");

						int CV_CAP_PROP_FPS = 5;
						double fps = camera0.get(CV_CAP_PROP_FPS); //get the frames per seconds of the camera
						System.out.println("Frames per second: " + fps);

						Mat frame0;
						boolean success = camera0.read(frame0); // read a new frame from camera

						if (!success) { //if not success, break loop
							System.out.println("Cannot read frame from camera");
							//break;
						}
						
						
						if(waitKey(30) == 27) { //wait for 'esc' key press for 30 ms. If 'esc' key is pressed, break loop
							System.out.println("esc key is pressed by user"); 
							break;
						}
						
						
					}
					if (camera0.retrieve(image)) {
						System.out.println("camera0 has been retrieved");
					}
				}
				Thread.sleep(5000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Fatal Error: error in retrieving USB camera!");
			camera.release();
		}

		//VideoCapture camera = new VideoCapture(cameraIP, Videoio.CAP_FFMPEG);


		final Size frameSize = new Size((int)camera.get
				(Videoio.CAP_PROP_FRAME_WIDTH),
				(int)camera.get(Videoio.CAP_PROP_FRAME_HEIGHT));
		System.out.println("frameSize: " + frameSize);


		
	    final FourCC fourCC=new FourCC("XVID");
	    VideoWriter videoWriter=new VideoWriter(outputFile,fourCC.toInt(),camera.get(Videoio.CAP_PROP_FPS),frameSize,true);
	    final Mat mat=new Mat();
	    int frames=0;
	    final long startTime=System.currentTimeMillis();
	    while (camera.read(mat)) {
	      videoWriter.write(mat);
	      frames++;
	    }
	    final long estimatedTime=System.currentTimeMillis() - startTime;
		 

		//CvSource outputStream = CameraServer.getInstance().putVideo("unprocessed", 640, 480);
		//CvSink cvsink = new CvSink("CV Image Grabber");
		//cvsink.setSource(camera0);


		// MARK Camera injection into GripPipeline
		Mat frame  = new Mat();
		Mat output = new Mat();
		Mat source = new Mat();
		//Mat frame0 = new Mat();


		*//**
	    while(!camera.isOpened()) {
	    	System.out.println("while(!camera.isOpened())");
	    	camera.retrieve(frame);
	    	Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY); // this might need to go...
	    	System.out.println("visonThread ran");
	    	if (!pipeline.filterContoursOutput().isEmpty()) {
	            Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
	            networkTable.putNumber("x", r.x);
	            networkTable.putNumber("y", r.y);
	            synchronized (imgLock) {
	                centerX = r.x + (r.width / 2);
	            }
	            System.out.println("Rect r: " + r);
	            System.out.println("r.x: " + r.x);
	            System.out.println("r.y: " + r.y);
	    	}
            //outputStream.putFrame(output);
	    }
		 **//*


		*//**
		 * Camera to Mat --> grab each frame
		 * Change - add a flag to check to see if the first frame has been read
		 * Change - declare previous frame here
		 *//*
		boolean debug = true;

		boolean keepProcessing = true;
		boolean firstFrame = false;
		Mat previousFrame;
		
		//Mat matFrame = null;
		//Mat matFrame = new Mat();
		
		if (firstFrame) {
			System.out.println("if (firstFrame)");
		}
		
		while (keepProcessing) {
			if (debug == true) { System.out.println("while (keepProcessing)"); }
			*//**
			 * Change - Save previous frame before getting next one
			 * Only do this if the first frame has passed! 
			 *//*

			*//** grab the next frame and decode it *//*
			//camera.read(matFrame);
			camera0.read(matFrame);
			if (matFrame == null) {
				System.out.println("matFrame: null");
			} else if (matFrame.empty()) {
				System.out.println("matFrame: empty");
			} else {
				System.out.println("matFrame: " + matFrame);
			}
			
			if (camera.grab()) {
				camera.retrieve(matFrame);
				if (debug == true) { System.out.println("matFrame: " + matFrame); }
			}

			if (!firstFrame) {
				previousFrame = matFrame.clone();
				if (debug == true) { 
					System.out.println("if (!firstFrame)");
					if (matFrame == null) {
						System.out.println("matFrame: null");
					} else {
						System.out.println("matFrame: " + matFrame);
					}
					System.out.println("previousFrame: " + previousFrame);
				}
			}

			*//** if the frame is not empty *//*
			if (!(matFrame.empty())) {
				if (debug == true) { System.out.println("if (!(matFrame.empty()))"); }
				*//** 
				 * Change - If we are on the first frame, 
				 * only show that and set the flag to false
				 *//*
				if (firstFrame) {
					//ims.showImage(matFrame);
					firstFrame = false;
				} else {
					*//** Change - now show absolute difference after first frame *//*
					//Core.absdiff(matFrame, previousFrame, diffFrame);
					//ims.showImage(diffFrame);                 
				}

				*//** display image with a delay of 40ms (i.e. 1000 ms / 25 = 25 fps) *//*
				//Thread.sleep(40);
			} else { 
				keepProcessing = false;
			}
			Mat m = ...;  // assuming it's of CV_8U type
        		byte buff[] = new byte[m.total() * m.channels()];
        		m.get(0, 0, buff);
        		// working with buff
        		// ...
        		m.put(0, 0, buff);
			 	
			*//***************************************************************//*

			if(camera.isOpened()) {
				System.out.println("camera.isOpened() & pipeline.process");
				pipeline.process(frame);
				//pipeline.process(output);
				//pipeline.process(source);
			}

			while(camera.isOpened()) {
				//System.out.println("while(!camera.isOpened())");
				camera.retrieve(frame);
				//Imgproc.cvtColor(source, output, Imgproc.COLOR_BGR2GRAY); // this might need to go...
				//System.out.println("visonThread ran");
				if (!pipeline.filterContoursOutput().isEmpty()) {
					System.out.println("pipeline.filterCountoursOutput is not empty");

					Rect r = Imgproc.boundingRect(pipeline.filterContoursOutput().get(0));
					networkTable.putNumber("x", r.x);
					networkTable.putNumber("y", r.y);
					synchronized (imgLock) {
						centerX = r.x + (r.width / 2);
					}

					System.out.println("Rect r: " + r);
					System.out.println("r.x: " + r.x);
					System.out.println("r.y: " + r.y);

				}
				//outputStream.putFrame(output);
			}

			System.out.println("Frame Obtained");
			System.out.println("Captured Frame Width " + frame.width());
			System.out.println("source: " + source);*/

			/*
	 // All Mats and Lists should be stored outside the loop 
	 // to avoid allocations, as they are expensive to create
	 	Mat inputImage = new Mat();
	 	Mat hsv = new Mat();

	    visionThread = new VisionThread(camera, pipeline, p -> {
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
			 */





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
	}
//}
