package de.bkusche.bktail2.gui.jfx;

import java.awt.ScrollPane;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
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
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

@SuppressWarnings({"rawtypes","unchecked", "restriction"})
public class LogviewerController implements I_LogfileEventListener, Initializable{

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
	private final AtomicBoolean reading;
	
	public LogviewerController() {
		
		logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addLogfileEventListener(this);
		executorService = Executors.newFixedThreadPool(5);
		content = new ArrayList<>();
		reading = new AtomicBoolean(false);
	}
	
	public void init( File logfile ){
		logfileHandler.addFileToWatch(logfile);
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

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
		
		logContent.setItems(observableList);
		
		//
		// monitoring the view position
		executorService.execute(() ->{
			while(true){
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
			while(true){
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