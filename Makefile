clean: clean.test clean.release

clean.test:
	rm -fr Test

clean.release:
	rm -fr Release

.PHONY: protobuf
protobuf:
	cd Protobuf && ./compile-proto.sh

lib.android: clean.release protobuf
	mkdir -p Release
	cd AndroidLibraryDistribution; ./gradlew clean buildAndroidReleasePackage;
	rm -f AndroidLibrary/tpalib/libs/tpalib-distribution.aar
	cp -r AndroidLibraryDistribution/tpalib-distribution/build/outputs/aar/tpalib-distribution-*.aar AndroidLibrary/tpalib/libs
	mv AndroidLibrary/tpalib/libs/*.aar AndroidLibrary/tpalib/libs/tpalib-distribution.aar
	mv -f AndroidLibraryDistribution/tpalib-distribution/build/dist/* Release/TPALib-Distribution.zip
	cd AndroidLibrary; ./gradlew clean buildAndroidReleasePackage;
	mv -f AndroidLibrary/tpalib/build/dist/* Release/TPALib.zip

DEST = Test
lib.android.test: clean.test protobuf
	mkdir -p $(DEST)
	cd AndroidLibraryDistribution; ./gradlew clean assembleAndroidDebug assembleAndroidRelease;
	mv -f AndroidLibraryDistribution/tpalib-distribution/build/outputs/aar/tpalib-distribution-*-debug.aar $(DEST)/tpalib-distribution-debug.aar
	mv -f AndroidLibraryDistribution/tpalib-distribution/build/outputs/aar/tpalib-distribution-*.aar $(DEST)/tpalib-distribution-release.aar
	cp $(DEST)/tpalib-distribution-release.aar AndroidLibrary/tpalib/libs/tpalib-distribution.aar
	cd AndroidLibrary; ./gradlew clean assembleAndroidDebug assembleAndroidRelease;
	mv -f AndroidLibrary/tpalib/build/outputs/aar/tpalib-*-debug.aar $(DEST)/tpalib-debug.aar
	mv -f AndroidLibrary/tpalib/build/outputs/aar/tpalib-*.aar $(DEST)/tpalib-release.aar

lib.android.crossplatform: clean.release protobuf
	mkdir -p Release
	cd AndroidLibraryDistribution; ./gradlew clean buildCrossPlatformReleasePackage;
	rm -f AndroidLibrary/tpalib/libs/tpalib-distribution.aar
	cp -r AndroidLibraryDistribution/tpalib-distribution/build/outputs/aar/ AndroidLibrary/tpalib/libs
	mv AndroidLibrary/tpalib/libs/*.aar AndroidLibrary/tpalib/libs/tpalib-distribution.aar
	mv -f AndroidLibraryDistribution/tpalib-distribution/build/dist/* Release/TPALib-Distribution.zip
	cd AndroidLibrary; ./gradlew clean buildCrossPlatformReleasePackage;
	mv -f AndroidLibrary/tpalib/build/dist/* Release/TPALib.zip
