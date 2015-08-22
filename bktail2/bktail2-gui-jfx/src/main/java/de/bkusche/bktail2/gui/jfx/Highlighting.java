package de.bkusche.bktail2.gui.jfx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class Highlighting {
	private StringProperty text;
	private ObjectProperty<Color> textColor;
	private ObjectProperty<Color> backgroundColor;
	
	public Highlighting(String text, Color textColor) {
		super();
		this.text = new SimpleStringProperty();
		this.textColor = new SimpleObjectProperty<>();
		this.backgroundColor = new SimpleObjectProperty<>();
		setText(text);
		setTextColor(textColor);
	}


	public final StringProperty textProperty() {
		return this.text;
	}
	

	public final java.lang.String getText() {
		return this.textProperty().get();
	}
	

	public final void setText(final java.lang.String text) {
		this.textProperty().set(text);
	}
	
	public final void setTextColor( Color textColor){
		this.textColor.setValue(textColor);
	}
	
	public final ObjectProperty<Color> textColorProperty(){
		return this.textColor;
	}
	
	public final ObjectProperty<Color> backgroundColorProperty(){
		return this.backgroundColor;
	}
	
	
//	public final int getTextColor(){
//		return this.textColorProperty().
//	}
}
