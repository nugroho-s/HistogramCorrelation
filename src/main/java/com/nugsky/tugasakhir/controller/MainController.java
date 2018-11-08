package com.nugsky.tugasakhir.controller;

import com.nugsky.tugasakhir.Loader;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

import java.io.File;

public class MainController {
    final static Logger logger = Logger.getLogger(MainController.class);
    public void chooseFile(ActionEvent event){
        logger.debug("click");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Video");
        File file = fileChooser.showOpenDialog(null);
        try{
            logger.debug(file.getAbsolutePath());
        }catch (RuntimeException e) {

        }
    }
}
