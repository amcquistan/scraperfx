// App.java

package com.thecodinginterface.scraperfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // instantiate FXMLoader and load the ScraperFX.fxml's BorderPane
        var fxmlLoader = new FXMLLoader(getClass().getResource("ScraperFX.fxml"));
        var borderPane = (BorderPane) fxmlLoader.load();

        // create a Scene object and source the styles.css stylesheet
        var scene = new Scene(borderPane);
        scene.getStylesheets().add(
            getClass().getResource("styles.css").toExternalForm()
        );

        // set main window title, associate the scene and show the window
        primaryStage.setTitle("ScraperFX");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
