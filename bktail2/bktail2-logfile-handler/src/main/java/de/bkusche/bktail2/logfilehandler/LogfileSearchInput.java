package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public class LogfileSearchInput {
	
	private Path path;
	private String searchPattern;
	
	public LogfileSearchInput(Path path, String searchPattern) {
		super();
		this.path = path;
		this.searchPattern = searchPattern;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}
	
}