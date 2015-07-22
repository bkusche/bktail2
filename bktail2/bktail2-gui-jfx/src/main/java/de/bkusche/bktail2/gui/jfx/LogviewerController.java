package de.bkusche.bktail2.gui.jfx;

import java.awt.ScrollPane;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.impl.S_LogfileHandlerImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

@SuppressWarnings({"rawtypes","unchecked", "restriction"})
public class LogviewerController implements I_LogfileEventListener, Initializable{

	final String filepath = "/opt/jboss/standalone/log/server.log";
	final int maxLinesToRead = 250;
	final int reloadThreshold = 125;
	
	@FXML ScrollPane scrollPane;
//	@FXML TextArea logContent;
	
	@FXML ListView logContent;
	
	private I_LogfileHandler logfileHandler;
	
	private ScheduledExecutorService executorService;
	private int first;
	private int last;
	private double rowHeight;
	
	private Runnable b = () -> {
		
	};
	private LogfileEvent event;
	
	public LogviewerController() {
		
		logfileHandler = S_LogfileHandlerImpl.getInstance();
		logfileHandler.addFileToWatch((new File( filepath )));
		logfileHandler.addLogfileEventListener(this);
		
		executorService = Executors.newScheduledThreadPool(2);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		logContent.setItems(FXCollections.observableList(new ArrayList<>()));
		//
		// HACK: faking an entry to obtain the rowHeight
		logContent.getItems().add("start");
		
		//
		// monitoring the view position
		executorService.execute(() ->{
			while(true){
				try {
					ListViewSkin<?> ts = (ListViewSkin<?>) logContent.getSkin();
			        VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
			        first = vf.getFirstVisibleCell().getIndex();
			        last = vf.getLastVisibleCell().getIndex();
			        rowHeight = vf.getFirstVisibleCell().getHeight();
//			        System.out.println("##### Scrolling first "+first+" last "+last);
					Thread.sleep(10L);
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
						
					Thread.sleep(20L);
				} catch (Throwable e) {}	
			}
		});
		
		//
		//removing fake line from above
		logContent.getItems().clear();
	}
	

	@Override public void onCreate(final LogfileEvent event) {
		this.event = event;
//		System.out.println( "lines: "+event.getLines() );
		load();
	}

	@Override public void onModify(LogfileEvent event) {
		this.event = event;
		load();
	}

	@Override public void onDelete(LogfileEvent event) {
		this.event = null;
		this.first = 0;
		this.last = 0;
		load();
	}
	
	
	private void load(){
//		System.out.println( first+" : "+last);
		Platform.runLater( () -> {
			
			if( event == null ){
				if( !logContent.getItems().isEmpty() ){
					logContent.getItems().clear();
				}
				return;
			}
			
			long from = first > reloadThreshold? first-reloadThreshold: 0;
			long to = event.getLines() > first+maxLinesToRead? first+maxLinesToRead : event.getLines(); 
			
			List<String> content = logfileHandler.readLines(new LogfileReadInput(
					event.getPath(), 
					from,
					to,
					event.getLines()));
			
			//
			// remove the old content from the view
			logContent.getItems().clear();
			
			if( first > reloadThreshold ){
				for( int i = 0; i < from; i++)
					logContent.getItems().add(new String());
			}
			
			content.forEach(l -> logContent.getItems().add(l+"\n"));
			
			if( last - reloadThreshold < event.getLines() ){
				for( long i = to; i < event.getLines(); i++)
					logContent.getItems().add(new String());
			}
		});
	}
}