package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public interface I_LogfileEventListener {

	public void onCreate( Path logfile );
	public void onModify( Path logfile );
	public void onDelete( Path logfile );
	
}
