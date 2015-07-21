package de.bkusche.bktail2.gui.jfx;

import java.awt.ScrollPane;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.javafx.scene.control.skin.ListViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.impl.S_LogfileHandlerImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.shape.Rectangle;

@SuppressWarnings({"rawtypes","unchecked"})
public class LogviewerController implements I_LogfileEventListener, Initializable{

	final String filepath = "/opt/jboss/standalone/log/server.log";
	final int lines = 80;
	
	@FXML ScrollPane scrollPane;
//	@FXML TextArea logContent;
	
	@FXML ListView logContent;
	
	private I_LogfileHandler logfileHandler;
	
	private ScheduledExecutorService executorService;
	private int first;
	private int last;
	private double rowHight;
	
	private Runnable b = () -> {
		
	};
	
	public LogviewerController() {
		
		logfileHandler = S_LogfileHandlerImpl.getInstance();
		logfileHandler.addFileToWatch((new File( filepath )));
		logfileHandler.addLogfileEventListener(this);
		
		executorService = Executors.newScheduledThreadPool(1);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		ObservableList ob = FXCollections.observableList(new ArrayList<>());
		logContent.setItems(ob);
		logContent.getItems().add("start");
		executorService.execute(() ->{
			while(true){
				try {
					ListViewSkin<?> ts = (ListViewSkin<?>) logContent.getSkin();
			        VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
			        first = vf.getFirstVisibleCell().getIndex();
			        last = vf.getLastVisibleCell().getIndex();
			        rowHight = vf.getFirstVisibleCell().getHeight();
			        //System.out.println("##### Scrolling first "+first+" last "+last);
					Thread.sleep(100L);
				} catch (Throwable e) {
					// TODO: handle exception
				}	
			}
		});
	}

	@Override
	public void onCreate(final LogfileEvent event) {
		Platform.runLater( () -> {
			logContent.getItems().clear();
			logfileHandler.readLines(new LogfileReadInput(
					event.getPath(), 
					first, 
					event.getLines() > lines? lines : event.getLines(), 
					event.getLines())).forEach(l -> logContent.getItems().add(l+"\n"));
//			//height
//			logContent.getItems().add("<html><body><div style=\"height:864px;text-align: center;\"></body></html>");
//			
			Rectangle r = new Rectangle();
			r.setHeight((event.getLines()-lines)*rowHight);
			logContent.getItems().add(r);
			logContent.getItems().add("end");
		});
		
	}

	@Override
	public void onModify(LogfileEvent event) {
		// TODO trigger reload to obtain the new line number value
		// TODO modify the rectangle shape 
		
	}

	@Override
	public void onDelete(LogfileEvent event) {
		// TODO clear the content
		
	}
	
}