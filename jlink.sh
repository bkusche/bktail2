#!/bin/bash
JLINK=${JAVA_HOME}/bin/jlink
JMODS=${JAVA_HOME}/jmods

${JLINK} --module-path $JMODS:bktail2-gui-jfx/target/bktail2-gui-jfx-0.0.1-SNAPSHOT.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar\
 --strip-debug\
 --strip-native-commands\
 --no-header-files\
 --no-man-pages\
 -G\
 --compress=2\
 --add-modules de.bkusche.bktail2.gui.jfx\
 --output target/bktail2_jre\
 --launcher bktail2=de.bkusche.bktail2.gui.jfx
