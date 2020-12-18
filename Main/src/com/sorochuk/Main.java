package com.sorochuk;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {
    public static Stage primaryStage;

    public static void main(String[] args) {
        Application.launch();
    }

    public void setStage(Stage stage) {
        Main.primaryStage = stage;
    }
    public static Stage getPrimaryStage() { return primaryStage; }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainForm2.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(new File("E:\\JavaProjects\\CourseProject\\Main\\resourses\\images\\mainFormIcon.png").toURI().toString()));
        primaryStage.setTitle("SL-парсер");

        runStage(primaryStage);
    }

    public static void runStage(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.show();
    }
}
