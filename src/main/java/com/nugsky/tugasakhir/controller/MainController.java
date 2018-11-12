package com.nugsky.tugasakhir.controller;

import com.nugsky.tugasakhir.models.ForegroundObject;
import com.nugsky.tugasakhir.models.PixelLine;
import com.nugsky.tugasakhir.models.TemporalPixelBelt;
import com.nugsky.tugasakhir.models.TemporalPixelBeltGroup;
import com.nugsky.tugasakhir.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.*;

public class MainController {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_ffmpeg331_64");
    }

    final static Logger logger = Logger.getLogger(MainController.class);

    public static VideoCapture sourceCapture;
    public static VideoCapture maskCapture;

    private static List<TemporalPixelBeltGroup> temporalPixelBeltGroupList = new ArrayList<>();
    private static List<ForegroundObject> foregroundObjects = new ArrayList<>();
    private static String fileName = "";
    private static List<TemporalPixelBelt> temporalPixelBelts = new ArrayList<>();

    public ImageView videoThumbnail;
    public ImageView pixelBeltViewer;
    public Button prosesBtn;
    public TextField fileTextField;
    public Slider frameNoSlider;
    public TextField frameNoTextField;

    public void chooseFile(ActionEvent event){
        logger.debug("click");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Video");
        File file = fileChooser.showOpenDialog(null);
        try{
            fileName = file.getAbsolutePath();
            fileTextField.setText(fileName);
            sourceCapture = new VideoCapture(file.getAbsolutePath());
            Platform.runLater(() -> {
                Thread t = new Thread(new UpdateImageRunnable(sourceCapture));
                t.start();
            });
        }catch (RuntimeException e) {

        }
    }

    public void proses(ActionEvent event) {
        logger.debug("proses");
        VideoCapture videoCap = new VideoCapture(fileName);
        VideoCapture maskCap = new VideoCapture("data/temp.mp4");
        Platform.runLater(() -> {
            Thread t = new Thread(new DefinePixelBelts(videoCap,maskCap));
            logger.debug("applying pixel belts");
            t.start();
        });
    }

    class DefinePixelBelts implements Runnable{
        VideoCapture videoCap, maskCap;

        public DefinePixelBelts(VideoCapture videoCap, VideoCapture maskCap) {
            this.videoCap = videoCap;
            this.maskCap = maskCap;
        }

        @Override
        public void run() {
            Mat frame = new Mat();
            Mat frameFg = new Mat();
            Mat maskFg = new Mat();
            Random rand = new Random();
            int frameNo=0;
            int frameMax = (int) videoCap.get(Videoio.CAP_PROP_FRAME_COUNT);
            int percent = 0;
            while(videoCap.read(frame)){
                if(100*((double)frameNo/(double)frameMax)>percent){
                    percent = (int) (100*((double)frameNo/(double)frameMax));
                    logger.debug(percent+"%");
                }
                maskCap.read(frameFg);
                Imgproc.cvtColor(frameFg,maskFg,Imgproc.COLOR_RGB2GRAY);
                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(maskFg,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
                logger.debug(contours.size());
                if(contours.isEmpty()) {
                    frameNo++;
                    continue;
                }
                Collections.sort(contours,(Comparator.comparingInt(o -> (int) Imgproc.contourArea(o))));
                int beltCount = 0,i=0;

                TemporalPixelBelt pixelBelt = new TemporalPixelBelt(frameNo);
                //horizontal pixel belts
                while (beltCount<4){
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    PixelLine line = new PixelLine(true,rand.nextInt(rect.height)+rect.y);
                    pixelBelt.addLines(line);
                    if(++i>=contours.size()) i=0;
                    beltCount++;
                    if(frameNo==30){
                        Imgproc.rectangle(maskFg,new Point(rect.x,rect.y),new Point(rect.x+rect.width,rect.y+rect.height),
                                new Scalar(255,255,255));
                        Imgproc.line(maskFg,new Point(0,line.location),new Point(maskFg.width(),line.location),new Scalar(255,255,255));
                        Utils.displayImage(maskFg);
                    }
                }
                if(frameNo==30) return;
                //vertical pixel belts
                beltCount=0;
                while (beltCount<4){
                    Rect rect = Imgproc.boundingRect(contours.get(i));
                    PixelLine line = new PixelLine(false,rand.nextInt(rect.width)+rect.x);
                    pixelBelt.addLines(line);
                    if(++i>=contours.size()) i=0;
                    beltCount++;
                }
                temporalPixelBelts.add(pixelBelt);
                frameNo++;
            }
            logger.debug("pixel belts done");

            int i = 30;
            maskCap.set(Videoio.CAP_PROP_POS_FRAMES,i);
            Mat mask = new Mat();
            maskCap.read(mask);
            TemporalPixelBelt tpb = temporalPixelBelts.get(i);
            for(PixelLine pl:tpb.pixelLines){
                if(pl.isHorizontal){
                    Imgproc.line(mask,new Point(0,pl.location),new Point(mask.width(),pl.location),new Scalar(255,255,255));
                    logger.debug(String.format("line %d",pl.location));
                }
            }
            pixelBeltViewer.setImage(Utils.mat2Image(mask));
        }
    }

    class UpdateImageRunnable implements Runnable{
        VideoCapture capture;

        public UpdateImageRunnable(VideoCapture capture) {
            this.capture = capture;
        }

        @Override
        public void run() {
            Utils.backgroundSubtractor(capture);
            logger.debug("done subtract");
            maskCapture = new VideoCapture(Utils.MASK_FILE);
            sourceCapture.set(Videoio.CAP_PROP_POS_FRAMES,0);
            Mat src = new Mat();
            Mat mask = new Mat();
            sourceCapture.read(src);
            maskCapture.read(mask);
            videoThumbnail.setImage(Utils.mat2Image(src));
            pixelBeltViewer.setImage(Utils.mat2Image(mask));
            prosesBtn.setDisable(false);
            logger.debug(sourceCapture.get(Videoio.CAP_PROP_FRAME_COUNT));
            frameNoSlider.setMax(sourceCapture.get(Videoio.CAP_PROP_FRAME_COUNT));
            frameNoSlider.setShowTickMarks(true);
            frameNoSlider.setBlockIncrement(10);
            frameNoSlider.setDisable(false);
            frameNoSlider.setOnMouseReleased((MouseEvent event) -> {
                int frameNo = (int) frameNoSlider.getValue();
                frameNoTextField.setText(""+frameNo);
                sourceCapture.set(Videoio.CAP_PROP_POS_FRAMES,frameNo);
                maskCapture.set(Videoio.CAP_PROP_POS_FRAMES,frameNo);
                sourceCapture.read(src);
                maskCapture.read(mask);
                videoThumbnail.setImage(Utils.mat2Image(src));
                pixelBeltViewer.setImage(Utils.mat2Image(mask));
            });
        }
    }
}
