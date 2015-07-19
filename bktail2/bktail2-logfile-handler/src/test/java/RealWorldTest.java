import java.io.File;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.impl.S_LogfileHandlerImpl;

public class RealWorldTest {

	public static void main(String[] args) {
		final String filepath = "/opt/jboss/standalone/log/server.log";
		final int lines = 80;
		I_LogfileHandler logfileHandler = S_LogfileHandlerImpl.getInstance();
		
		System.out.println("using testfile: "+filepath);
		logfileHandler.addFileToWatch(new File( filepath ));
		logfileHandler.addLogfileEventListener(new I_LogfileEventListener() {
			@Override
			public void onModify(LogfileEvent event) {
				System.out.println("onModify: "+event.getName()
				+" lines: "+event.getLines()
				+" delta: "+event.getDelta());
				
				//
				//reading the last 80 lines
				logfileHandler.readLines(new LogfileReadInput(
						event.getPath(), 
						event.getLines()-event.getDelta(), 
						event.getDelta(), 
						event.getLines())).forEach(l -> System.out.println(l));
			}
			
			@Override
			public void onDelete(LogfileEvent event) {
				System.out.println("onDelete: "+event.getName());
			}
			
			@Override
			public void onCreate(LogfileEvent event) {
				System.out.println("onCreate:"+event.getName()+" lines: "+event.getLines());
				//
				//reading the last 80 lines
				logfileHandler.readLines(new LogfileReadInput(
						event.getPath(), 
						event.getLines()-lines, 
						lines, 
						event.getLines())).forEach(l -> System.out.println(l));
			}
		});
	}

}
