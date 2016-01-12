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

import java.awt.ScrollPane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.LogfileSearchInput;
import de.bkusche.bktail2.logfilehandler.impl.LogfileHandlerImpl;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * @author bkusche
 *
 */
@SuppressWarnings({"rawtypes","unchecked", "restriction"})
public class LogviewerController implements I_LogfileEventListener{

	private static final String EMPTY = "";
	private static final int MAXLINESTOREAD = 5000;
	private static final int RELOADTHRESHOLD = 5000;
	
	@FXML AnchorPane anchor;
	@FXML ScrollPane scrollPane;	
	@FXML ListView logContent;
	@FXML AnchorPane extendableSearchPane;
	@FXML Label searchHitLabel;
	@FXML TextField searchFiled;

	private final I_LogfileHandler logfileHandler;
	
	private final ExecutorService executorService;
	private int first;
	private int last;
	private LogfileEvent event;
	private List<String> content;
	private ObservableList<String> observableList;
	private Theme theme;
	private List<Highlighting> highlightings;
	private final AtomicBoolean reading;
	private final AtomicBoolean running;
	private Rectangle clipRect;
	private double heightInitial;
	private List<Integer> searchHitList;
	private int searchHitPos;
	private long from;
	private boolean ignoreCase;
	
	public LogviewerController() {
		
		logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addLogfileEventListener(this);
		executorService = Executors.newFixedThreadPool(3);
		content = new ArrayList<>();
		reading = new AtomicBoolean(false);
		running = new AtomicBoolean(true);
		ignoreCase = false;
	}
	
	public void init( File logfile, Theme theme, List<Highlighting> highlightings ){
		this.theme = theme;
		this.highlightings = highlightings;
		logfileHandler.addFileToObserve(logfile);
	}
	
	
	@FXML void initialize() {

		observableList = new ModifiableObservableListBase<String>() {

			@Override public String get(int index) {
				if( content == null || content.size() == 0 ) return EMPTY;
//				System.out.println("index: "+index+" -> calcIndex: "+(((index- (from%content.size()))%content.size())));
				int i = (int) (((index- (from%content.size()))%content.size()));
				return content.get(i < 0 ? 0 : i);
			}

			@Override public int size() {
				return (int) (event == null ? 0 : event.getLines());
			}

			//Methods remain unimplemented. We're only interested in the implementations event handling
			@Override protected void doAdd(int index, String element) {}
			@Override protected String doSet(int index, String element) {return null;}
			@Override protected String doRemove(int index) {return null;}
		};
		
		logContent.setCellFactory(p -> {
			ListCell<String> cell = new ListCell<String>(){
				@Override protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setStyle("-fx-background-color: "+theme.getBackgroundColor().toString().replace("0x", "#"));
					setTextFill(theme.getForegroundColor());
					if( empty ) return;
					setText(item);
		            setGraphic(null);
		            for( Highlighting h : highlightings ){
		            	if( !item.contains(h.getText())) continue;
		            	setStyle("-fx-background-color: "+h.backgroundColorProperty().getValue().toString().replace("0x", "#"));
		            	setTextFill(h.textColorProperty().getValue());
		            	return;
		            }
				}
			};
			return cell;
		});
		logContent.setStyle("-fx-font-family: monospace;"
				+ "-fx-font-size: 11;");
		logContent.setItems(observableList);
		
		//
		// monitoring the view position
		executorService.execute(() ->{
			while(running.get()){
				try {
					ListViewSkin<?> ts = (ListViewSkin<?>) logContent.getSkin();
			        VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
			        first = vf.getFirstVisibleCell().getIndex();
			        last = vf.getLastVisibleCell().getIndex();
					Thread.sleep(100L);
				} catch (Throwable e) {}	
			}
		});
		
		//
		// reloading triggered by scrolling 
		executorService.execute(() ->{
			int prevFirst = 0;
			while(running.get()){
				try {
					if( !reading.get() && (first - prevFirst > RELOADTHRESHOLD || prevFirst - first > RELOADTHRESHOLD) ){
						load();
						prevFirst = first;
						
						//force reloading - hack
						Platform.runLater( () -> {
							observableList.remove(0);
						});
					}
						
					Thread.sleep(100L);
				} catch (Throwable e) {}	
			}
		});
		
		//
		// fancy search field stuff
		double widthInitial = extendableSearchPane.prefWidthProperty().doubleValue();
		heightInitial = extendableSearchPane.prefHeightProperty().doubleValue();
		clipRect = new Rectangle();
		clipRect.setWidth(widthInitial);
		clipRect.setHeight(0);
		clipRect.translateYProperty().set(heightInitial);
		extendableSearchPane.setClip(clipRect);
		
