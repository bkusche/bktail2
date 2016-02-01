# bktail2

[![Build Status](https://travis-ci.org/bkusche/bktail2.svg?branch=master)](https://travis-ci.org/bkusche/bktail2)

**bktail2** is a simple log viewing / monitoring program.

![bktail2](https://cloud.githubusercontent.com/assets/16456496/12538099/ade61766-c2d0-11e5-8a62-b19a13b08754.png)

##Intention

What started out as a Java 8 learning exercise, turned into a quiet powerful little log file viewing program. 

##Features

* handles large log files up to 500 MB.
* log file monitoring ( detects creation, modification, deletion ).
* multi drag & drop support.
* monitors multiple log files simultaneously.
* customizable highlighting colors.
* search function relative to the viewing position. 

##Building

Since this is a Java 8 and Maven based project, you have to make sure that you have Java 8 and Maven 3 installed on your system. 

To build bktail2 simply execute:
```
mvn clean install
```

##License

This project is licensed under the ["Apache Software License, Version 2.0"](http://www.apache.org/licenses/LICENSE-2.0).

##Known issues & limitations
* The search shortcut keys ``` crtl+f | cmd+f ``` are only responding after clicking into the log file area.
* Loading times increasing to the end of log files. This may has something to do with the line based reading mechanism.
```java
try (Stream<String> stream = Files.lines(logfileReadInput.getPath())) {
	stream.skip(logfileReadInput.getFrom()).limit(limit)
		.forEach(lineRange::add); 
} catch (Throwable e) {
	// 
}
```
* Line counting takes to long for file sizes above 500 MB.
