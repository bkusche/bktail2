package de.bkusche.bktail2.gui.jfx;

import java.awt.ScrollPane;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	final int maxLinesToRead = 5000;
	final int reloadThreshold = 2000;
	
	@FXML ScrollPane scrollPane;	
	@FXML ListView logContent;
	
	private I_LogfileHandler logfileHandler;
	
	private ExecutorService executorService;
	private int first;
	private int last;
	private LogfileEvent event;
	private List<String> content;
	private ObservableList<String> observableList;
	
	public LogviewerController() {
		
		logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addLogfileEventListener(this);
		executorService = Executors.newCachedThreadPool();
		content = new ArrayList<>();
	}
	
	public void init( File logfile ){
		logfileHandler.addFileToWatch(logfile);
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		observableList = new ModifiableObservableListBase<String>() {

			@Override public String get(int index) {
				if( content.size() == 0 ) return null;
				if( index < maxLinesToRead )
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
					Thread.sleep(50L);
				} catch (Throwable e) {}	
			}
		});
		
		//
		// reloading triggered by scrolling 
		executorService.execute(() ->{
			int prevFirst = 0;
			while(true){
				try {
					if( first - prevFirst > reloadThreshold || prevFirst - first > reloadThreshold ){
						load();
						prevFirst = first;
					}
						
					Thread.sleep(60L);
				} catch (Throwable e) {}	
			}
		});
	}
	

	@Override public void onCreate(final LogfileEvent event) {
		this.event = event;
		load();
		Platform.runLater( () -> {
			observableList.add("ADD");
		});
	}

	@Override public void onModify(LogfileEvent event) {
		this.event = event;
		load();
		Platform.runLater( () -> {
			observableList.add("MOD");
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
		
		long from = first > reloadThreshold? first-reloadThreshold: 0;
		long to = event.getLines() > first+maxLinesToRead? first+maxLinesToRead : event.getLines(); 
		
		Platform.runLater( () -> {
			
			long start = System.currentTimeMillis();
			
			content = logfileHandler.readLines(new LogfileReadInput(
					event.getPath(), 
					from,
					to,
					event.getLines()));
			
			System.out.println( "loading first: "+first+" : last: "+last
					+" - from: "+from+" : to: "+to+" in "+(System.currentTimeMillis()-start)+" ms");
			
		});
	}
}