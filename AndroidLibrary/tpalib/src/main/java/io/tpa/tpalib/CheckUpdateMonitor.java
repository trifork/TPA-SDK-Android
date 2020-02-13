package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import io.tpa.tpalib.lifecycle.TpaNoCheck;

/**
 * This class is a proxy between the main tpalib and the update library extension using reflection.
 */
class CheckUpdateMonitor extends TpaFeature {

    private static final String TAG = "CheckUpdateMonitor";

    @NonNull
    private Object checkUpdateMonitor; // THIS MUST BE OF TYPE io.tpa.tpalib.distribution.CheckUpdateMonitor

    CheckUpdateMonitor(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);

        try {
            Class.forName("io.tpa.tpalib.distribution.CheckUpdateMonitor");

            checkUpdateMonitor = new io.tpa.tpalib.distribution.CheckUpdateMonitor(config.getVersionsUrl(), config.useSimpleUpdate(), config.debug(), config.isAutomaticUpdateCheckEnabled());
            TpaDebugging.log.i(TAG, "Enabling TPA distribution module");
        } catch (ClassNotFoundException e) {
            TpaDebugging.log.e(TAG, "Couldn't find CheckUpdateMonitor in update library extension.");
            throw new FeatureInitializationException("Initialization of TPA Distribution Library failed. Update monitoring will not be available");
        }
    }

    @NonNull
    private io.tpa.tpalib.distribution.CheckUpdateMonitor getUpdateMonitor() {
        return (io.tpa.tpalib.distribution.CheckUpdateMonitor) checkUpdateMonitor;
    }

    @Override
    void onAppStarted() {
        getUpdateMonitor().onAppStarted();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle arg1) {
        getUpdateMonitor().onActivityCreated(arg1);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle arg1) {
        getUpdateMonitor().onActivitySaveInstanceState(activity, arg1);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity instanceof TpaNoCheck) {
            TpaDebugging.log.d(TAG, "Instanceof TpaNoCheck - returning..");
            return;
        }

        getUpdateMonitor().onActivityResumed(activity);
    }

    void checkForUpdates(@NonNull Activity activity) {
        getUpdateMonitor().checkForUpdates(activity);
    }

    void checkForUpdateAvailable(@NonNull Activity activity, @NonNull final UpdateAvailableCallback updateAvailableCallback) {
        getUpdateMonitor().checkForUpdateAvailable(activity, new io.tpa.tpalib.distribution.UpdateAvailableCallback() {
            @Override
            public void isUpdateAvailable(boolean isUpdateAvailable) {
                updateAvailableCallback.isUpdateAvailable(isUpdateAvailable);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CheckUpdateMonitor;
    }

    @Override
    public int hashCode() {
        return 17;
    }
}
