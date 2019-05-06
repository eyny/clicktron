package com.clicktron;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    // Main application singleton
    private static Main instance;
    public static Main getInstance() { return instance; }
    private static void setInstance(Main instance) { Main.instance = instance; }

    // Runnable onAppExit to be invoked whenever application is closed
    public Runnable onAppExit;
    @Override
    public void stop() {
        if (onAppExit != null) { onAppExit.run(); }
    }

    @Override
    public void start(Stage stage) throws Exception {
        setInstance(this);
        Parent root = FXMLLoader.load(getClass().getResource("fxml/view.fxml"));
        stage.setTitle("Clicktron");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("images/icon.png")));
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}