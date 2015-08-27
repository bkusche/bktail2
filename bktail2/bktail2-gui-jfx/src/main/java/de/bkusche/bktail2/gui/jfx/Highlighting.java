package de.bkusche.bktail2.gui.jfx;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class Highlighting {
	public static final String HIGHLIGHTING_ENTRY = "HIGHLIGHTING_ENTRY";
    public static final String HIGHLIGHTING_VALUESDELIMITER = ";";
    public static final String HIGHLIGHTING_VALUEDELIMITER = "->";
    public static final String HIGHLIGHTING_VALUE_TEXT = "HIGHLIGHTING_VALUE_TEXT";
    public static final String HIGHLIGHTING_VALUE_FG = "HIGHLIGHTING_VALUE_FG";
    public static final String HIGHLIGHTING_VALUE_BG = "HIGHLIGHTING_VALUE_BG";
	
	private StringProperty text;
	private ObjectProperty<Color> textColor;
	private ObjectProperty<Color> backgroundColor;
	
	/**
	 * populates the highlightings list with the values of the preferences
	 * @param preferences
	 * @param highlightings
	 */
	public static void loadFromPreferences(Preferences preferences, List<Highlighting> highlightings){
		try {
			for( String key: preferences.keys() ){
				if( !key.startsWith(HIGHLIGHTING_ENTRY)) continue;
				String text = null;
				String fg = null;
				String bg = null;
				for( String val : preferences.get(key,"").split(HIGHLIGHTING_VALUESDELIMITER)){
					if( val.startsWith(HIGHLIGHTING_VALUE_TEXT) ){
						text = val.replace(HIGHLIGHTING_VALUE_TEXT + HIGHLIGHTING_VALUEDELIMITER, "");
					} else if( val.startsWith(HIGHLIGHTING_VALUE_FG) ){
						fg = val.replace(HIGHLIGHTING_VALUE_FG + HIGHLIGHTING_VALUEDELIMITER, "");
					} else if( val.startsWith(HIGHLIGHTING_VALUE_BG) ){
						bg = val.replace(HIGHLIGHTING_VALUE_BG + HIGHLIGHTING_VALUEDELIMITER, "");
					}
				}
				highlightings.add(new Highlighting(text, Color.web(fg), Color.web(bg)));
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Highlighting(String text, Color textColor, Color backgroundColor) {
		super();
		this.text = new SimpleStringProperty();
		this.textColor = new SimpleObjectProperty<>();
		this.backgroundColor = new SimpleObjectProperty<>();
		setText(text);
		setTextColor(textColor);
		setBackgroundColor(backgroundColor);
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
	
	public final void setBackgroundColor( Color backgroundColor ){
		this.backgroundColor.setValue(backgroundColor);
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
