#!/bin/sh

./protobuf_tpa_flavour/dist/bin/protoc -I=src/ --java_out=lite:../AndroidLibrary/tpalib/protobuf/src src/*.proto
