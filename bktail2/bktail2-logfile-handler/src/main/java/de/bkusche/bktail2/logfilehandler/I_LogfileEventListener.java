package de.bkusche.bktail2.logfilehandler;

public interface I_LogfileEventListener {

	public void onCreate( LogfileEvent event );
	public void onModify( LogfileEvent event );
	public void onDelete( LogfileEvent event );
	
}
