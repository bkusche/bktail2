package de.bkusche.bktail2.gui.jfx;

import java.io.File;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

public class TabbedMainController {

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	@FXML TabPane tabpane;

	public TabbedMainController() {
		
	}
	
	
	@FXML
	public void onDragDroppedEvent(DragEvent e){
		final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            success = true;
            // Only get the first file from the list
            final File file = db.getFiles().get(0);
            Platform.runLater( () -> {
    			try {
    				FXMLLoader loader = new FXMLLoader(getClass().getResource(logViewerFxmlFile));
    				loader.setControllerFactory(new Callback<Class<?>,Object>(){
    				    @Override public Object call( Class<?> param){
    				    	LogviewerController lc = new LogviewerController();
    				    	lc.init(file);
    				    	return lc;
    				    }
    				  });
    				Tab tab = new Tab(file.getName());
    				tab.setContent(loader.load());
    				
    				tabpane.getTabs().add(tab);
    			} catch (Throwable ex) {
    				// TODO Auto-generated catch block
    				ex.printStackTrace();
    			}
    		});
        }
        e.setDropCompleted(success);
        e.consume();
		
	}

	@FXML
	public void mouseDragOver(final DragEvent e) {
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
	public void mouseDragExit(final DragEvent e) {
		tabpane.setStyle("-fx-border-color: #C6C6C6;");//TODO move to css
	}
}
