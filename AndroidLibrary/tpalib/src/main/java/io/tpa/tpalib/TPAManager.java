package io.tpa.tpalib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.tpa.tpalib.feedback.FeedbackInvocation;
import io.tpa.tpalib.lifecycle.ActivityEventListener;
import io.tpa.tpalib.lifecycle.ApplicationEventListener;

final class TPAManager {

    @Nullable
    private static TPAManager instance;

    private TPAManager() {
    }

    @NonNull
    static TPAManager getInstance() {
        if (instance == null) {
            instance = new TPAManager();
        }
        return instance;
    }

    private final String TAG = "TPA";

    // Features
    @NonNull
    private TpaFeatureManager featureManager = new TpaFeatureManager();

    @Nullable
    private AppLifeCycle appLifeCycle;

    private boolean initialized = false;

    /**
     * Initialize TPA.
     *
     * @param context       Usually an Activity or an ApplicationContext
     * @param configuration A {@link TpaConfiguration TpaConfiguration} instance.
     */
    void initialize(@NonNull final Context context, @Nullable final TpaConfiguration configuration) {
        boolean debug = configuration != null && configuration.isDebugEnabled();
        // We only want to initialize once
        // Use normal Log statements at this point, TpaDebugging is not enabled until after config
        if (initialized) {
            if (debug) {
                Log.w(TAG, "TPA already initialized.");
            }
            return;
        }

        if (debug) {
            Log.i(TAG, "TPA initializing");
        }
        initialized = true;

        final Constants constants;
        try {
            constants = new Constants(context);
        } catch (PackageManager.NameNotFoundException ex) {
            if (debug) {
                Log.e(TAG, "Could not initialize TPA, missing package name");
            }
            return;
        }
        final Config config;
        try {
            config = configure(constants, configuration);
        } catch (Config.ConfigInitializationException ex) {
            if (debug) {
                Log.e(TAG, "Could not initialize TPA, missing TpaConfiguration or build.properties file");
            }
            return;
        }
        TpaDebugging.setEnabled(config.debug());
        final ProtobufFactory protobufFactory = new ProtobufFactory(constants);
        final ProtobufReceiver protobufReceiver = new TpaProtobufQueue(config.getProtobufPostUrl(), new BackendPing(featureManager, protobufFactory), constants);

        appLifeCycle = initAppLifeCycle(context);

        //migrate before starting up anything else
        MigrationHelper.migrate(context, constants, new Runnable() {
            @Override
            public void run() {
                featureManager.registerFeatures(getFeatures(config), context, config, constants, protobufFactory, protobufReceiver);
                if (appLifeCycle != null) {
                    appLifeCycle.add((ApplicationEventListener) featureManager);
                    appLifeCycle.add((ActivityEventListener) featureManager);
                } else {
                    TpaDebugging.log.e(TAG, "Failed to instantiate AppLifecyle, session events will not be sent.");
                }
            }
        });
    }

    @NonNull
    private TpaFeatureManager.FeatureKey[] getFeatures(@NonNull Config config) {
        List<TpaFeatureManager.FeatureKey> features = new ArrayList<>();

        features.add(TpaFeatureManager.FeatureKeys.UpdateMonitoring);
        if (config.isAnalyticsEnabled()) {
            features.add(TpaFeatureManager.FeatureKeys.SessionRecording);
            features.add(TpaFeatureManager.FeatureKeys.Tracking);
        }

        if (config.getLoggingDestination() != LoggingDestination.NONE) {
            features.add(TpaFeatureManager.FeatureKeys.Logging);
        }

        if (!config.getCrashHandling().isDisabled()) {
            features.add(TpaFeatureManager.FeatureKeys.CrashReporting);
        }

        if (config.getFeedbackInvocation() != FeedbackInvocation.DISABLED) {
            features.add(TpaFeatureManager.FeatureKeys.Feedback);
        }

        return features.toArray(new TpaFeatureManager.FeatureKey[0]);
    }

