package com.nugsky.tugasakhir.controller;

import com.nugsky.tugasakhir.models.ForegroundObject;
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
import org.opencv.core.Core;
import org.opencv.core.Mat;
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

    public static VideoCapture sourceCapture;
    public static VideoCapture maskCapture;

    private static List<TemporalPixelBeltGroup> temporalPixelBeltGroupList = new ArrayList<>();
    private static List<ForegroundObject> foregroundObjects = new ArrayList<>();

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
            fileTextField.setText(file.getAbsolutePath());
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
