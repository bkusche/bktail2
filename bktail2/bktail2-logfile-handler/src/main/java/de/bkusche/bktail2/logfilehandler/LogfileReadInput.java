package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public class LogfileReadInput {

	private Path path;
	private long from;
	private long to;
	private long lines;
	
	public LogfileReadInput() {
	
	}

	public LogfileReadInput(Path path, long from, long to, long lines) {
		this.path = path;
		this.from = from;
		this.to = to;
		this.lines = lines;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public long getLines() {
		return lines;
	}

	public void setLines(long lines) {
		this.lines = lines;
	}
	
	
}
