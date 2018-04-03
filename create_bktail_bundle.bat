

echo clean target
del /S /Q target\*

echo build maven projekt
mvn -e -DskipTests clean install

mkdir target\tmp
copy bktail2-gui-jfx\target\bktail2-gui-jfx-0.0.1-SNAPSHOT.jar target\tmp\
copy bktail2-logfile-handler\target\bktail2-logfile-handler-0.0.1-SNAPSHOT.jar target\tmp\

echo build jlink based runtime
call jlink_win.bat

echo execute java packager
call javapackager_win.bat

echo merge java packager with jlink runtime
del /S /Q target\out\bktail2\runtime\*
xcopy target\bktail2_jre_win\* target\out\bktail2\runtime\ /S /E /Y

rem echo create tar.gz
rem cd target\out\
rem tar cvf bktail2.tgz bktail2

echo finished!
