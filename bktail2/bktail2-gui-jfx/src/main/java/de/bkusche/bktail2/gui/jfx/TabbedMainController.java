package de.bkusche.bktail2.gui.jfx;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TabbedMainController implements Initializable{

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	@FXML TabPane tabpane;
	@FXML MenuBar menuBar;


	public TabbedMainController() {
		
	}
	
	@Override public void initialize(URL location, ResourceBundle resources) {
		menuBar.useSystemMenuBarProperty().set(true);
	}
	
	@FXML
	void onDragDroppedEvent(DragEvent e){
		final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
//            final File file = db.getFiles().get(0);
//            openLogTab(file);
            db.getFiles().forEach(this::openLogTab);
        }
        e.setDropCompleted(success);
        e.consume();
		
	}

	@FXML
	void mouseDragOver(final DragEvent e) {
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
	
	@FXML
	void mouseDragExit(final DragEvent e) {
		tabpane.setStyle("-fx-border-color: #C6C6C6;");//TODO move to css
	}

	
	@FXML
    void onOpenLogFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
		if( files == null || files.isEmpty() ) return;
		files.forEach(this::openLogTab);
    }

	
	@FXML
	void onOpenHighlighting(ActionEvent event) {
		System.out.println("onOpenHighlighting");
	}
	
	private void openLogTab( File logfile ){
		Platform.runLater( () -> {
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(logViewerFxmlFile));
				loader.setControllerFactory(new Callback<Class<?>,Object>(){
				    @Override public Object call( Class<?> param){
				    	LogviewerController lc = new LogviewerController();
				    	lc.init(logfile);
				    	return lc;
				    }
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
