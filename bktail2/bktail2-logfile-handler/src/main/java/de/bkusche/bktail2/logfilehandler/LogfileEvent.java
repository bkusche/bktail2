package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public class LogfileEvent {

	private String name;
	private Path path;
	private long length;
	
	public LogfileEvent() {
	
	}
	
	
	public LogfileEvent(String name, Path path, long length) {
		this.name = name;
		this.path = path;
		this.length = length;
	}


	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Path getPath() {
		return path;
	}
	public void setPath(Path path) {
		this.path = path;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	
}