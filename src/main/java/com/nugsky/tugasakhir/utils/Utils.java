package com.nugsky.tugasakhir.utils;

import javafx.scene.image.Image;
import org.apache.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String MASK_FILE = "data/temp.mp4";

    private static Logger logger = Logger.getLogger(Utils.class);

    public static Image mat2Image(Mat mat){
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public static void displayImage(Mat m)
    {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage img2 = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) img2.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        //BufferedImage img=ImageIO.read(new File("/HelloOpenCV/lena.png"));
        ImageIcon icon=new ImageIcon(img2);
        JFrame frame=new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img2.getWidth(null)+50, img2.getHeight(null)+50);
        JLabel lbl=new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void backgroundSubtractor(VideoCapture capture){
//        Mat camImage = new Mat();
//        BackgroundSubtractorMOG2 backgroundSubtractorMOG= Video.createBackgroundSubtractorMOG2();
//        if (capture.isOpened()) {
//            while (true) {
//                capture.read(camImage);
//
//                Mat fgMask=new Mat();
//                backgroundSubtractorMOG.apply(camImage, fgMask,0.1);
//
//                Mat output=new Mat();
//                camImage.copyTo(output,fgMask);
//
//                //displayImageOnScreen(output);
//            }
//        }

        VideoWriter writer = new VideoWriter(MASK_FILE, VideoWriter.fourcc('M','J','P','G'),
                capture.get(Videoio.CV_CAP_PROP_FPS), new Size(capture.get(Videoio.CV_CAP_PROP_FRAME_WIDTH), capture.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT)),
                false);
        Mat camImage = new Mat();
        BackgroundSubtractorMOG2 backgroundSubtractorMOG= Video.createBackgroundSubtractorMOG2();
        int frameCount = 0;

        boolean firstRun=true;
        float threshold = 0;
        float recHeightThreshold = 0;
        float recWidthThreshold = 0;
        while (capture.read(camImage)) {
            if (firstRun){
                firstRun = false;
                threshold = camImage.rows()*camImage.cols()/5000;
                recHeightThreshold = camImage.height()/50;
                recWidthThreshold = camImage.width()/50;
                logger.debug(String.format("threshold:%f;%f;%f",threshold,recHeightThreshold,recWidthThreshold));
            }
            frameCount+=1;
            Mat fgMask=new Mat();
            backgroundSubtractorMOG.apply(camImage, fgMask,0.1);

            Mat output=new Mat();
            camImage.copyTo(output,fgMask);

            //                Photo.fastNlMeansDenoising(fgMask,denoise,1.0f,7,21);

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(fgMask,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            Mat contourImg =  new Mat(fgMask.rows(), fgMask.cols(), CvType.CV_8U, Scalar.all(0));
            for(int i=0;i<contours.size();i++){
                if(Imgproc.contourArea(contours.get(i))>threshold){
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    logger.debug(frameCount+":"+rect.height+","+rect.width);
                    if(rect.height>recHeightThreshold && rect.width>recWidthThreshold){
                        Imgproc.drawContours(contourImg,contours,i,new Scalar(255,255,255));
                    }
                }
            }

//                for(int y=0;y<fgMask.rows();y++){
//                    for(int x=0;x<fgMask.cols();x++){
//                        color = fgMask.get(y,x);
//                        if(color[0]>=100){
//                            fgMask.copyTo(copy);
//                            if(Imgproc.floodFill(copy,new Mat(),new Point(x,y),new Scalar(0,0,0))<threshold){
//                                copy.copyTo(fgMask);
//                            }
//                        }
//                    }
//                    if((y>(percentT*(percent+1)))){
//                        percent+=1;
//                        logger.debug("proses "+percent+"%");
//                    }
//                }

            //displayImageOnScreen(output);
            writer.write(contourImg);
        }
        writer.release();
    }
}
