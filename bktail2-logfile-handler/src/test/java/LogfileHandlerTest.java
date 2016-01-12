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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.impl.LogfileHandlerImpl;

/**
 * @author bkusche
 *
 */
public class LogfileHandlerTest {

	
	private String filepath = System.getProperty("java.io.tmpdir")+"testfile.log";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		try {
			Files.delete(Paths.get(filepath));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception{
//		I_LogfileHandler logfileHandler = S_LogfileHandlerImpl.getInstance();
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		System.out.println("using testfile: "+filepath);
		logfileHandler.addFileToObserve(new File( filepath ));
		logfileHandler.addLogfileEventListener(new I_LogfileEventListener() {
			@Override
			public void onModify(LogfileEvent event) {
				System.out.println("onModify: "+event.getName());
			}
			
			@Override
			public void onDelete(LogfileEvent event) {
				System.out.println("onDelete: "+event.getName());
			}
			
			@Override
			public void onCreate(LogfileEvent event) {
				System.out.println("onCreate:"+event.getName());
			}
		});
		Thread.sleep(1000L);
		Files.createFile(Paths.get(filepath));
		Files.setLastModifiedTime(Paths.get(filepath), FileTime.fromMillis(System.currentTimeMillis()));
		System.out.println("writing");
		Thread.sleep(1000L);
		for( int i = 0; i < 5; i++){
			Files.write(Paths.get(filepath), (i+"\n").getBytes());
			Thread.sleep(5000L);
		}
		
		System.out.println("deleting");
		Thread.sleep(1000L);
		Files.delete(Paths.get(filepath));
		Thread.sleep(1000L);
		
	}

}
