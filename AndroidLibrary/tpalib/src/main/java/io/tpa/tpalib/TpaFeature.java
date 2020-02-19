package io.tpa.tpalib;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

abstract class TpaFeature {

    private static final String TAG = "TpaFeature";

    @NonNull
    private ProtobufFactory protobufFactory;
    @NonNull
    private ProtobufReceiver protobufReceiver;
    @NonNull
    private Config config;
    @NonNull
    private Constants constants;
    @NonNull
    private SessionUUIDProvider sessionUUIDProvider;

    TpaFeature(@NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        this.config = config;
        this.constants = constants;
        this.protobufFactory = protobufFactory;
        this.protobufReceiver = protobufReceiver;
        this.sessionUUIDProvider = sessionUUIDProvider;
    }

    void onAppStarted() {
    }

    void onAppEnding() {
    }

    void onActivityCreated(@NonNull Activity activity, @Nullable Bundle arg1) {
    }

    void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle arg1) {
    }

    void onActivityResumed(@NonNull Activity activity) {
    }

    void onActivityPaused(@NonNull Activity activity) {
    }

    protected void sendFeedback(@NonNull String comment, @NonNull byte[] mediaBytes) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(
                    protobufFactory
                            .newBuilder(sessionUUID)
                            .setFeedback(comment, mediaBytes)
                            .build(),
                    true
            );
        } else {
            TpaDebugging.log.w(TAG, "A feedback message was dropped because the sessionUUID is null");
        }
    }

    protected void sendCrashReport(@NonNull String report, @NonNull String kind, boolean isFatal) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(
                    protobufFactory
                            .newBuilder(sessionUUID)
                            .setCrashReport(report, kind, isFatal)
                            .build(),
                    true
            );
        } else {
            TpaDebugging.log.w(TAG, "A crash message was dropped because the sessionUUID is null");
        }
    }

    protected void sendLog(@NonNull String text, @NonNull String tag, @NonNull String logLevel) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setLog(text, tag, logLevel)
                            .build(),
                    false
            );
        } else {
            TpaDebugging.log.w(TAG, "A log message was dropped because the sessionUUID is null");
        }
    }

    protected void sendInstallation(boolean isNewInstallation) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setInstallation(isNewInstallation)
                            .build(),
                    true
            );
        } else {
            TpaDebugging.log.w(TAG, "A installation message was dropped because the sessionUUID is null");
        }
    }

    protected void sendSessionStart() {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setSessionStart()
                            .build(),
                    true
            );
        } else {
            TpaDebugging.log.w(TAG, "A session start message was dropped because the sessionUUID is null");
        }
    }

    protected void sendSessionEnd() {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setSessionEnd()
                            .build(),
                    true
            );
        } else {
            TpaDebugging.log.w(TAG, "A session end message was dropped because the sessionUUID is null");
        }
    }

    protected void sendTrackingEvent(@NonNull TpaEvent event) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setTrackingEvent(event)
                            .build(),
                    false
            );
        } else {
            TpaDebugging.log.w(TAG, "A tracking event message was dropped because the sessionUUID is null");
        }
    }

    protected void sendTrackingNumberEvent(@NonNull TpaNumberEvent event) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setTrackingNumberEvent(event)
                            .build(),
                    false
            );
        } else {
            TpaDebugging.log.w(TAG, "A tracking number event message was dropped because the sessionUUID is null");
        }
    }

    protected void sendTimingEvent(@NonNull TpaTimingEvent event, @Nullable Long duration) {
        String sessionUUID = getSessionUUID();
        if (sessionUUID != null) {
            protobufReceiver.saveProtobufMessage(protobufFactory
                            .newBuilder(sessionUUID)
                            .setTimingEvent(event, duration)
                            .build(),
                    false
            );
        } else {
            TpaDebugging.log.w(TAG, "A timing event message was dropped because the sessionUUID is null");
        }
    }

    @NonNull
    protected Config getConfig() {
        return config;
    }

    @NonNull
    protected Map<String, String> getSystemDetails() {
        return protobufFactory.getSystemDetails();
    }

    @NonNull
    protected Constants getConstants() {
        return constants;
    }

    @Nullable
    protected String getSessionUUID() {
        return sessionUUIDProvider.getSessionUUID();
    }

    static class FeatureInitializationException extends RuntimeException {

        FeatureInitializationException(String message) {
            super(message);
        }
    }
}
