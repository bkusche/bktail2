#!/bin/bash
JAVAPACKAGER=${JAVA_HOME}/bin/javapackager
JMODS=${JAVA_HOME}/jmods

#$JAVAPACKAGER -deploy -native image 
#$JAVAPACKAGER -deploy -native installer 
#$JAVAPACKAGER -deploy -native msi 
$JAVAPACKAGER -deploy -v -native dmg\
  -name bktail2\
  -outdir target\
  -outfile bktail2\
  -BlicenseFile=LICENSE\
  -BsignBundle=false\
  -BappVersion=1.0\
  -Bmac.dmg.simple=true\
  --module-path bktail2-gui-jfx/target/bktail2-gui-jfx.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar\
  --module de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp
