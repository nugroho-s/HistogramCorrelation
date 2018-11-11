package com.nugsky.tugasakhir.utils;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;

public class Utils {
    private static BackgroundSubtractorMOG2 mog2;

    public static Image mat2Image(Mat mat){
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public static void backgroundSubtractor(VideoCapture capture){
        Mat camImage = new Mat();
        BackgroundSubtractorMOG2 backgroundSubtractorMOG= Video.createBackgroundSubtractorMOG2();
        if (capture.isOpened()) {
            while (true) {
                capture.read(camImage);

                Mat fgMask=new Mat();
                backgroundSubtractorMOG.apply(camImage, fgMask,0.1);

                Mat output=new Mat();
                camImage.copyTo(output,fgMask);

                //displayImageOnScreen(output);
            }
        }
    }
}
