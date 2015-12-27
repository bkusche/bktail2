package de.bkusche.bktail2.logfilehandler;

import java.nio.file.Path;

public class LogfileSearchInput {
	
	private Path path;
	private String searchPattern;
	private boolean ignoreCase;
	
	public LogfileSearchInput(Path path, String searchPattern,boolean ignoreCase) {
		super();
		this.path = path;
		this.searchPattern = searchPattern;
		this.ignoreCase = ignoreCase;
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
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public boolean isIgnoreCase() {
		return ignoreCase;
	}
}