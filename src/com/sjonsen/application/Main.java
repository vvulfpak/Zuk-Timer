package com.sjonsen.application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;


public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
		    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("gui.fxml"));
		    Parent root = (Parent) fxmlLoader.load();
		    Controller controller = (Controller)fxmlLoader.getController();
            Scene scene = new Scene(root, 200, 90);
            primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream( "pizza.png" )));
            primaryStage.setTitle("Zuk Timer");
            primaryStage.setResizable(false);
            primaryStage.setAlwaysOnTop(true);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Platform.exit();
                    System.exit(0);
                }
            });
			primaryStage.setScene(scene);
			controller.setStage(primaryStage);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
