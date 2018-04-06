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
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.LogfileSearchInput;
import de.bkusche.bktail2.logfilehandler.impl.LogfileHandlerImpl;

/**
 * @author bkusche
 *
 */
public class LogfileHandlerTest {

	private static final String LOGFILENAME = "testfile.log";
	private static final String FILEPATH = /*System.getProperty("java.io.tmpdir")+*/LOGFILENAME;//travis can't handle temp dirs :-/
	private static final int maxLines = 10;
	private Logger log = Logger.getLogger(LogfileHandlerTest.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		try {
			Files.delete(Paths.get(FILEPATH));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Before
	public void setUp() throws Exception {
		try {
			Files.delete(Paths.get(FILEPATH));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile(FILEPATH);
		fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();
	
		//add appender to any Logger (here is root)
		Logger.getRootLogger().addAppender(fa);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void normal_logging() throws Exception{
		final int[] actualLine = new int[1];
		
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		System.out.println("using testfile: "+FILEPATH);
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.addLogfileEventListener(new I_LogfileEventListener() {
			@Override
			public void onModify(LogfileEvent event) {
				assertNotNull(event);
				assertEquals(LOGFILENAME,event.getName());
				assertEquals(actualLine[0], event.getLines());
				System.out.println("onModify: "+event.getName());
			}
			
			@Override
			public void onDelete(LogfileEvent event) {
				assertNotNull(event);
				assertEquals(LOGFILENAME,event.getName());
				assertEquals(maxLines, event.getLines());
				System.out.println("onDelete: "+event.getName());
			}
			
			@Override
			public void onCreate(LogfileEvent event) {
				assertNotNull(event);
				assertEquals(LOGFILENAME,event.getName());
				System.out.println("onCreate:"+event.getName());
			}
		});
		
		System.out.println("writing");
		try {
			for( int i = 0; i < maxLines; i++){
				actualLine[0] = i+1;
				log.info("TEST_LINE_"+actualLine);
				Thread.sleep(1000L);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		System.out.println("deleting");
		Thread.sleep(1000L);
		Files.delete(Paths.get(FILEPATH));
		Thread.sleep(1000L);
		logfileHandler.stopObserving();
	}

	/**
	 * this should never happen... but just in case it does
	 */
	@Test(expected=NullPointerException.class)
	public void null_parameter_addFile() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(null);
	}
	
	/**
	 * this should never happen... but just in case it does
	 */
	@Test(expected=NullPointerException.class)
	public void null_parameter_addLogfileEventListener() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.addLogfileEventListener(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void null_parameter_removeLogfileEventListener() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.removeLogfileEventListener(null);
	}
	
	@Test
	public void removeLogfileEventListener() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.removeLogfileEventListener(new I_LogfileEventListener() {
			@Override public void onCreate(LogfileEvent event) {}
			@Override public void onModify(LogfileEvent event) {}
			@Override public void onDelete(LogfileEvent event) {}
		});
	}
	
	@Test
	public void no_eventListener() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		for( int i = 0; i < maxLines; i++)
			log.info("TEST_LINE_"+i);
	}
	
	@Test(expected=NullPointerException.class)
	public void null_parameter_readLines() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.readLines(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void null_parameter_readLinesPath() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.readLines(new LogfileReadInput());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void illegalRange_readLines() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		logfileHandler.readLines(new LogfileReadInput(new File( FILEPATH ).toPath(),10L,0L));
	}
	
	@Test
	public void readLines() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		try {
			for( int i = 0; i < maxLines; i++){
				log.info("TEST_LINE_"+i);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		logfileHandler.readLines(new LogfileReadInput(new File( FILEPATH ).toPath(),1L,2L));
	}
	
	@Test
	public void normal_search() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		Thread.sleep(1000L);
		try {
			for( int i = 0; i < maxLines; i++){
				log.info("TEST_LINE_"+i);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Integer> result = logfileHandler.searchInLogFile( new LogfileSearchInput(new File( FILEPATH ).toPath(), "TEST_LINE_1", false));
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
	}
	
	@Test
	public void normal_search_ignoreCase() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		Thread.sleep(1000L);
		try {
			for( int i = 0; i < maxLines; i++){
				log.info("TEST_LINE_"+i);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
		List<Integer> result = logfileHandler.searchInLogFile( new LogfileSearchInput(new File( FILEPATH ).toPath(), "TEST_LINE_1", true));
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
	}
	
	@Test(expected=NullPointerException.class)
	public void nulll_parameter_search() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		Thread.sleep(1000L);
		logfileHandler.searchInLogFile(null);
	}
	
	@Test(expected=NullPointerException.class)
	public void nulll_parameterPath_search() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		Thread.sleep(1000L);
		logfileHandler.searchInLogFile(new LogfileSearchInput(null, null, false));
	}
	
	@Test(expected=NullPointerException.class)
	public void nulll_parameterPattern_search() throws Exception{
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		logfileHandler.addFileToObserve(new File( FILEPATH ));
		Thread.sleep(1000L);
		logfileHandler.searchInLogFile(new LogfileSearchInput(new File( FILEPATH ).toPath(), null, false));
	}
}
