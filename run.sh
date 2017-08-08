#!/bin/sh

#java --module-path bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar:bktail2-gui-jfx/target/bktail2-gui-jfx.jar -m de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp
java --module-path bktail2-gui-jfx/target/bktail2-gui-jfx.jar:bktail2-logfile-handler/target/bktail2-logfile-handler-0.0.1-SNAPSHOT.jar -m de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp
