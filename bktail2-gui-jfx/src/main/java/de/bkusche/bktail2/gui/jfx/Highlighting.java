/*
 * Copyright 2016 Björn Kusche
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bkusche.bktail2.gui.jfx;

import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/**
 * @author bkusche
 *
 */
public class Highlighting {
	static final String THEME_TEXTCOLOR = "THEME_TEXTCOLOR";
	static final String THEME_BACKGROUNDCOLOR = "THEME_BACKGROUNDCOLOR";
	static final String HIGHLIGHTING_ENTRY = "HIGHLIGHTING_ENTRY";
    static final String HIGHLIGHTING_VALUESDELIMITER = ";";
    static final String HIGHLIGHTING_VALUEDELIMITER = "->";
    static final String HIGHLIGHTING_VALUE_TEXT = "HIGHLIGHTING_VALUE_TEXT";
    static final String HIGHLIGHTING_VALUE_FG = "HIGHLIGHTING_VALUE_FG";
    static final String HIGHLIGHTING_VALUE_BG = "HIGHLIGHTING_VALUE_BG";
	
	private StringProperty text;
	private ObjectProperty<Color> textColor;
	private ObjectProperty<Color> backgroundColor;
	
	/**
	 * populates the highlightings list with the values of the preferences
	 * @param preferences
	 * @param highlightings
	 */
	static void loadFromPreferences(Preferences preferences, List<Highlighting> highlightings){
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


	final StringProperty textProperty() {
		return this.text;
	}
	

	final java.lang.String getText() {
		return this.textProperty().get();
	}
	

	final void setText(final java.lang.String text) {
		this.textProperty().set(text);
	}
	
	final void setTextColor( Color textColor){
		this.textColor.setValue(textColor);
	}
	
	final void setBackgroundColor( Color backgroundColor ){
		this.backgroundColor.setValue(backgroundColor);
	}
	
	final ObjectProperty<Color> textColorProperty(){
		return this.textColor;
	}
	
	final ObjectProperty<Color> backgroundColorProperty(){
		return this.backgroundColor;
	}

}
