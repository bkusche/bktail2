package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public class LogfileEvent {

	private String name;
	private Path path;
	private long lines;
	private long delta;
	
	public LogfileEvent() {
	
	}
	
	
	public LogfileEvent(String name, Path path, long lines) {
		this.name = name;
		this.path = path;
		this.lines = lines;
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
	public long getLines() {
		return lines;
	}
	public void setLines(long lines) {
		this.delta = lines - this.lines;
		this.lines = lines;
	}
	public long getDelta() {
		return delta;
	}
}