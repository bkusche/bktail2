package de.bkusche.bktail2.gui.jfx;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    
    private static final String THEME_TEXTCOLOR = "THEME_TEXTCOLOR";
    private static final String THEME_BACKGROUNDCOLOR = "THEME_BACKGROUNDCOLOR";
    
    
    private ObservableList<Highlighting> highlightings;
    private Preferences preferences;
    
    @FXML void initialize() {
    	highlightings = FXCollections.observableArrayList( 
    			);
//    			new Highlighting("test",btnText.getValue(),btnBackground.getValue()));
    	tblContent.setItems(highlightings);
    	
    	tblColText.setCellValueFactory(new PropertyValueFactory<>("text"));
    	tblColText.setCellFactory( column -> { 
    		return new HighLightingTextCell();
		});
    	
    	// define a simple boolean cell value for the action column so that the column will only be shown for non-empty rows.
    	tblColColor.setCellValueFactory( features -> {
    		return new SimpleBooleanProperty(features.getValue() != null);
    	});
    	
    	tblColColor.setCellFactory( column -> { 
    		return new HighlightingButtonCell(tblContent);
		});
    	
    	btnText.setValue(Color.web(preferences.get(THEME_TEXTCOLOR, "#ffffff")));
    	btnBackground.setValue(Color.web(preferences.get(THEME_BACKGROUNDCOLOR, "#000000")));
    	Highlighting.loadFromPreferences(preferences, highlightings);
    }
    
    public void init( Preferences preferences ){
    	this.preferences = preferences;
    }
    
    @FXML void onAddEntry(ActionEvent event) {
    	highlightings.add(
    			new Highlighting("CHANGE ME!",btnText.getValue(),btnBackground.getValue()));
    }

    @FXML void onRemoveEntry(ActionEvent event) {
    	highlightings.remove(tblContent.getSelectionModel().getSelectedIndex());
    }

    @FXML void onCancel(ActionEvent event) {
    	//TODO close this stage
    }

    @FXML void onOK(ActionEvent event) {
    	for( int i = 0; i < highlightings.size(); i++){
    		Highlighting h = highlightings.get(i);
    		String value = Highlighting.HIGHLIGHTING_VALUE_TEXT + Highlighting.HIGHLIGHTING_VALUEDELIMITER + h.getText()+Highlighting.HIGHLIGHTING_VALUESDELIMITER
    				+Highlighting.HIGHLIGHTING_VALUE_FG+Highlighting.HIGHLIGHTING_VALUEDELIMITER+h.textColorProperty().getValue().toString().replace("0x", "#")+Highlighting.HIGHLIGHTING_VALUESDELIMITER
    				+Highlighting.HIGHLIGHTING_VALUE_BG+Highlighting.HIGHLIGHTING_VALUEDELIMITER+h.backgroundColorProperty().getValue().toString().replace("0x", "#");
    		preferences.put(Highlighting.HIGHLIGHTING_ENTRY+"_"+i, value);
    	}
    	
    	try {
			preferences.flush();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
	private class HighlightingButtonCell extends TableCell<Highlighting, Boolean> implements ChangeListener<Color> {

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
					
					if( !currentHighlighting.textColorProperty().getValue().toString().equals(
							btnFgColor.getValue().toString())){
						btnFgColor.setValue(currentHighlighting.textColorProperty().getValue());
					}
					if( !currentHighlighting.backgroundColorProperty().getValue().toString().equals(
							btnBgColor.getValue().toString())){
						btnBgColor.setValue(currentHighlighting.backgroundColorProperty().getValue());
					}
					
					currentHighlighting.textColorProperty().bind(btnFgColor.valueProperty());

					btnBgColor.valueProperty().removeListener(this);
					btnBgColor.valueProperty().addListener(this);
					
				}
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setGraphic(hBox);
			} else {
				setGraphic(null);
			}
		}

		@Override public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			Highlighting currentHighlighting = getTableRow() == null ? null : (Highlighting)getTableRow().getItem();
			if( currentHighlighting != null ){	
				currentHighlighting.backgroundColorProperty().set(btnBgColor.getValue());
			}
		}
	}

	private class HighLightingTextCell extends TextFieldTableCell<Highlighting, String> implements ChangeListener<Color> {
		
		private String defaultStyle = null;
		public HighLightingTextCell() {
			super(new DefaultStringConverter());
			defaultStyle = getStyle();
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
            		
	            	setStyle("-fx-background-color: "
	        				+currentTask.backgroundColorProperty().getValue().toString().replace("0x", "#"));
	            	
	            	textFillProperty().bind(currentTask.textColorProperty());
//	            	System.out.println(currentTask.backgroundColorProperty().getValue().toString()
//	            			+" "+getTableRow().getIndex()
//	            			+" "+currentTask.getText());

	            	currentTask.backgroundColorProperty().removeListener(this);
	            	currentTask.backgroundColorProperty().addListener(this);
	            }
			}
			else
				setStyle(defaultStyle);
		}
		@Override public void commitEdit(String newValue) {
			super.commitEdit(newValue);
			((Highlighting) getTableRow().getItem()).setText(newValue);
		}

		@Override public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
			Highlighting currentTask = getTableRow() == null ? null : (Highlighting)getTableRow().getItem();
            if( currentTask != null ){
            	setStyle("-fx-background-color: "
        				+currentTask.backgroundColorProperty().getValue().toString().replace("0x", "#"));
            }
		}
		
	}
}