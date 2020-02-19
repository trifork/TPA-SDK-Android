#!/bin/sh

if [ -d "protobuf_tpa_flavour" ]; then
  echo "It looks like protobuf is already in place and ready to use."
  echo "Consider using the script recompile-proto-files.sh\n"
  echo "If you want to run the setup again, you need to remove the directory 'protobuf_tpa_flavour'."
  exit
fi


# Clone protobuf
#git clone https://github.com/google/protobuf.git protobuf_tpa_flavour
git clone -b 'v3.11.3' --single-branch --depth 1 https://github.com/google/protobuf.git protobuf_tpa_flavour


# Build protobuf compiler
cd protobuf_tpa_flavour
mkdir dist
./autogen.sh
./configure --prefix=`pwd`/dist
make -j6
make check
make install
cd ..


# Get the Protobuf source files and put them into the correct folder
mkdir temp
cd temp
cp ../../AndroidLibrary/tpalib/protobuf/libs/protobuf-javalite-3.11.3-sources.jar sources.jar
unzip sources.jar
mkdir -p ../../AndroidLibrary/tpalib/protobuf/src/io/tpa/tpalib/protobuf/runtime
cp com/google/protobuf/*.java ../../AndroidLibrary/tpalib/protobuf/src/io/tpa/tpalib/protobuf/runtime/
cd ..
rm -rf temp/


# Compile protobuf messages
./protobuf_tpa_flavour/dist/bin/protoc -I=src/ --java_out=lite:../AndroidLibrary/tpalib/protobuf/src src/*.proto


# Rename packages to TPA packages (first script is for Windows, second is for Mac)
cd ../AndroidLibrary/tpalib/protobuf/src/io/
#find . -name '*.cc' -o -name '*.java' -o -name '*.proto' -type f | xargs sed -i  's/com.google.protobuf/io.tpa.tpalib.protobuf.runtime/g'
grep -ilr 'com.google.protobuf' * | xargs -I@ sed -i '' 's/com.google.protobuf/io.tpa.tpalib.protobuf.runtime/g' @
cd ../../../../../Protobuf/