#!/bin/bash
JP=${JAVA_HOME}/bin/javapackager
JMODS=${JAVA_HOME}/jmods

cp bktail2-gui-jfx/target/bktail2-gui-jfx-0.0.1-SNAPSHOT.jar target/tmp/
cp bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar target/tmp/

${JP} -deploy -native image\
 --module-path ${JMODS}:target/tmp\
 -outdir target/out\
 --module de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp\
 -name bktail2\
 -title bktail2\
 -BjvmOptions="-Xrs -Xms72m -Xmx72m -XX:MaxMetaspaceSize=48m -XX:+AggressiveOpts -XX:ParallelGCThreads=4 -XX:NewRatio=8"\
 -v
