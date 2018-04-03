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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author bkusche
 *
 */
public class TabbedMainController{

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	private static final String highlightingFxmlFile = "/fxml/HighlightingView.fxml";
	
	private Preferences prefs;
	private List<Highlighting> highlightings;
	private Theme theme;
	@FXML TabPane tabpane;
	@FXML MenuBar menuBar;


	public TabbedMainController() {
		
	}
	
	@FXML void initialize() {
		menuBar.useSystemMenuBarProperty().set(true);
		prefs = Preferences.userRoot().node("bktail2");
		highlightings = new ArrayList<>();
		theme = new Theme(Color.web(prefs.get(Highlighting.THEME_TEXTCOLOR, "#ffffff")),
    			Color.web(prefs.get(Highlighting.THEME_BACKGROUNDCOLOR, "#000000")));
		Highlighting.loadFromPreferences(prefs, highlightings);
		prefs.addPreferenceChangeListener( p -> {
			highlightings.clear();
			theme.setForegroundColor(Color.web(prefs.get(Highlighting.THEME_TEXTCOLOR, "#ffffff")));
			theme.setBackgroundColor(Color.web(prefs.get(Highlighting.THEME_BACKGROUNDCOLOR, "#000000")));
			Highlighting.loadFromPreferences(prefs, highlightings);
		});
	}
	
	@FXML void onDragDroppedEvent(DragEvent e){
		final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
        	success = true;
            db.getFiles().forEach(this::openLogTab);
        }
        e.setDropCompleted(success);
        e.consume();
		
	}

	@FXML void mouseDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();

        if (db.hasFiles()) {
			final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".log")
					|| db.getFiles().get(0).getName().toLowerCase().endsWith(".txt");
            if (isAccepted) {
                tabpane.setStyle("-fx-border-color: red;"
              + "-fx-border-width: 5;"
              + "-fx-background-color: #C6C6C6;"
              + "-fx-border-style: solid;");//TODO move to css
                e.acceptTransferModes(TransferMode.COPY);
            }
        } else {
            e.consume();
        }
    }
	
	@FXML void mouseDragExit(final DragEvent e) {
		tabpane.setStyle("-fx-border-color: #C6C6C6;");//TODO move to css
	}

	
	@FXML void onOpenLogFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
		if( files == null || files.isEmpty() ) return;
		files.forEach(this::openLogTab);
    }

	
	@FXML void onOpenHighlighting(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(highlightingFxmlFile));
			loader.setControllerFactory( p ->{
				HighlightingController hc = new HighlightingController();
				hc.init(prefs);
				return hc;
			});
			Stage stage = new Stage();
			Parent rootNode = (Parent) loader.load();
			Scene scene = new Scene(rootNode);

			scene.getStylesheets().add(TabbedMainController.class.getResource("/styles/styles.css").toExternalForm());
			stage.setTitle("bktail2 highlighting");
			stage.setScene(scene);
			stage.show();
		} catch (Throwable e) {
			//TODO display error message
			e.printStackTrace();
		}
	}
	
	private void openLogTab( File logfile ){
		Platform.runLater( () -> {
			try {
				BktailTab tab = new BktailTab(logfile.getName());
				tab.setClosable(true);
				FXMLLoader loader = new FXMLLoader(getClass().getResource(logViewerFxmlFile));
				loader.setControllerFactory( p ->{
			    	LogviewerController lc = new LogviewerController();
			    	lc.init(logfile,
			    			theme,
			    			highlightings);
					tab.setTailActionEventListener(lc);
					lc.setTailActionEventListener(tab);
			    	return lc;
				});

				tab.setContent(loader.load());
				tab.setOnClosed(e -> {
					((LogviewerController)loader.getController()).dispose();
					tabpane.getTabs().remove(e.getSource());
				});
				tabpane.getTabs().add(tab);
			} catch (Throwable ex) {
				// TODO display error message
				ex.printStackTrace();
			}
		});
	}
}
