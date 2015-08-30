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
import de.bkusche.bktail2.logfilehandler.impl.LogfileHandlerImpl;
import javafx.application.Platform;
import javafx.collections.ModifiableObservableListBase;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

@SuppressWarnings({"rawtypes","unchecked", "restriction"})
public class LogviewerController implements I_LogfileEventListener{

	private static final String EMPTY = "";
	private static final int MAXLINESTOREAD = 5000;
	private static final int RELOADTHRESHOLD = 5000;
	
	@FXML ScrollPane scrollPane;	
	@FXML ListView logContent;
	
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
	
	public LogviewerController() {
		
		logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addLogfileEventListener(this);
		executorService = Executors.newFixedThreadPool(3);
		content = new ArrayList<>();
		reading = new AtomicBoolean(false);
		running = new AtomicBoolean(true);
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
				if( index < MAXLINESTOREAD )
					return content.get(index);
				else
					return content.get(index%content.size());
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
	
	private void load(){
		
		final long from = first > MAXLINESTOREAD? first-MAXLINESTOREAD: 0;
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