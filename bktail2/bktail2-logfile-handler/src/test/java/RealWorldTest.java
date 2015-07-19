import java.io.File;

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.S_LogfileHandler;

public class RealWorldTest {

	public static void main(String[] args) {
		String filepath = "/opt/jboss/standalone/log/server.log";
		S_LogfileHandler logfileHandler = S_LogfileHandler.getInstance();
		
		System.out.println("using testfile: "+filepath);
		logfileHandler.addFileToWatch(new File( filepath ));
		logfileHandler.addLogfileEventListener(new I_LogfileEventListener() {
			@Override
			public void onModify(LogfileEvent event) {
				System.out.println("onModify: "+event.getName()+" lines: "+event.getLength());
			}
			
			@Override
			public void onDelete(LogfileEvent event) {
				System.out.println("onDelete: "+event.getName());
			}
			
			@Override
			public void onCreate(LogfileEvent event) {
				System.out.println("onCreate:"+event.getName()+" lines: "+event.getLength());
			}
		});
	}

}
