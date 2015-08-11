package de.bkusche.bktail2.gui.jfx;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Highlighting {
	private StringProperty text;

	public Highlighting(String text) {
		super();
		this.text = new SimpleStringProperty();
		setText(text);
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
	
}
