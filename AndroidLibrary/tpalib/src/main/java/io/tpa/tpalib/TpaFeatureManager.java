package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.tpa.tpalib.lifecycle.ActivityEventListener;
import io.tpa.tpalib.lifecycle.ApplicationEventListener;

final class TpaFeatureManager implements ApplicationEventListener, ActivityEventListener, SessionUUIDProvider {

    private static final String TAG = "TpaFeatureManager";

    @Nullable
    private ProtobufReceiver protobufReceiver;
    private boolean isRunning = false;

    static abstract class FeatureKey<T extends TpaFeature> {

        @NonNull
        private final String name;

        FeatureKey(@NonNull String name) {
            this.name = name;
        }

        abstract T createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException;
    }

    static class FeatureKeys {

        final static FeatureKey<SessionRecording> SessionRecording = new FeatureKey<io.tpa.tpalib.SessionRecording>("SessionRecording") {
            @Override
            io.tpa.tpalib.SessionRecording createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                return new SessionRecording(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
            }
        };
        final static FeatureKey<TpaLog> Logging = new FeatureKey<TpaLog>("Logging") {
            @Override
            TpaLog createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                return new TpaLog(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
            }
        };
        final static FeatureKey<TPACrashReporting> CrashReporting = new FeatureKey<TPACrashReporting>("CrashReporting") {
            @Override
            TPACrashReporting createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                return new TPACrashReporting(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
            }
        };
        final static FeatureKey<CheckUpdateMonitor> UpdateMonitoring = new FeatureKey<CheckUpdateMonitor>("UpdateMonitoring") {
            @Override
            CheckUpdateMonitor createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                return new CheckUpdateMonitor(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
            }
        };
        final static FeatureKey<TpaTracker> Tracking = new FeatureKey<TpaTracker>("Tracking") {
            @Override
            TpaTracker createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                return new TpaTracker(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
            }
        };
        final static FeatureKey<FeedbackManager> Feedback = new FeatureKey<FeedbackManager>("Feedback") {
            @Override
            FeedbackManager createFeature(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws TpaFeature.FeatureInitializationException {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return new LollipopFeedbackManager(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
                } else {
                    return new PreLollipopFeedbackManager(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
                }
            }
        };
    }

    private Map<String, TpaFeature> features;

    TpaFeatureManager() {
        features = new HashMap<>();
    }

    void registerFeatures(@NonNull FeatureKey[] featureKeys, @NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver) {
        this.protobufReceiver = protobufReceiver;
        TpaDebugging.log.d(TAG, "Registering features.");
        features = new HashMap<>();
        for (FeatureKey featureKey : featureKeys) {
            try {
                addFeature(featureKey, featureKey.createFeature(context, config, constants, protobufFactory, protobufReceiver, this));
                TpaDebugging.log.d(TAG, "Started " + featureKey.name + " feature");
            } catch (TpaFeature.FeatureInitializationException ex) {
                TpaDebugging.log.e(TAG, "Failed to initiate feature: " + featureKey.name + ". Reason: " + ex.getMessage());
            }
        }
        if (isRunning) {
            protobufReceiver.restart();
            TpaDebugging.log.d(TAG, "App is already running, calling onAppStarted for features.");
            for (TpaFeature feature : features.values()) {
                feature.onAppStarted();
            }
        }
    }

    @Override
    public void started() {
        generateSessionUUID();
        if (protobufReceiver != null) {
            protobufReceiver.restart();
        }
        TpaDebugging.log.d(TAG, "Sending appStarted to features.");
        isRunning = true;
        for (TpaFeature feature : features.values()) {
            feature.onAppStarted();
        }
    }

    @Override
    public void ending() {
        TpaDebugging.log.d(TAG, "Sending appEnding to features.");
        isRunning = false;
        for (TpaFeature feature : features.values()) {
            feature.onAppEnding();
        }
        if (protobufReceiver != null) {
            protobufReceiver.stop();
        }
        resetSessionUUID();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle arg1) {
        for (TpaFeature feature : features.values()) {
            feature.onActivityCreated(activity, arg1);
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle arg1) {
        for (TpaFeature feature : features.values()) {
            feature.onActivitySaveInstanceState(activity, arg1);
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        for (TpaFeature feature : features.values()) {
            feature.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        for (TpaFeature feature : features.values()) {
            feature.onActivityPaused(activity);
        }
    }

    private <T extends TpaFeature> void addFeature(@NonNull FeatureKey<T> key, @NonNull T feature) {
        features.put(key.name, feature);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    <T extends TpaFeature> T getFeature(FeatureKey<T> featureKey) {
        TpaFeature feature = features.get(featureKey.name);
        if (feature == null) {
            return null;
        }

        try {
            return (T) feature;
        } catch (ClassCastException ex) {
            features.remove(featureKey.name);
            return null;
        }
    }

    //region SessionUUIDProvider
    // Session UUID must be accessed and mutated from synchronized methods to ensure it is correct across multiple threads.
    @Nullable
    private String sessionUUID = null;

    @Nullable
    @Override
    public synchronized String getSessionUUID() {
        return sessionUUID;
    }

    private synchronized void generateSessionUUID() {
        sessionUUID = UUID.randomUUID().toString();
    }

    private synchronized void resetSessionUUID() {
        sessionUUID = null;
    }
    //endregion
}
