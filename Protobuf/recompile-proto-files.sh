#!/bin/sh

# Compile protobuf messages
./protobuf_tpa_flavour/dist/bin/protoc -I=src/ --java_out=lite:../AndroidLibrary/tpalib/protobuf/src src/*.proto


# Rename packages to TPA packages (first script is for Windows, second is for Mac)
cd ../AndroidLibrary/tpalib/protobuf/src/io/
#find . -name '*.cc' -o -name '*.java' -o -name '*.proto' -type f | xargs sed -i  's/com.google.protobuf/io.tpa.tpalib.protobuf.runtime/g'
grep -ilr 'com.google.protobuf' * | xargs -I@ sed -i '' 's/com.google.protobuf/io.tpa.tpalib.protobuf.runtime/g' @
cd ../../../../../Protobuf/