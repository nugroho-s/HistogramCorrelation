package com.nugsky.tugasakhir.controller;

import com.nugsky.tugasakhir.models.ForegroundObject;
import com.nugsky.tugasakhir.models.PixelLine;
import com.nugsky.tugasakhir.models.TemporalPixelBelt;
import com.nugsky.tugasakhir.models.TemporalPixelBeltGroup;
import com.nugsky.tugasakhir.utils.Utils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_ffmpeg331_64");
    }

    final static Logger logger = Logger.getLogger(MainController.class);

    public static VideoCapture sourceCapture;
    public static VideoCapture beltCapture;

    private static List<TemporalPixelBeltGroup> temporalPixelBeltGroupList = new ArrayList<>();
    private static List<ForegroundObject> foregroundObjects = new ArrayList<>();
    public static String fileName = "";
    private static Map<Integer, TemporalPixelBelt> temporalPixelBeltMap = new HashMap<>();
    private static List<TemporalPixelBelt> temporalPixelBelts = new ArrayList<>();
    public Label resultLabel;
    private int thresW=50,thresH=50;

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
        temporalPixelBeltMap.clear();
        VideoCapture videoCap = new VideoCapture(fileName);
        VideoCapture maskCap = new VideoCapture(Utils.MASK_FILE);
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
            TemporalPixelBelt pixelBelt = new TemporalPixelBelt();

            //skip frame 1 and place pixel belt
            videoCap.read(new Mat());
            maskCap.read(new Mat());
            frameNo++;
            boolean first=true;
            while(videoCap.read(frame)){
                if(100*((double)frameNo/(double)frameMax)>percent){
                    percent = (int) (100*((double)frameNo/(double)frameMax));
                    logger.debug(percent+"%");
                }
                maskCap.read(frameFg);
                Imgproc.cvtColor(frameFg,maskFg,Imgproc.COLOR_RGB2GRAY);
                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(maskFg,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

                contours = Utils.filterContours(contours,maskFg.height()/thresH,maskFg.width()/thresW,maskFg.height(),maskFg.width());
                if(contours.isEmpty()) {
                    logger.debug("Empty "+frameNo);
                    frameNo++;
                    continue;
                }

                for (MatOfPoint contour:contours){
                    foregroundObjects.add(new ForegroundObject(frameNo,Imgproc.boundingRect(contour)));
                }

                int beltCount = 0,i=0;

//                if (frameNo==30){
//                    for(PixelLine line:pixelBelt.pixelLines){
//                        if(line.isHorizontal)Imgproc.line(frame,new Point(0,line.location),new Point(maskFg.width(),line.location),new Scalar(255,255,255));
//                        else Imgproc.line(frame,new Point(line.location,0),new Point(line.location,maskFg.height()),new Scalar(255,255,255));
//                    }
//                    Utils.displayImage(frame);
//                }

                if(Utils.isValidPixelBelt(pixelBelt,contours)){
                    frameNo++;
                    continue;
                }
                pixelBelt.frameEnd = frameNo-1;

                int totArea=0;
                List<Integer> areaList = contours.stream().map(s->(int)Imgproc.contourArea(s)).collect(Collectors.toList());
                for(MatOfPoint contour:contours){
                    double area = Imgproc.contourArea(contour);
                    logger.debug(area);
                    totArea+=Imgproc.contourArea(contour);
                }
                logger.debug(totArea);
                pixelBelt = new TemporalPixelBelt();
                //horizontal pixel belts
                while (beltCount<4){
                    i=0;
                    int randIn = rand.nextInt(totArea);
                    int accArea=0;
                    while(accArea<=randIn){
                        accArea += areaList.get(i);
                        i++;
                    }
                    Rect rect = Imgproc.boundingRect(contours.get(--i));
                    if(rect.height+rect.y>frame.height()){
                        logger.error("out of frame "+rect.height+","+rect.y);
                    }
                    PixelLine line = new PixelLine(true,rand.nextInt(rect.height)+rect.y);
                    pixelBelt.addLines(line);
                    if(++i>=contours.size()) i=0;
                    beltCount++;
                }
//                if(frameNo==0) return;

                //vertical pixel belts
                beltCount=0;
                while (beltCount<4){
                    i=0;
                    int randIn = rand.nextInt(totArea);
                    int accArea=0;
                    while(accArea<=randIn){
                        accArea += areaList.get(i);
                        i++;
                    }
                    Rect rect = Imgproc.boundingRect(contours.get(--i));
                    if(rect.width+rect.x>frame.width()){
                        logger.error("out of frame "+rect.width+","+rect.x);
                    }
                    PixelLine line = new PixelLine(false,rand.nextInt(rect.width)+rect.x);
                    pixelBelt.addLines(line);
                    if(++i>=contours.size()) i=0;
                    beltCount++;
                }
                pixelBelt.frameStart = (temporalPixelBeltMap.isEmpty())?0:frameNo;
                temporalPixelBeltMap.put(pixelBelt.frameStart,pixelBelt);
                frameNo++;
            }
            pixelBelt.frameEnd = frameMax;
            logger.debug("pixel belts done");
            logger.debug(temporalPixelBeltMap);

            int i = 30;
            maskCap.set(Videoio.CAP_PROP_POS_FRAMES,i);
            Mat mask = new Mat();
            maskCap.read(mask);
            Imgproc.cvtColor(mask,mask,Imgproc.COLOR_RGB2GRAY);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            contours = Utils.filterContours(contours,mask.height()/thresH,maskFg.width()/thresW,maskFg.height(),maskFg.width());

            TemporalPixelBelt tpb = null;

            int pbi = i;
            while(!temporalPixelBeltMap.containsKey(pbi)){
                pbi--;
            }
            tpb=temporalPixelBeltMap.get(pbi);
            logger.debug(tpb);
            logger.debug(Utils.isValidPixelBelt(tpb,contours));

            for(MatOfPoint contour:contours){
                Rect rect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(mask,new Point(rect.x,rect.y),new Point(rect.x+rect.width,rect.y+rect.height),
                        new Scalar(255,255,255),3);
            }

            for(PixelLine pl:tpb.pixelLines){
                if(pl.isHorizontal){
                    Imgproc.line(mask,new Point(0,pl.location),new Point(mask.width(),pl.location),new Scalar(255,255,255));
                } else {
                    Imgproc.line(mask,new Point(pl.location,0),new Point(pl.location,mask.height()),new Scalar(255,255,255));
                }
            }
            pixelBeltViewer.setImage(Utils.mat2Image(mask));
            VideoCapture cap4frame = new VideoCapture(fileName);
            cap4frame.set(Videoio.CAP_PROP_POS_FRAMES,4);
            List<Integer> frames = Utils.calculateCorrelation(temporalPixelBeltMap, new VideoCapture(fileName),cap4frame);
            resultLabel.setFont(new Font("Arial", 30));
            resultLabel.setTextFill(Color.web(frames.isEmpty()?"#00FF00":"#FF0000"));
            resultLabel.setText(frames.isEmpty()?"video asli":"video tampering terdeteksi pada "+frames);
        }
    }

    class UpdateImageRunnable implements Runnable{
        VideoCapture capture;
        Mat src = new Mat();
        Mat beltMat = new Mat();
        Mat mask = new Mat();

        public UpdateImageRunnable(VideoCapture capture) {
            this.capture = capture;
        }

        @Override
        public void run() {
            Utils.backgroundSubtractor(capture);
            logger.debug("done subtract");
            beltCapture = new VideoCapture(Utils.MASK_FILE);
            sourceCapture.set(Videoio.CAP_PROP_POS_FRAMES,0);
            sourceCapture.read(src);
            src.copyTo(beltMat);
            videoThumbnail.setImage(Utils.mat2Image(src));
            pixelBeltViewer.setImage(Utils.mat2Image(beltMat));
            prosesBtn.setDisable(false);
            logger.debug(sourceCapture.get(Videoio.CAP_PROP_FRAME_COUNT));
            frameNoSlider.setMax(sourceCapture.get(Videoio.CAP_PROP_FRAME_COUNT));
            frameNoSlider.setShowTickMarks(true);
            frameNoSlider.setBlockIncrement(10);
            frameNoSlider.setDisable(false);
            frameNoTextField.setDisable(false);
            frameNoSlider.setOnMouseReleased((MouseEvent event) -> {
                int frameNo = (int) frameNoSlider.getValue();
                frameNoTextField.setText(""+frameNo);
                setFrame(frameNo);
            });
            frameNoTextField.setOnAction((event -> {
                setFrame(Integer.parseInt(frameNoTextField.getText()));
            }));
        }

        private void setFrame(int frameNo){
            sourceCapture.set(Videoio.CAP_PROP_POS_FRAMES,frameNo);
            beltCapture.set(Videoio.CAP_PROP_POS_FRAMES,frameNo);
            sourceCapture.read(src);
            src.copyTo(beltMat);
            beltCapture.read(mask);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.cvtColor(mask,mask,Imgproc.COLOR_RGB2GRAY);
            Imgproc.findContours(mask,contours,new Mat(),Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            contours = Utils.filterContours(contours,mask.height()/thresH,mask.width()/thresW,mask.height(),mask.width());
            for(MatOfPoint contour:contours){
                Rect rect = Imgproc.boundingRect(contour);
                Imgproc.rectangle(beltMat,new Point(rect.x,rect.y),new Point(rect.x+rect.width,rect.y+rect.height),
                        new Scalar(255,0,0),3);
            }
            if(!temporalPixelBeltMap.isEmpty()){
                int i=frameNo;
                while(!temporalPixelBeltMap.containsKey(i)) --i;
                TemporalPixelBelt tpb = temporalPixelBeltMap.get(i);
                for(PixelLine pl:tpb.pixelLines){
                    if(pl.isHorizontal){
                        Imgproc.line(beltMat,new Point(0,pl.location),new Point(beltMat.width(),pl.location),new Scalar(255,255,255));
                    } else {
                        Imgproc.line(beltMat,new Point(pl.location,0),new Point(pl.location,beltMat.height()),new Scalar(255,255,255));
                    }
                }
            }
            videoThumbnail.setImage(Utils.mat2Image(src));
            pixelBeltViewer.setImage(Utils.mat2Image(beltMat));
        }
    }
}
