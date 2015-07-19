/**
 * 
 */
package de.bkusche.bktail2.logfilehandler.impl;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;

/**
 * @author bjornkusche
 * 
 */
public enum S_LogfileHandlerImpl implements I_LogfileHandler{

	INSTANCE;

	private ExecutorService service;
	private List<I_LogfileEventListener> logfileEventListeners;
	private List<LogfileEvent> logfileEvents;
	
	private Function<File, Long> countLines = t -> {
		try (Stream<String> stream = Files.lines(t.toPath())) {
			return stream.count();
		} catch(Throwable e){}
		return 0L;
	};
	
	private Predicate<File> containsLogFile = t -> {
		return logfileEvents.stream()
			.filter( f -> f.getName().equals(t.getName() ) )
			.filter( f -> f.getPath().toString().equals(t.toPath().toString()))
			.count() > 0;
	};
	
	private Function<File, LogfileEvent> getLogfileEvent = t -> {
		return logfileEvents.stream()
			.filter( f -> f.getName().equals(t.getName() ) )
			.filter( f -> f.getPath().toString().equals(t.toPath().toString()))
			.findFirst().get();
	};
	
	private S_LogfileHandlerImpl() {
		service = Executors.newCachedThreadPool();
		logfileEventListeners = new LinkedList<>();
		logfileEvents = new LinkedList<>();
	}

	public static S_LogfileHandlerImpl getInstance() {
		return INSTANCE;
	}

	@Override public void addFileToWatch(File filepath) {
		//TODO implement evaluations
		service.execute( () -> {
			while( true ){
				try {
					if (!containsLogFile.test(filepath) && filepath.exists()) {
						LogfileEvent logfileEvent = new LogfileEvent( filepath.getName(), 
								filepath.toPath(),
								countLines.apply(filepath));
						logfileEventListeners.forEach(l -> l.onCreate(
								logfileEvent));
						logfileEvents.add(logfileEvent);
					} else if (containsLogFile.test(filepath) && filepath.exists()) {
						LogfileEvent logfileEvent = getLogfileEvent.apply(filepath);
						long lines = countLines.apply(filepath);
						if( logfileEvent.getLines() != lines ){
							logfileEvent.setLines(lines);
							logfileEventListeners.forEach(l -> l.onModify(
									logfileEvent));
						}
					} else if (containsLogFile.test(filepath) && !filepath.exists()) {
						LogfileEvent logfileEvent = getLogfileEvent.apply(filepath);
						logfileEvents.remove(logfileEvent);
						logfileEventListeners.forEach(l ->l.onDelete(logfileEvent));
					}
						
					Thread.sleep(50L);
				} catch (Throwable e) {
					// TODO: handle exception
				}
			}
		});
	}
	
	@Override public List<String> readLines( LogfileReadInput logfileReadInput ){
		List<String> lineRange = new LinkedList<>();
		try (Stream<String> stream = Files.lines(logfileReadInput.getPath())) {
			stream.skip(logfileReadInput.getFrom()).limit(logfileReadInput.getTo())
				.forEach(l -> lineRange.add(l));
		} catch (Throwable e) {
			// TODO: handle exception
		}
		return lineRange;
	}
	
	@Override public void addLogfileEventListener( I_LogfileEventListener l ){
		logfileEventListeners.add(l);
	}
	
	@Override public void removeLogfileEventListener( I_LogfileEventListener l ){
		logfileEventListeners.remove(l);
	}
}