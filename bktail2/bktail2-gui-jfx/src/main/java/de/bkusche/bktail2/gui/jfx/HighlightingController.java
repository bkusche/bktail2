package de.bkusche.bktail2.gui.jfx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class HighlightingController {

    @FXML Label lblForBackColorTitle;
    @FXML Label lblText;
    @FXML Label lblBackgrount;
    @FXML TableView<?> tblContent;
    @FXML ContextMenu tblContextMenu;
    @FXML Button btnText;
    @FXML Button btnBackground;
    @FXML Button btnOK;
    @FXML Button btnCancel;
    

    @FXML
    void initialize() {
    	
    }

    
    @FXML
    void onTextColor(ActionEvent event) {

    }

    @FXML
    void onBackgroundColor(ActionEvent event) {

    }
    
    @FXML
    void onAddEntry(ActionEvent event) {

    }

    @FXML
    void onRemoveEntry(ActionEvent event) {

    }

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void onOK(ActionEvent event) {

    }

}