package io.tpa.tpalib;

import android.content.Context;
import android.support.annotation.NonNull;

final class SessionRecording extends TpaFeature {

    private final static String TAG = "SessionRecording";

    SessionRecording(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
    }

    @Override
    public void onAppStarted() {
        TpaDebugging.log.d(TAG, "Sending start event");

        sendSessionStart();

        addInstallationEventIfNeeded();
    }

    @Override
    public void onAppEnding() {
        TpaDebugging.log.d(TAG, "Sending end event");

        sendSessionEnd();
    }

    private void addInstallationEventIfNeeded() {
        try {
            VersionCode.VersionCodeChange versionCodeChange = VersionCode.updateVersionCode(getConstants().APP_VERSION, getConstants().FILES_PATH);

            if (versionCodeChange == VersionCode.VersionCodeChange.CREATED || versionCodeChange == VersionCode.VersionCodeChange.UPDATED) {
                boolean isNewInstallation = false;
                // Guard against update from old versions of TPA lib. Existing users will have an existing installation id.
                if (versionCodeChange == VersionCode.VersionCodeChange.CREATED && Installation.id(getConstants().FILES_PATH).isNewInstallation()) {
                    isNewInstallation = true;
                }

                sendInstallation(isNewInstallation);
            }
        } catch (VersionCode.InstallationValidationException ex) {
            TpaDebugging.log.e(TAG, ex.getMessage(), ex);
        }
    }
}
