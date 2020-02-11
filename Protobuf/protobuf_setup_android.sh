#!/bin/sh
if [ -d "protobuf_tpa_flavour" ]; then
  echo "It looks like protobuf is already in place and ready to use."
  echo "Consider using the script recompile-proto-files.sh\n"
  echo "If you want to run the setup again, you need to remove the directory 'protobuf_tpa_flavour'."
  exit
fi

# Clone protobuf
git clone https://github.com/google/protobuf.git protobuf_tpa_flavour

cd protobuf_tpa_flavour

# Build protobuf compiler
mkdir dist
./autogen.sh
./configure --prefix=`pwd`/dist
make -j6
make check
make install
cd ..

# Compile protobuf messages
./protobuf_tpa_flavour/dist/bin/protoc -I=src/ --java_out=lite:../AndroidLibrary/tpalib/protobuf/src src/*.proto
