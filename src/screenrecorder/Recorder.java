/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package screenrecorder;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
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
import javax.swing.JFrame;

/**
 *
 * @author Anthony Canicatti <a.canicatti@gmail.com>
 */
public class Recorder {
    
    static String tempDir = null;
    static boolean record = false;
    static int width, height;
    
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
        Recorder.width = WIDTH;
        Recorder.height = HEIGHT;
        createTempDir();
        Thread recordingThread = new Thread(new Runnable(){
           @Override
           public void run(){
               Robot robot;
               int count = 0;
               try {
                   robot = new Robot();
                   while(count == 0 || record){
                       BufferedImage img = robot.createScreenCapture(new Rectangle(width, height));
                       Image cursor = ImageIO.read(new File("res\\cursor.png"));
                       int x = MouseInfo.getPointerInfo().getLocation().x;
                       int y = MouseInfo.getPointerInfo().getLocation().y;
                       Graphics2D g2D = img.createGraphics();
                       g2D.drawImage(cursor, x, y, 17, 23, null);
                       File imgFile = new File(tempDir+"\\"+System.currentTimeMillis()+".jpeg");
                       ImageIO.write(img, "jpeg", imgFile);
                       if(count == 0){
                           record = true;
                           count = 1;
                       }
                   }
               } catch (AWTException | IOException ex) {
                   Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        });
        recordingThread.start();
    }
    
    public static void startRecording(JFrame frame){
        Recorder.width = frame.getWidth();
        Recorder.height = frame.getHeight();
        createTempDir();
        Thread recordingThread = new Thread(new Runnable(){
           @Override
           public void run(){
               Robot robot;
               int count = 0;
               try {
                   robot = new Robot();
                   while(count == 0 || record){
                       Rectangle r = new Rectangle(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight());
                       BufferedImage img = robot.createScreenCapture(r);
                       Image cursor = ImageIO.read(new File("res\\cursor.png"));
                       int x = MouseInfo.getPointerInfo().getLocation().x;
                       int y = MouseInfo.getPointerInfo().getLocation().y;
                       Graphics2D g2D = img.createGraphics();
                       g2D.drawImage(cursor, x, y, 17, 23, null);
                       File imgFile = new File(tempDir+"\\"+System.currentTimeMillis()+".jpeg");
                       ImageIO.write(img, "jpeg", imgFile);
                       if(count == 0){
                           record = true;
                           count = 1;
                       }
                   }
               } catch (AWTException | IOException ex) {
                   Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, ex);
               }
           }
        });
        recordingThread.start();
    }
    
    /**
     * Stop recording 
     */
    public static void stopRecording(){
        record = false;
    }
    
    /**
     * Stop recording by setting record boolean to false and begin creating mov file
     * JpegImagesToMovie stitches all image files to mov
     * 
     * @param outFile the path to the output file
     * @return success code - 0 for complete success, 1 if createMediaLocator returned null, 2 for exception
     */
    public static int createFile(String outFile){
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
            imgsToMovie.doIt(width, height, 10, imgList, oml);
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
    
    /**
     * Take a screenshot
     * 
     * @param filePath the file path to save the screenshot
     */
    public static void takeScreenshot(String filePath){
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Robot robot = new Robot();
                BufferedImage img = robot.createScreenCapture(new Rectangle(WIDTH, HEIGHT));
                ImageIO.write(img, "png", new File(filePath));
            } catch(AWTException | IOException | InterruptedException e){
                Logger.getLogger(Recorder.class.getName()).log(Level.SEVERE, null, e);
            }
        }).start();
    }
}
