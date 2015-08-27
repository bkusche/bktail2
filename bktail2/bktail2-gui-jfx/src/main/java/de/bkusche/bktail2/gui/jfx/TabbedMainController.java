package de.bkusche.bktail2.gui.jfx;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class TabbedMainController implements Initializable{

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	private static final String highlightingFxmlFile = "/fxml/HighlightingView.fxml";
	
	private Preferences prefs;
	private List<Highlighting> highlightings;
	@FXML TabPane tabpane;
	@FXML MenuBar menuBar;


	public TabbedMainController() {
		
	}
	
	@Override public void initialize(URL location, ResourceBundle resources) {
		menuBar.useSystemMenuBarProperty().set(true);
		prefs = Preferences.userRoot().node("bktail2");
		highlightings = new ArrayList<>();
		Highlighting.loadFromPreferences(prefs, highlightings);
		prefs.addPreferenceChangeListener( p -> {
			highlightings.clear();
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
 
        final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".log")
                || db.getFiles().get(0).getName().toLowerCase().endsWith(".txt");
 
        if (db.hasFiles()) {
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

			scene.getStylesheets().add("/styles/styles.css");
			stage.setTitle("Logviewer");
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
				FXMLLoader loader = new FXMLLoader(getClass().getResource(logViewerFxmlFile));
				loader.setControllerFactory( p ->{
			    	LogviewerController lc = new LogviewerController();
			    	lc.init(logfile,
			    			Color.web(prefs.get(Highlighting.THEME_TEXTCOLOR, "#ffffff")),
			    			Color.web(prefs.get(Highlighting.THEME_BACKGROUNDCOLOR, "#000000")),
			    			highlightings);
			    	return lc;
				});
				Tab tab = new Tab(logfile.getName());
				tab.setContent(loader.load());
				
				tabpane.getTabs().add(tab);
			} catch (Throwable ex) {
				// TODO display error message
				ex.printStackTrace();
			}
		});
	}
}
