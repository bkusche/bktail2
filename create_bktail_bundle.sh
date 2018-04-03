#!/bin/sh

echo clean target
rm -rf target/*
#mkdir target/tmp

echo build maven projekt
mvn -e -DskipTests clean install

echo build jlink based runtime
./jlink.sh

mkdir target/tmp
echo execute java packager
if [ "$(uname)" == "Darwin" ]; then
  ./package.sh
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  ./javapackager.sh
fi

echo merge java packager with jlink runtime
if [ "$(uname)" == "Darwin" ]; then
  rm -rf target/out/bktail2.app/Contents/PlugIns/Java.runtime/Contents/Home/*
  cp -R target/bktail2_jre/* target/out/bktail2.app/Contents/PlugIns/Java.runtime/Contents/Home/
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  rm -rf target/out/bktail2/runtime/*
  cp -rf target/bktail2_jre/* target/out/bktail2/runtime/
fi

echo create tar.gz

cd target/out/
if [ "$(uname)" == "Darwin" ]; then
  tar cvf bktail2.tgz bktail2.app
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
  tar cvf bktail2.tgz bktail2
fi

echo finished!
