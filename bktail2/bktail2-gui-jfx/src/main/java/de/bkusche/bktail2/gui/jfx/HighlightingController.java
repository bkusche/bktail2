package de.bkusche.bktail2.gui.jfx;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class HighlightingController {

    @FXML Label lblForBackColorTitle;
    @FXML Label lblText;
    @FXML Label lblBackgrount;
    @FXML TableView<Highlighting> tblContent;
    @FXML TableColumn<Highlighting, String> tblColText;
    @FXML TableColumn<Highlighting, Boolean> tblColColor;
    @FXML ContextMenu tblContextMenu;
    @FXML Button btnText;
    @FXML Button btnBackground;
    @FXML Button btnOK;
    @FXML Button btnCancel;
    
    private ObservableList<Highlighting> highlightings;

    @FXML
    void initialize() {
    	highlightings = FXCollections.observableArrayList( new Highlighting("test"));
    	tblContent.setItems(highlightings);
    	tblColText.setCellValueFactory(new PropertyValueFactory<>("text"));
    	
    	// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
    	tblColColor.setCellValueFactory((features) -> {
    		return new SimpleBooleanProperty(features.getValue() != null);
    	});
    	
    	// create a cell value factory with an add button for each row in the table.
    	tblColColor.setCellFactory((column) -> { 
    		return new HighlightingButtonCell(tblContent);
		});
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
    
    
    /** A table cell containing a button for customizing the highlighting. */
	private class HighlightingButtonCell extends TableCell<Highlighting, Boolean> {

		final Button button;
		final StackPane paddedButton;
		// records the y pos of the last button press so that the add person
		// dialog can be shown next to the cell.
		final DoubleProperty buttonY;

		HighlightingButtonCell(final TableView<Highlighting> table) {
			button = new Button("...");
			paddedButton = new StackPane();
			buttonY = new SimpleDoubleProperty();
			paddedButton.setPadding(new Insets(1));
			paddedButton.getChildren().add(button);
			button.setOnMousePressed((e) -> buttonY.set(e.getScreenY()) );

			//TODO open color chooser'n stuff
			button.setOnAction((e) -> {
				System.out.println("do stuff!");
				table.getSelectionModel().select(getTableRow().getIndex());
			} );
		}

		/** places an add button in the row only if the row is not empty. */
		@Override protected void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) {
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(paddedButton);
			} else {
				setGraphic(null);
			}
		}
	}

}