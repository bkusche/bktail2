/**
 * 
 */
package de.bkusche.bktail2.logfilehandler;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author bjornkusche
 *
 */
public enum S_LogfileHandler {

	INSTANCE;

	private ExecutorService service;
	private List<I_LogfileEventListener> logfileEventListeners;
	private S_LogfileHandler() {
		service = Executors.newCachedThreadPool();
		logfileEventListeners = new LinkedList<>();
	}

	public static S_LogfileHandler getInstance() {
		return INSTANCE;
	}

	public void addFileToWatch(File filepath) {
		//TODO implement evaluations
		service.execute( () -> {
			try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

				Path logfile = Paths.get(filepath.getParentFile().getCanonicalPath());
				
				logfile.register(watchService, 
						StandardWatchEventKinds.ENTRY_CREATE, 
						StandardWatchEventKinds.ENTRY_MODIFY,
						StandardWatchEventKinds.ENTRY_DELETE);
				
				while (true) {
					WatchKey wk = watchService.take();
					if( wk == null ) {
						Thread.sleep(100L);
						continue;
					}
					wk.pollEvents().forEach(we ->{
						if( we.context() instanceof Path){
							Path p = (Path) we.context();
							if( p.getFileName().toString().equals(filepath.getName())){
								
								if( we.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)){
									logfileEventListeners.forEach( l -> l.onCreate(new LogfileEvent(filepath.getName(), p, 0) ) );
								} else if( we.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)){
									logfileEventListeners.forEach( l -> l.onModify(new LogfileEvent(filepath.getName(), p, 0) ) );
								} else if( we.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)){
									logfileEventListeners.forEach( l -> l.onDelete(new LogfileEvent(filepath.getName(), p, 0) ) );
								}
							}
						}
					});
					wk.reset();
				}
			} catch (Throwable e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		});
	}
	
	public void addLogfileEventListener( I_LogfileEventListener l ){
		logfileEventListeners.add(l);
	}
	
	public void removeLogfileEventListener( I_LogfileEventListener l ){
		logfileEventListeners.remove(l);
	}
}