/**
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

import java.io.File;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author bkusche
 *
 */
public class TabbedMainController{

	private static final String logViewerFxmlFile = "/fxml/Logviewer.fxml";
	private static final String highlightingFxmlFile = "/fxml/HighlightingView.fxml";

	//
    //TAB RELATED CONFIGURATION VALUES
	private static final String WINDOW = "WINDOW";
    private static final String TAB = "TAB";
    private static final String PATH = "PATH";
    private static final String NAME = "NAME";
    private static final String CHECKED = "CHECKED";
	
	private Preferences prefs;
	private List<Highlighting> highlightings;
	private Theme theme;
	@FXML TabPane tabpane;
	@FXML MenuBar menuBar;


	public TabbedMainController() {
		
	}
	
	@FXML void initialize() {
		menuBar.useSystemMenuBarProperty().set(true);
		prefs = Preferences.userRoot().node("bktail2");
		highlightings = new ArrayList<>();
		theme = new Theme(Color.web(prefs.get(Highlighting.THEME_TEXTCOLOR, "#ffffff")),
    			Color.web(prefs.get(Highlighting.THEME_BACKGROUNDCOLOR, "#000000")));
		Highlighting.loadFromPreferences(prefs, highlightings);
		prefs.addPreferenceChangeListener( p -> {
			highlightings.clear();
			theme.setForegroundColor(Color.web(prefs.get(Highlighting.THEME_TEXTCOLOR, "#ffffff")));
			theme.setBackgroundColor(Color.web(prefs.get(Highlighting.THEME_BACKGROUNDCOLOR, "#000000")));
			Highlighting.loadFromPreferences(prefs, highlightings);
		});

		//
        //registering initial "main" tabpane
		BktailTab.getTabPanes().add(tabpane);

		restoreOpenTabs();
		//
        //registering a shutdown hook thread
		Runtime.getRuntime().addShutdownHook( new Thread(() -> {
            shutdown();
        }));
	}
	
	@FXML void onDragDroppedEvent(DragEvent e){
		final Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
        	success = true;
            db.getFiles().forEach(this::openLogTab);
        }
        e.setDropCompleted(success);
        e.consume();
		
	}

	@FXML void mouseDragOver(final DragEvent e) {
        final Dragboard db = e.getDragboard();

        if (db.hasFiles()) {
			final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".log")
					|| db.getFiles().get(0).getName().toLowerCase().endsWith(".txt");
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
	
	@FXML void mouseDragExit(final DragEvent e) {
		tabpane.setStyle("-fx-border-color: #C6C6C6;");//TODO move to css
	}

	
	@FXML void onOpenLogFile(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		List<File> files = fileChooser.showOpenMultipleDialog(new Stage());
		if( files == null || files.isEmpty() ) return;
		files.forEach(this::openLogTab);
    }

	
	@FXML void onOpenHighlighting(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(highlightingFxmlFile));
			loader.setControllerFactory( p ->{
				HighlightingController hc = new HighlightingController();
				hc.init(prefs);
				return hc;
			});
			Stage stage = new Stage();
			Parent rootNode = (Parent) loader.load();
			Scene scene = new Scene(rootNode);

			scene.getStylesheets().add(TabbedMainController.class.getResource("/styles/styles.css").toExternalForm());
			stage.setTitle("bktail2 highlighting");
			stage.setScene(scene);
			stage.show();
		} catch (Throwable e) {
			//TODO display error message
			e.printStackTrace();
		}
	}
	private void openLogTab( File logfile ){
		openLogTab(logfile,false);
	}

	private void openLogTab( File logfile, boolean checked ){
		try {
			BktailTab tab = new BktailTab(logfile.getName());
			tab.setClosable(true);
			tab.setChecked(checked);
			FXMLLoader loader = new FXMLLoader(getClass().getResource(logViewerFxmlFile));
			loader.setControllerFactory( p ->{
				LogviewerController lc = new LogviewerController();
				lc.init(logfile,
						theme,
						highlightings);
				tab.setTailActionEventListener(lc);
				tab.setLogviewerController(lc);
				lc.setTailActionEventListener(tab);
				return lc;
			});

			tab.setContent(loader.load());
			tab.setOnClosed(e -> {
				((LogviewerController)loader.getController()).dispose();
				tabpane.getTabs().remove(e.getSource());
			});
			tabpane.getTabs().add(tab);
		} catch (Throwable ex) {
			// TODO display error message
			ex.printStackTrace();
		}
	}

	private void restoreOpenTabs(){
		try {
			Set<String> keys = new HashSet<>();
			Arrays.stream(prefs.keys()).filter(f -> f.startsWith(WINDOW)).sorted().forEach(key -> {
				keys.add(key.split("\\.")[0]);
			});
			keys.stream().sorted().forEach(key -> {
				int window = Integer.valueOf(key.split("\\.")[0].split("&")[0].split("_")[1]);
				int tab = Integer.valueOf(key.split("\\.")[0].split("&")[0].split("_")[1]);
				String name = prefs.get(key+"."+NAME,null);
				String path = prefs.get(key+"."+PATH,null);
				boolean checked = Boolean.valueOf(prefs.get(key+"."+CHECKED,null));
				System.out.println();
				openLogTab(new File(path), checked);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void shutdown()
    {
        System.out.println("performing shutdown....");
        try {
			Arrays.stream(prefs.keys()).filter(f -> f.startsWith(WINDOW)).sorted().forEach(prefs::remove);
            List<TabPane> tabpanes = new LinkedList(BktailTab.getTabPanes());
            for( int i = 0; i < tabpanes.size(); i++)
            {
                TabPane tabPane = tabpanes.get(i);
                for( int t = 0; t < tabPane.getTabs().size(); t++)
                {
                    BktailTab tab = (BktailTab) tabPane.getTabs().get(t);
                    LogfileEvent event = tab.getLogviewerController().getEvent();
                    System.out.println("window #"+i+" tab #"+t+" file "+event.getName());
                    String name  = WINDOW+"_"+i+"&"+TAB+"_"+t+"."+NAME;
                    String path = WINDOW+"_"+i+"&"+TAB+"_"+t+"."+PATH;
                    String checked = WINDOW+"_"+i+"&"+TAB+"_"+t+"."+CHECKED;
                    prefs.put(name, event.getName());
                    prefs.put(path, event.getPath().toString());
                    prefs.put(checked, String.valueOf(tab.isChecked()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
