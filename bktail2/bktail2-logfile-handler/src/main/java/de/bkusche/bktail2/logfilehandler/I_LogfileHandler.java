package de.bkusche.bktail2.logfilehandler;

import java.io.File;
import java.util.List;

public interface I_LogfileHandler {

	public void addFileToObserve(File filepath);
	
	public void stopObserving();
	
	public List<String> readLines( LogfileReadInput logfileReadInput );
	
	public void addLogfileEventListener( I_LogfileEventListener l );
	
	public void removeLogfileEventListener( I_LogfileEventListener l );
	
	public List<Integer> searchInLogFile( LogfileSearchInput logfileSearchInput );
}
