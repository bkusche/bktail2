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
import de.bkusche.bktail2.logfilehandler.impl.S_LogfileHandlerImpl;

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
		I_LogfileHandler logfileHandler = S_LogfileHandlerImpl.getInstance();
		
		System.out.println("using testfile: "+filepath);
		logfileHandler.addFileToWatch(new File( filepath ));
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
