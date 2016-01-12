/**
 * Copyright 2016 Björn Kusche
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bkusche.bktail2.gui.jfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author bkusche
 *
 */
public class MainApp extends Application {

	//-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/bktail2_heapdump`${current_date}`.hprof
	//-XX:+DisableAttachMechanism
	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(Stage stage) throws Exception {

		String fxmlFile = "/fxml/TabbedMainView.fxml";

		FXMLLoader loader = new FXMLLoader();
		Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
		Scene scene = new Scene(rootNode, 1024, 768);

		scene.getStylesheets().add("/styles/styles.css");
		stage.setTitle("bktail2");
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> System.exit(1) );
		stage.show();
	}
}