#!/bin/sh
echo "Checking protobuf setup..."
if [ -d "protobuf_tpa_flavour" ]; then
	echo "Protobuf already setup, compiling protobuf files in ./src"
	./recompile-proto-files.sh
else
	echo "Protobuf not setup, running setup script and compiling protobuf files in ./src"
	./protobuf_setup_android.sh	
fi
echo "Completed!"
