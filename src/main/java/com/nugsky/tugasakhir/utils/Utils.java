package com.nugsky.tugasakhir.utils;

import com.nugsky.tugasakhir.CobaChart;
import com.nugsky.tugasakhir.models.PixelLine;
import com.nugsky.tugasakhir.models.TemporalPixelBelt;
import com.nugsky.tugasakhir.ui.CorrelationDisplay;
import javafx.scene.image.Image;
import org.apache.log4j.Logger;
import org.jfree.ui.RefineryUtilities;
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
import java.util.*;
import java.util.List;

public class Utils {
    public static String MASK_FILE = "data/temp.avi";

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

        VideoWriter writer = new VideoWriter(MASK_FILE, VideoWriter.fourcc('X','V','I','D'),
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

//            List<MatOfPoint> contours = new ArrayList<>();
//            Imgproc.findContours(fgMask,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
//            Mat contourImg =  new Mat(fgMask.rows(), fgMask.cols(), CvType.CV_8U, Scalar.all(0));
//            for(int i=0;i<contours.size();i++){
//                if(Imgproc.contourArea(contours.get(i))>threshold){
//                    Rect rect = Imgproc.boundingRect(contours.get(i));
//                    logger.debug(frameCount+":"+rect.height+","+rect.width);
//                    if(rect.height>recHeightThreshold && rect.width>recWidthThreshold){
//                        Imgproc.drawContours(contourImg,contours,i,new Scalar(255,255,255));
//                    }
//                }
//            }

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
            writer.write(fgMask);

        }
        writer.release();
    }

    public static boolean isValidPixelBelt(TemporalPixelBelt tpb, List<MatOfPoint> contours){
        int countHIntersect=0,countVIntersect=0;
        for(PixelLine pl:tpb.pixelLines){
            for(MatOfPoint contour:contours){
                Rect rect = Imgproc.boundingRect(contour);
                int lowerBound = (pl.isHorizontal)?rect.y:rect.x;
                int upperBound = (pl.isHorizontal)?rect.y+rect.height:rect.x+rect.width;
                if(pl.location>=lowerBound && pl.location<=upperBound){
                    if(pl.isHorizontal) countHIntersect++;
                    else countVIntersect++;
                    continue;
                }
            }
        }
        return (countHIntersect>=1 && countVIntersect>=1);
    }

    public static List<MatOfPoint> filterContours(List<MatOfPoint> contours,int tresH, int tresW){
        Collections.sort(contours, Comparator.comparingInt(o-> (int)Imgproc.contourArea(o)));
        Collections.reverse(contours);
        contours = contours.subList(0,4);
        for(int i=3;i>=0;i--){
            Rect rect = Imgproc.boundingRect(contours.get(i));
            if(rect.height<tresH||rect.width<tresW)
                contours.remove(i);
        }
        return contours;
    }

    public static List<Double> calculateCorrelation(Map<Integer,TemporalPixelBelt> beltMap,VideoCapture vc1,VideoCapture vc2){
        MatOfInt channel = new MatOfInt(0,1,2);
        MatOfInt size = new MatOfInt(256,256,256);
        MatOfFloat range = new MatOfFloat(0.0f,255.0f, 0.0f, 255.0f, 0.0f, 255.0f);
        int i=0,frameMax = (int) vc1.get(Videoio.CAP_PROP_FRAME_COUNT);
        Mat frame1 = new Mat(),frame2=new Mat();
        Mat mask = new Mat((int)vc1.get(Videoio.CV_CAP_PROP_FRAME_HEIGHT),(int)vc1.get(Videoio.CV_CAP_PROP_FRAME_WIDTH),CvType.CV_8UC1);
        TemporalPixelBelt tpb = beltMap.get(0);
        Mat hist1 = new Mat(),hist2 = new Mat();
        List<Double> result = new ArrayList<>();
        while(vc2.read(frame2)){
            vc1.read(frame1);
            logger.debug(i);
            if(i>tpb.frameEnd){
                tpb = beltMap.get(i);
            }
            mask.setTo(new Scalar(0,0,0));
            if(tpb == null)
                continue;
            for(PixelLine pl:tpb.pixelLines){
                Mat m = (pl.isHorizontal)?mask.row(pl.location):mask.col(pl.location);
                m.setTo(new Scalar(255,255,255));
            }
            List<Mat> images = new ArrayList<>();
            Core.split(frame1,images);
            if(images.isEmpty()) continue;
            Imgproc.calcHist(images, channel,mask,hist1,size,range);
            images.clear();
            Core.split(frame2,images);
            Imgproc.calcHist(images, channel,mask,hist2,size,range);
            double corr = Imgproc.compareHist(hist1,hist2,Imgproc.CV_COMP_CORREL);
            images.clear();
            logger.debug(i+"/"+frameMax+" "+corr);
            result.add(corr);
            frame1.release(); frame2.release();
            i++;
        }
        logger.debug("done corr");
        logger.debug(result);
        CorrelationDisplay chart = new CorrelationDisplay(
                "School Vs Years", result );

        chart.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
        return result;
    }
}
