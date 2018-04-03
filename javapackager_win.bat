
set JP="%JAVA_HOME%\bin\javapackager.exe"
set JMODS="%JAVA_HOME%\jmods"

%JP% -deploy -native image --module-path %JMODS%;target\tmp --add-modules de.bkusche.bktail2.logfilehandler  -outdir target\out --module de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp -name bktail2 -title bktail2 -BjvmOptions="-Xrs -Xms72m -Xmx72m -XX:MaxMetaspaceSize=48m -XX:NewRatio=8" -Bicon=bktail2.ico -v
rem java 10 
rem %JP% -deploy -native image --module-path %JMODS%;target\tmp --add-modules de.bkusche.bktail2.logfilehandler  -outdir target\out --module de.bkusche.bktail2.gui.jfx/de.bkusche.bktail2.gui.jfx.MainApp -name bktail2 -title bktail2 -BjvmOptions="-Xrs -Xms72m -Xmx72m -XX;MaxMetaspaceSize=48m -XX;+AggressiveOpts -XX;ParallelGCThreads=2 -XX;NewRatio=8" -Bicon=bktail2.ico -v
