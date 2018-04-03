
set JLINK="%JAVA_HOME%/bin/jlink.exe"
set JMODS="%JAVA_HOME%/jmods"

rmdir /S /Q target\bktail2_jre_win
%JLINK% -v --module-path %JMODS%;target\tmp --strip-debug --strip-native-commands --no-header-files --no-man-pages --compress=2 --add-modules de.bkusche.bktail2.gui.jfx --output target\bktail2_jre_win --launcher bktail2=de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp
