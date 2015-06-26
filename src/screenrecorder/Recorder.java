/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenrecorder;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.MediaLocator;

/**
 *
 * @author Anthony Canicatti <a.canicatti@gmail.com>
 */
public class Recorder {
    
    static String tempDir = null;
    static boolean record = false;
    
    static final int WIDTH = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    static final int HEIGHT = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    
    /**
     * Create a temporary directory to store image files to convert to mov
     */
    private static void createTempDir(){
        try {
            File temp = Files.createTempDirectory("tempimgs").toFile();
            tempDir = temp.getAbsolutePath();
            System.out.println("Temp directory: "+tempDir);
        } catch (IOException ex) {
            Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Begin recording process
     * Use Robot to capture screenshots and save them to files
     */
    public static void startRecording(){
        createTempDir();
        Thread recordingThread = new Thread(new Runnable(){
           @Override
           public void run(){
               Robot robot;
               int count = 0;
               try {
                   robot = new Robot();
                   while(count == 0 || record){
                       BufferedImage img = robot.createScreenCapture(new Rectangle(WIDTH, HEIGHT));
                       File imgFile = new File(tempDir+"\\"+System.currentTimeMillis()+".jpeg");
                       ImageIO.write(img, "jpeg", imgFile);
                       if(count == 0){
                           record = true;
                           count = 1;
                       }
                       Thread.sleep(10);
                   }
               } catch (AWTException | InterruptedException | IOException ex) {
                   Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        });
        recordingThread.start();
    }
    
    /**
     * Stop recording by setting record boolean to false and begin creating mov file
     * JpegImagesToMovie stitches all image files to mov
     * 
     * @param outFile the path to the output file
     * @return success code - 0 for complete success, 1 if createMediaLocator returned null, 2 for exception
     */
    public static int stopAndCreate(String outFile){
        record = false;
        int retVal = 0;
        try {
            JpegImagesToMovie imgsToMovie = new JpegImagesToMovie();
            // I know Vector sucks but JpegImagesToMovie takes it as parameter
            Vector<String> imgList = new Vector<>();
            File[] list = (new File(tempDir)).listFiles();
            for(File f : list){
                imgList.add(f.getAbsolutePath());
            }
            MediaLocator oml;
            if((oml = JpegImagesToMovie.createMediaLocator(outFile)) == null){
                retVal = 1;
            }
            imgsToMovie.doIt(WIDTH, HEIGHT, 3, imgList, oml);
        } catch(MalformedURLException e){
            retVal = 2;
        } finally {
            File tmp = new File(tempDir);
            for(File f : tmp.listFiles())
                f.deleteOnExit();
            tmp.deleteOnExit();
        }
        return retVal;
    }
}