		extendableSearchPane.translateYProperty().set(-heightInitial);
		extendableSearchPane.prefHeightProperty().set(0);
		//
		// requesting focus
		anchor.requestFocus();
	}
	

	@Override public void onCreate(final LogfileEvent event) {
		this.event = event;
		load();
		//force reloading - hack
		Platform.runLater( () -> {
			observableList.remove(0);
		});
	}

	@Override public void onModify(LogfileEvent event) {
		this.event = event;
		load();
		//force reloading - hack
		Platform.runLater( () -> {
			observableList.remove(0);
		});
	}

	@Override public void onDelete(LogfileEvent event) {
		this.event = null;
		this.first = 0;
		this.last = 0;

		Platform.runLater( () -> {
			observableList.remove(0);
		});
		return;
	}

	public void dispose(){
		running.set(false);
		logfileHandler.stopObserving();
	}
	
	@FXML void onKeyTyped(KeyEvent event) {
		try {
			if( new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)
					|| event.isShortcutDown() && event.getCharacter().equals("f"))
				toggleExtendableSearch();
		} catch (Throwable e) {
			// 
		}
    }
	
	@FXML void onSearchKeyTyped(KeyEvent ke) {
		if( ke.getCode().equals(KeyCode.ENTER)){
			performSearch();
		}
    }

	@FXML void previousSearchEntry(ActionEvent event) {
    	if( searchHitPos == 0 ) 
    		return;
    	
    	for( int i = searchHitList.size()-1; i >= 0; i-- ){
    		if( searchHitList.get(i) <= first ){
    			searchHitPos = i;
    			break;
    		}
    	}
    	
    	selectSearchHit(searchHitList.get(searchHitPos));
    	searchHitLabel.setText(searchHitPos+1+" of "+searchHitList.size()+" matches");
    }

    @FXML void nextSearchEntry(ActionEvent event) {
    	if( searchHitPos >= searchHitList.size()-1 ) 
    		return;
		
    	for( int i = 0; i < searchHitList.size(); i++ ){
    		if( searchHitList.get(i) > first ){
    			searchHitPos = i;
    			break;
    		}
    	}
    	searchHitPos++;
    	selectSearchHit(searchHitList.get(searchHitPos));
    	searchHitLabel.setText(searchHitPos+1+" of "+searchHitList.size()+" matches");
    }

    @FXML void hideSearchArea(ActionEvent event) {
    	toggleExtendableSearch();
    }

    @FXML void toggleIgnoreCase(ActionEvent event) {
    	ignoreCase = !ignoreCase;
    	performSearch();
    }

    private void performSearch() {
    	searchHitList = logfileHandler.searchInLogFile(new LogfileSearchInput(event.getPath(), 
				searchFiled.getText(), ignoreCase));
		
		searchHitPos = 0; //reset
		if( searchHitList.isEmpty() ){
			searchHitLabel.setText("No result");
			return;
		}
		else {
			nextSearchEntry(null);
		}	
	}
    
    private void selectSearchHit( int searchHitPos ){
    	System.out.println("searchHitPos: "+searchHitPos);
    	Platform.runLater(()->{
    		logContent.scrollTo(searchHitPos);
    		logContent.getSelectionModel().select(searchHitPos);
    	});
    }
	
    //
    // kindly provided by: http://stevenschwenke.de/extendableSearchPaneInJavaFX
	private void toggleExtendableSearch() {
		clipRect.setWidth(extendableSearchPane.getWidth());
		Timeline timeline = new Timeline();
		if (clipRect.heightProperty().get() != 0) {
			//
			// Animation of sliding the search pane up, implemented via clipping.
			final KeyValue kvUp1 = new KeyValue(clipRect.heightProperty(), 0);
			final KeyValue kvUp2 = new KeyValue(clipRect.translateYProperty(), heightInitial);
			//
			// The actual movement of the search pane. This makes the table grow.
			final KeyValue kvUp4 = new KeyValue(extendableSearchPane.prefHeightProperty(), 0);
			final KeyValue kvUp3 = new KeyValue(extendableSearchPane.translateYProperty(), -heightInitial);
			final KeyFrame kfUp = new KeyFrame(Duration.millis(200), kvUp1, kvUp2, kvUp3, kvUp4);
			
			timeline.getKeyFrames().add(kfUp);
			anchor.requestFocus();
		} else {
			//
			// Animation for sliding the search pane down. No change in size,
			// just making the visible part of the pane bigger.
			final KeyValue kvDwn1 = new KeyValue(clipRect.heightProperty(), heightInitial);
			final KeyValue kvDwn2 = new KeyValue(clipRect.translateYProperty(), 0);
			//
			// Growth of the pane.
			final KeyValue kvDwn4 = new KeyValue(extendableSearchPane.prefHeightProperty(), heightInitial);
			final KeyValue kvDwn3 = new KeyValue(extendableSearchPane.translateYProperty(), 0);
			final KeyFrame kfDwn = new KeyFrame(Duration.millis(200), createBouncingEffect(heightInitial), kvDwn1, kvDwn2, kvDwn3, kvDwn4);
			
			timeline.getKeyFrames().add(kfDwn);
			searchFiled.requestFocus();
		}
		timeline.play();
	}
	
	private EventHandler<ActionEvent> createBouncingEffect(double height) {
		final Timeline timelineBounce = new Timeline();
		timelineBounce.setCycleCount(2);
		timelineBounce.setAutoReverse(true);
		final KeyValue kv1 = new KeyValue(clipRect.heightProperty(), (height - 15));
		final KeyValue kv2 = new KeyValue(clipRect.translateYProperty(), 15);
		final KeyValue kv3 = new KeyValue(extendableSearchPane.translateYProperty(), -15);
		final KeyFrame kf1 = new KeyFrame(Duration.millis(100), kv1, kv2, kv3);
		timelineBounce.getKeyFrames().add(kf1);
		//
		// Event handler to call bouncing effect after the scroll down is finished.
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				timelineBounce.play();
			}
		};
		return handler;
	}
	
	
	//
	// handles log file loading
	private void load(){
		
		from = first > MAXLINESTOREAD? first-MAXLINESTOREAD: 0;
		final long to = event.getLines() > first+MAXLINESTOREAD? first+MAXLINESTOREAD : event.getLines(); 
	
		try {
			reading.set(true);
			final long start = System.currentTimeMillis();
			
			content = logfileHandler.readLines(new LogfileReadInput(
					event.getPath(), 
					from,
					to,
					event.getLines()));
			
			System.out.println( "loading first: "+first+" : last: "+last
					+" - from: "+from+" : to: "+to+" = "+(to-from)+" in "+(System.currentTimeMillis()-start)+" ms");
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			executorService.execute(() ->{
				try { Thread.sleep(500L); } catch (Throwable e) {}
				reading.set(false);
			});
		}
	}
	
}