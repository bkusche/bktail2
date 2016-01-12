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
package de.bkusche.bktail2.logfilehandler;

import java.io.File;
import java.util.List;

/**
 * @author bkusche
 *
 */
public interface I_LogfileHandler {

	public void addFileToObserve(File filepath);
	
	public void stopObserving();
	
	public List<String> readLines( LogfileReadInput logfileReadInput );
	
	public void addLogfileEventListener( I_LogfileEventListener l );
	
	public void removeLogfileEventListener( I_LogfileEventListener l );
	
	public List<Integer> searchInLogFile( LogfileSearchInput logfileSearchInput );
}
