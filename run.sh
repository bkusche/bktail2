#!/bin/sh

#java --module-path bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar:bktail2-gui-jfx/target/bktail2-gui-jfx.jar -m de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp

BKTAIL2_JRE="target/bktail2_jre"

if [ -d "$BKTAIL2_JRE" ]; then
    JAVA_HOME=$BKTAIL2_JRE
fi

echo "using JVM: $JAVA_HOME"
$JAVA_HOME/bin/java --module-path bktail2-gui-jfx/target/bktail2-gui-jfx-0.0.1-SNAPSHOT.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar -m de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp
