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

import de.bkusche.bktail2.logfilehandler.I_LogfileEventListener;
import de.bkusche.bktail2.logfilehandler.I_LogfileHandler;
import de.bkusche.bktail2.logfilehandler.LogfileEvent;
import de.bkusche.bktail2.logfilehandler.LogfileReadInput;
import de.bkusche.bktail2.logfilehandler.impl.LogfileHandlerImpl;

/**
 * @author bkusche
 *
 */
public class RealWorldTest {

	public static void main(String[] args) {
		final String filepath = "/opt/jboss/standalone/log/server.log";
		final int lines = 80;
//		I_LogfileHandler logfileHandler = S_LogfileHandlerImpl.getInstance();
		I_LogfileHandler logfileHandler = new LogfileHandlerImpl();
		
		System.out.println("using testfile: "+filepath);
		logfileHandler.addFileToObserve(new File( filepath ));
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
