package com.nugsky.tugasakhir;

import com.nugsky.tugasakhir.utils.ResourcesWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import javafx.application.Application;

public class Loader extends Application{
    @Override
    public void start(Stage primaryStage) {
        ResourcesWrapper.init(getClass());
        Parent root = null;
        try {
            root = FXMLLoader.load(ResourcesWrapper.getResource("/MainFX.fxml",getClass()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root, 854, 480);

        primaryStage.setTitle("Deteksi Keaslian Video");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
