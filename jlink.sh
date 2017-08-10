#!/bin/bash
JLINK=${JAVA_HOME}/bin/jlink
JMODS=${JAVA_HOME}/jmods

${JLINK} --module-path $JMODS:bktail2-gui-jfx/target/bktail2-gui-jfx-0.0.1-SNAPSHOT.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar --strip-debug --no-header-files --no-man-pages -G --compress=1 --add-modules=de.bkusche.bktail2.gui.jfx --limit-modules de.bkusche.bktail2.gui.jfx --output=target/bktail2_jre 

#JVMMODS=$JMODS:java.base.jmod:$JMODS:java.prefs.jmod:$JMODS:javafx.base.jmod:$JMODS:javafx.controls.jmod:$JMODS:javafx.fxml.jmod:$JMODS:javafx.graphics.jmod
#${JLINK} --module-path $JMODS:bktail2-gui-jfx/target/bktail2-gui-jfx.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar --no-header-files --no-man-pages -G --compress=1 --add-modules=de.bkusche.bktail2.gui.jfx --limit-modules de.bkusche.bktail2.gui.jfx --output=target/bktail2_jre 
