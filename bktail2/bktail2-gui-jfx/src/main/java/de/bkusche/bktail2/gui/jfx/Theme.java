package de.bkusche.bktail2.gui.jfx;

import javafx.scene.paint.Color;

public class Theme {

	private Color foregroundColor;
	private Color backgroundColor;
	
	public Theme(Color foregroundColor, Color backgroundColor) {
		super();
		this.foregroundColor = foregroundColor;
		this.backgroundColor = backgroundColor;
	}
	
	public Color getForegroundColor() {
		return foregroundColor;
	}
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	
}
