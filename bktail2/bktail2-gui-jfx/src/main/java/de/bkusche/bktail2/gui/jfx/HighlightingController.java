package de.bkusche.bktail2.gui.jfx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;

public class HighlightingController {

    @FXML Label lblForBackColorTitle;
    @FXML Label lblText;
    @FXML Label lblBackgrount;
    @FXML TableView<Highlighting> tblContent;
    @FXML TableColumn<Highlighting, String> tblColText;
    @FXML TableColumn<Highlighting, Boolean> tblColColor;
    @FXML ContextMenu tblContextMenu;
    @FXML ColorPicker btnText;
    @FXML ColorPicker btnBackground;
    @FXML Button btnOK;
    @FXML Button btnCancel;
    
    private ObservableList<Highlighting> highlightings;

    @FXML
    void initialize() {
    	//
    	//HACK //TODO remove!!!
    	btnText.setValue(Color.BLACK);
    	highlightings = FXCollections.observableArrayList( new Highlighting("test",btnText.getValue()));
    	tblContent.setItems(highlightings);
    	
    	tblColText.setCellValueFactory(new PropertyValueFactory<>("text"));
    	tblColText.setCellFactory( (column) -> { 
    		return new HighLightingTextCell();
		});
    	
    	// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
    	tblColColor.setCellValueFactory((features) -> {
    		return new SimpleBooleanProperty(features.getValue() != null);
    	});
    	
    	tblColColor.setCellFactory((column) -> { 
    		return new HighlightingButtonCell(tblContent);
		});
    }
    
    @FXML
    void onAddEntry(ActionEvent event) {
    	highlightings.add(new Highlighting("CHANGE ME!",btnText.getValue()));
    }

    @FXML
    void onRemoveEntry(ActionEvent event) {
    	highlightings.remove(tblContent.getSelectionModel().getSelectedIndex());
    }

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void onOK(ActionEvent event) {

    }
    
    
	private class HighlightingButtonCell extends TableCell<Highlighting, Boolean> {

		final ColorPicker btnFgColor;
		final ColorPicker btnBgColor;
		final HBox hBox;

		HighlightingButtonCell(final TableView<Highlighting> table) {
			hBox = new HBox();
			
			StackPane paddedFgLabel = new StackPane();
			paddedFgLabel.setPadding(new Insets(1));
			paddedFgLabel.getChildren().add(new Label("FG"));
			
			btnFgColor = new ColorPicker(Color.BLACK); //TODO obtain from main
			btnFgColor.setStyle("-fx-color-label-visible: false ;");
			StackPane paddedFgButton = new StackPane();
			paddedFgButton.setPadding(new Insets(1));
			paddedFgButton.getChildren().add(btnFgColor);
			
			StackPane paddedBgLabel = new StackPane();
			paddedBgLabel.setPadding(new Insets(1));
			paddedBgLabel.getChildren().add(new Label("BG"));
			
			btnBgColor = new ColorPicker(Color.WHITE); //TODO obtain from main
			btnBgColor.setStyle("-fx-color-label-visible: false ;");
			StackPane paddedBgButton = new StackPane();
			paddedBgButton.setPadding(new Insets(1));
			paddedBgButton.getChildren().add(btnBgColor);
			hBox.getChildren().add(paddedFgLabel);
			hBox.getChildren().add(paddedFgButton);
			hBox.getChildren().add(paddedBgLabel);
			hBox.getChildren().add(paddedBgButton);
		}


		/** places an add button in the row only if the row is not empty. */
		@Override protected void updateItem(Boolean item, boolean empty) {
			super.updateItem(item, empty);
			if (!empty) {
				//
				//
	            Highlighting currentHighlighting = getTableRow() == null ? null : (Highlighting)getTableRow().getItem();
				if( currentHighlighting != null ){	
					btnFgColor.setValue(currentHighlighting.textColorProperty().getValue());
					currentHighlighting.textColorProperty().bind(btnFgColor.valueProperty());
				
				}
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(hBox);
			} else {
				setGraphic(null);
			}
		}
	}

	private class HighLightingTextCell extends TextFieldTableCell<Highlighting, String>{
		
		public HighLightingTextCell() {
			super(new DefaultStringConverter());
		}
		
		@Override public void startEdit() {
			super.startEdit();
		}
		@Override public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if( !empty )
			{
				setText(item);
	            setGraphic(null);
	            Highlighting currentTask = getTableRow() == null ? null : (Highlighting)getTableRow().getItem();
	            if( currentTask != null ){
	            	textFillProperty().bind(currentTask.textColorProperty());
//	            	styleProperty().
//	            	backgroundProperty().bind(currentTask.backgroundColorProperty());
	            }
			}
		}
		@Override public void commitEdit(String newValue) {
			super.commitEdit(newValue);
			((Highlighting) getTableRow().getItem()).setText(newValue);
		}
	}
}