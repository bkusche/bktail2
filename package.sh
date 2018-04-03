#!/bin/bash
JAVAPACKAGER=${JAVA_HOME}/bin/javapackager
JMODS=${JAVA_HOME}/jmods

cp bktail2-gui-jfx/target/bktail2-gui-jfx-0.0.1-SNAPSHOT.jar target/tmp/
cp bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar target/tmp/

# -BlicenseFile=$(pwd)/LICENSE\

$JAVAPACKAGER -deploy -v -native image\
 -name bktail2\
 -outdir target/out\
 -outfile bktail2\
 -BsignBundle=false\
 -BappVersion=1.0\
 -Bmac.dmg.simple=true\
 -Bicon=bktail2.icns\
 --module-path ${JMODS}:target/tmp\
 --module de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp\
 -BjvmOptions="-Xrs -Xms72m -Xmx72m -XX:MaxMetaspaceSize=48m -XX:+AggressiveOpts -XX:ParallelGCThreads=4 -XX:NewRatio=8" 
