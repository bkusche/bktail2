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
package de.bkusche.bktail2.logfilehandler.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.LogfileSearchInput;

/**
 * @author bkusche
 * 
 */
public class LogfileHandlerImpl implements I_LogfileHandler{

	final static int CR = 10;
	
	private final ExecutorService service;
	private final List<I_LogfileEventListener> logfileEventListeners;
	private List<LogfileEvent> logfileEvents;
	private final List<String> lineRange;
	private long fileSize = 0;
	private final AtomicBoolean observing;
	
	/**
	 * Count lines of a file
	 */
	private Function<File, Long> countLines = t -> {
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(t))) {
			byte[] buffer = new byte[8192];
			long lines = 0;
		    int read;

		    while ((read = r.read(buffer)) != -1)
		        for (int i = 0; i < read; i++) {
		            if (buffer[i] == CR) lines++;
		    }

			return lines;
		} catch (Throwable e) {}
		return 0L;
	};
	
	/**
	 * determines whether the size of a file has changed
	 */
	private Predicate<File> fileSizeChanged = f -> {
		try {
			long size = Files.size(f.toPath());
			if( size != fileSize ){
				fileSize = size;
				return true;
			}
		} catch (Throwable e) {}
		return false;
	};
	
	/**
	 * determines whether the reference of a file is stored in the local repository
	 */
	private Predicate<File> containsLogFile = t -> {
		return logfileEvents.stream()
			.filter( f -> f.getName().equals(t.getName() ) )
			.filter( f -> f.getPath().toString().equals(t.toPath().toString()))
			.count() > 0;
	};
	
	/**
	 * yields the reference of a file and it's meta data from the local repository
	 */
	private Function<File, LogfileEvent> getLogfileEvent = t -> {
		return logfileEvents.stream()
			.filter( f -> f.getName().equals(t.getName() ) )
			.filter( f -> f.getPath().toString().equals(t.toPath().toString()))
			.findFirst().get();
	};
	
	public LogfileHandlerImpl() {
		service = Executors.newCachedThreadPool();
		logfileEventListeners = new LinkedList<>();
		logfileEvents = new LinkedList<>();
		lineRange = new ArrayList<>();
		observing = new AtomicBoolean(true);
	}


	@Override public void addFileToObserve(File filepath) {
		if( filepath == null ) throw new NullPointerException("filepath must not be null!");
		//
		//file monitor thread
		service.execute( () -> {
			while( observing.get() ){
				try {
					//
					//detect file creation
					if (!containsLogFile.test(filepath) && filepath.exists()) {
						LogfileEvent logfileEvent = new LogfileEvent( filepath.getName(), 
								filepath.toPath(),
								countLines.apply(filepath));
						logfileEventListeners.forEach(l -> l.onCreate(
								logfileEvent));
						logfileEvents.add(logfileEvent);
						
					//
					//detect file modification 	
					} else if (containsLogFile.test(filepath) && filepath.exists()) {
						if( fileSizeChanged.test(filepath) ){
							LogfileEvent logfileEvent = getLogfileEvent.apply(filepath);
							long lines = countLines.apply(filepath);
							if( logfileEvent.getLines() != lines ){
								logfileEvent.setLines(lines);
								logfileEventListeners.forEach(l -> l.onModify(
										logfileEvent));
							}
						}
						
					//
					//detect file deletion	
					} else if (containsLogFile.test(filepath) && !filepath.exists()) {
						LogfileEvent logfileEvent = getLogfileEvent.apply(filepath);
						logfileEvents.remove(logfileEvent);
						logfileEventListeners.forEach(l ->l.onDelete(logfileEvent));
					}
						
					Thread.sleep(1000L);
				} catch (Throwable e) {
					//
				}
			}
		});
	}
	
	@Override public void stopObserving() {
		observing.set(false);
	}
	
	@Override public List<String> readLines( LogfileReadInput logfileReadInput ){
		if( logfileReadInput == null ) throw new NullPointerException("logfileReadInput must not be null!");
		if( logfileReadInput.getPath() == null ) throw new NullPointerException("logfileReadInput.getPath must not be null!");
		if( logfileReadInput.getTo() < logfileReadInput.getFrom() ) throw new IllegalArgumentException("to must not be smaller than from!");
		
//		System.out.println("loading "+logfileReadInput.getFrom()+" to "+logfileReadInput.getTo());
		//
		//reusing lineRange list (reference) to avoid unnecessary 0..n references & gc usage  
		lineRange.clear();
		long limit = logfileReadInput.getTo()-logfileReadInput.getFrom();	
		boolean useJava6 = true;
//		try (Stream<String> stream = Files.lines(logfileReadInput.getPath())) {
//			stream.skip(logfileReadInput.getFrom()).limit(limit)
//				.forEach(lineRange::add); //TODO evaluate performance especially with large files
//		} catch (Throwable e) {
//			// 
//		}
		if( useJava6 )
			return readLinesJava6Way(logfileReadInput.getPath(), logfileReadInput.getFrom(), limit);
		else
			return readLinesJava8Way(logfileReadInput.getPath(), logfileReadInput.getFrom(), limit);

	}
	
	@Override public void addLogfileEventListener( I_LogfileEventListener l ){
		if( l == null ) throw new NullPointerException();
		logfileEventListeners.add(l);
	}
	
	@Override public void removeLogfileEventListener( I_LogfileEventListener l ){
		if( l == null ) throw new NullPointerException();
		logfileEventListeners.remove(l);
	}
	
	@Override public List<Integer> searchInLogFile( LogfileSearchInput logfileSearchInput ) {
		if(logfileSearchInput == null) throw new NullPointerException("logfileSearchInput must not be null!");
		if(logfileSearchInput.getPath() == null ) throw new NullPointerException("logfileSearchInput.getPath must not be null!");
		if(logfileSearchInput.getSearchPattern() == null ) throw new NullPointerException("logfileSearchInput.getSearchPattern must not be null!");
		
		List<Integer> resultHitList = new LinkedList<>();
		try( BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				logfileSearchInput.getPath().toFile())))){
			String line = null;
			int c = 0;
			while ((line = br.readLine()) != null) {
				if( logfileSearchInput.isIgnoreCase() ){
					if( line.toLowerCase().contains(logfileSearchInput.getSearchPattern().toLowerCase()) ){ 
						resultHitList.add(c);
					}
				}else{
					if( line.contains(logfileSearchInput.getSearchPattern()) ){ 
						resultHitList.add(c);
					}
				}
				
				c++;
			}
		} catch( Throwable e){
			//
		}
		
		return resultHitList;
	}
	
	private List<String> readLinesJava8Way(Path p, final long from, final long limit){
		try (Stream<String> stream = Files.lines(p)) {
			stream.skip(from).limit(limit)
				.forEach(lineRange::add); //TODO evaluate performance especially with large files
		} catch (Throwable e) {
			// 
		}
		return lineRange;
	}
	
	
	private List<String> readLinesJava6Way(Path p, final long from, final long limit){
		long lineStrart = 0;
		try(BufferedInputStream r = new BufferedInputStream(new FileInputStream(p.toFile()))) {	
		    final byte[] buffer = new byte[8192];
		    final int[] line = new int[1];
		    final boolean[] reading = new boolean[]{true};
		    int read = 0;
		    while ((read = r.read(buffer)) != -1 && reading[0]) {
		    	lineStrart = searchFromIndex(buffer, line, reading, read, from, lineStrart);
		    }

		} catch (Throwable e) {e.printStackTrace();}
		
		try(BufferedRandomAccessFile braf = new BufferedRandomAccessFile(p.toFile(), "r", 8192)){
			int line = 0;
			braf.seek(lineStrart+1);
			while( line < limit){
				line++;
				lineRange.add(braf.readNextLine());
			}
		} catch (Throwable e) {}
		return lineRange;
	}
	
	private final long searchFromIndex(final byte[] buffer, final int[] line, final boolean[] reading, 
			final int read, final long from, long lineStrart) {

		for (int i = 0; i < read; i++) {
			if (buffer[i] == CR) {
				line[0]++;
				if (line[0] == from) {
					reading[0] = false;
					break;
				}
			}
			lineStrart++;
		}
		return lineStrart;
	}
}