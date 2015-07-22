package de.bkusche.bktail2.gui.jfx;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class TabbedMainController implements Initializable {

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	@FXML TabPane tabpane;

	private FXMLLoader loader;
	private Node node;
	private LogviewerController lc;
	public TabbedMainController() {
		try {
			loader = new FXMLLoader();
			
		} catch (Throwable e) {
			throw new RuntimeException("Logviewer initialization has failed with: "+e.getMessage());
		}
	}
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			node = (Node)loader.load(getClass().getResourceAsStream(logViewerFxmlFile));
			lc = loader.<LogviewerController>getController();
		} catch (Throwable e) {
			throw new RuntimeException("Logviewer initialization has failed with: "+e.getMessage());
		}
	}
	int t = 0;
	
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
    				lc.init(file);
    				Tab tab = new Tab("Some title_"+t);
    				tab.setContent(node);
    				
    				tabpane.getTabs().add(tab);
    				t++;
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
