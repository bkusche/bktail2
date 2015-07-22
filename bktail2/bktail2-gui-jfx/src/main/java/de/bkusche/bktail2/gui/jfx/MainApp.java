package de.bkusche.bktail2.gui.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(Stage stage) throws Exception {

		String fxmlFile = "/fxml/Logviewer.fxml";

		FXMLLoader loader = new FXMLLoader();
		Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
		Scene scene = new Scene(rootNode, 1024, 768);

		scene.getStylesheets().add("/styles/styles.css");
		stage.setTitle("Logviewer");
		stage.setScene(scene);
		stage.show();
	}
}