    void logDebug(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
        TpaLog tpaLog = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Logging);
        if (tpaLog != null) {
            tpaLog.d(tag, format, args);
        }
    }

    void logInfo(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
        TpaLog tpaLog = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Logging);
        if (tpaLog != null) {
            tpaLog.i(tag, format, args);
        }
    }

    void logWarning(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
        TpaLog tpaLog = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Logging);
        if (tpaLog != null) {
            tpaLog.w(tag, format, args);
        }
    }

    void logError(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
        TpaLog tpaLog = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Logging);
        if (tpaLog != null) {
            tpaLog.e(tag, format, args);
        }
    }

    /**
     * Start feedback using the build-in screenshot functionality
     */
    void startFeedback() {
        FeedbackManager feedbackManager = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Feedback);
        if (feedbackManager != null) {
            feedbackManager.startFeedback();
        }
    }

    /**
     * Start feedback providing the path of the screenshot yourself
     *
     * @param screenshotFileLocation location of screenshot to use in the feedback functionality
     */
    void startFeedback(@Nullable String screenshotFileLocation) {
        FeedbackManager feedbackManager = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Feedback);
        if (feedbackManager != null) {
            feedbackManager.startFeedback(screenshotFileLocation);
        }
    }

    /**
     * Report a non-fatal issue to TPA
     *
     * @param reason    text describing the reason for the non-fatal issue. Should always be the same for the non-fatal issue reported, as it is used to group together non-fatal issue reports on TPA.
     * @param userInfo  map with additional info for the non-fatal issue. Values for this and all children must be of the following types: String, Integer, Long, Double, Boolean, List and Map
     * @param throwable the throwable containing the stacktrace and message to report. If null, nothing will be reported.
     */
    void reportNonFatalIssue(@NonNull Throwable throwable, @Nullable String reason, @Nullable Map<String, Object> userInfo) {
        TPACrashReporting crashReporting = featureManager.getFeature(TpaFeatureManager.FeatureKeys.CrashReporting);
        if (crashReporting != null) {
            crashReporting.reportNonFatalIssue(reason, userInfo, throwable);
        }
    }

    void reportNonFatalIssue(@Nullable String stacktrace, @Nullable String reason, @Nullable Map<String, Object> userInfo, @Nullable String kind) {
        TPACrashReporting crashReporting = featureManager.getFeature(TpaFeatureManager.FeatureKeys.CrashReporting);
        if (crashReporting != null) {
            crashReporting.reportNonFatalIssue(stacktrace, reason, userInfo, kind);
        }
    }

    void checkForUpdates(@NonNull Activity activity) {
        CheckUpdateMonitor updateMonitor = featureManager.getFeature(TpaFeatureManager.FeatureKeys.UpdateMonitoring);
        if (updateMonitor != null) {
            updateMonitor.checkForUpdates(activity);
        }
    }

    /**
     * isUpdateAvailable will be called on the updateAvailableCallback when it has been determined if there is a new version to update to.
     *
     * @param updateAvailableCallback must be instance of io.tpa.tpalib.distribution.UpdateAvailableCallback.
     */
    void checkForUpdateAvailable(@NonNull Activity activity, @NonNull UpdateAvailableCallback updateAvailableCallback) {
        CheckUpdateMonitor updateMonitor = featureManager.getFeature(TpaFeatureManager.FeatureKeys.UpdateMonitoring);
        if (updateMonitor != null) {
            updateMonitor.checkForUpdateAvailable(activity, updateAvailableCallback);
        }
    }

    void trackEvent(@NonNull String category, @NonNull String name, @Nullable Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            if (tags != null) {
                tracker.trackEvent(category, name, tags);
            } else {
                tracker.trackEvent(category, name);
            }
        }
    }

    void trackEvent(@NonNull String category, @NonNull String name, double value, @Nullable Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            if (tags != null) {
                tracker.trackEvent(category, name, value, tags);
            } else {
                tracker.trackEvent(category, name, value);
            }
        }
    }

    @Nullable
    TpaTimingEvent startTimingEvent(@NonNull String category, @NonNull String name) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            return tracker.startTimingEvent(category, name);
        }
        return null;
    }

    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            tracker.trackTimingEvent(timingEvent);
        }
    }

    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, @NonNull Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            tracker.trackTimingEvent(timingEvent, tags);
        }
    }

    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, long duration) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            tracker.trackTimingEvent(timingEvent, duration);
        }
    }

    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, long duration, @NonNull Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            tracker.trackTimingEvent(timingEvent, duration, tags);
        }
    }

    /**
     * Track a screen appearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    void trackScreenAppearing(@NonNull String screenName, @Nullable Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            if (tags != null) {
                tracker.trackScreenAppearing(screenName, tags);
            } else {
                tracker.trackScreenAppearing(screenName);
            }
        }
    }

    /**
     * Track a screen disappearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    void trackScreenDisappearing(@NonNull String screenName, @Nullable Map<String, String> tags) {
        TpaTracker tracker = featureManager.getFeature(TpaFeatureManager.FeatureKeys.Tracking);
        if (tracker != null) {
            if (tags != null) {
                tracker.trackScreenDisappearing(screenName, tags);
            } else {
                tracker.trackScreenDisappearing(screenName);
            }
        }
    }

    @Nullable
    Activity getCurrentActivity() {
        if (appLifeCycle == null) {
            return null;
        }
        return appLifeCycle.getCurrentActivity();
    }

    //This is used by cross-platform module
    @SuppressWarnings("unused")
    void resumeActivityLifecycleIfNeeded(@NonNull Activity activity) {
        if (appLifeCycle != null && appLifeCycle.getCurrentActivity() == null) {
            appLifeCycle.resumed(activity);
        }
    }

    private Config configure(@NonNull Constants constants, @Nullable TpaConfiguration configuration) throws Config.ConfigInitializationException {
        if (configuration != null) {
            return new Config(configuration, constants.APP_PACKAGE);
        } else {
            return new Config(constants.APP_PACKAGE);
        }
    }

    @Nullable
    private AppLifeCycle initAppLifeCycle(Context context) {
        Context applicationContext = context.getApplicationContext();
        if (applicationContext instanceof Application) {
            return new AppLifeCycle((Application) applicationContext);
        }
        return null;
    }
}
