package com.bank.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("Banking System starts here"), 400, 200));
        stage.setTitle("Banking System");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}