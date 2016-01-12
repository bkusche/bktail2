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

import java.nio.file.Path;

/**
 * @author bkusche
 *
 */
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