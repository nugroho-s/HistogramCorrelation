package com.nugsky.tugasakhir.controller;

import com.nugsky.tugasakhir.utils.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_ffmpeg331_64");
    }

    final static Logger logger = Logger.getLogger(MainController.class);

    public static VideoCapture videoCapture;
    public ImageView videoThumbnail;
    public ImageView pixelBeltViewer;

    public void chooseFile(ActionEvent event){
        logger.debug("click");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Video");
        File file = fileChooser.showOpenDialog(null);
        try{
            videoCapture = new VideoCapture(file.getAbsolutePath());
            Platform.runLater(() -> {
                Thread t = new Thread(new UpdateImageRunnable(videoCapture));
                t.start();
            });
        }catch (RuntimeException e) {

        }
    }

    class UpdateImageRunnable implements Runnable{
        VideoCapture capture;

        public UpdateImageRunnable(VideoCapture capture) {
            this.capture = capture;
        }

        @Override
        public void run() {
//            VideoCapture capture = new VideoCapture("rtsp://192.168.0.100:8080/h264_ulaw.sdp");
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

                double[] color;
                Mat copy = new Mat();
                int percentT = fgMask.rows()/100;
                int percent = 0;

                Mat denoise = new Mat();
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
                videoThumbnail.setImage(Utils.mat2Image(camImage));
                pixelBeltViewer.setImage(Utils.mat2Image(contourImg));
            }

//            Mat frame = new Mat();
//            Mat gray = new Mat();
//            Mat bw = new Mat();
//            Mat mask = new Mat();
//            Image image;
//            int frameCounter = 0;
//            while(videoCapture.read(frame)){
//                Mat filtered = new Mat();
//
//                ++frameCounter;
//                if(frameCounter == 350){
//                    videoThumbnail.setImage(Utils.mat2Image(frame));
//                    logger.debug("image set");
//                }
//            }
        }
    }
}
