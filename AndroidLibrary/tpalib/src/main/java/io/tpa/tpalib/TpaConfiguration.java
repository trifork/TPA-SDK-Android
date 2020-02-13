package io.tpa.tpalib;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.tpa.tpalib.feedback.FeedbackInvocation;
import io.tpa.tpalib.feedback.ShakeFeedbackHandler;

/**
 * TPA configuration used for initialization.<br><br>
 * <p>
 * Use {@link io.tpa.tpalib.TpaConfiguration.Builder TpaConfiguration.Builder} to create an instance.
 */
public class TpaConfiguration {

    private String serverUrl;
    private String projectUuid;
    private LoggingDestination loggingDestination;
    private LogLevel minimumLogLevelConsole;
    private LogLevel minimumLogLevelRemote;
    private CrashHandling crashHandling;
    private boolean isNonFatalIssuesEnabled;
    private FeedbackInvocation feedbackInvocation;
    private boolean enableAutoTrackScreen;
    private boolean basicUpdateDialog;
    private boolean debugEnabled;
    private boolean isAnalyticsEnabled;
    private ShakeFeedbackHandler shakeFeedbackHandler;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private boolean isAutomaticUpdateCheckEnabled;

    private TpaConfiguration(@NonNull Builder builder) {
        this.projectUuid = builder.projectUuid;
        this.serverUrl = builder.serverUrl;
        this.loggingDestination = builder.loggingDestination;
        this.minimumLogLevelConsole = builder.minimumLogLevelConsole;
        this.minimumLogLevelRemote = builder.minimumLogLevelRemote;
        this.feedbackInvocation = builder.feedbackInvocation;
        this.enableAutoTrackScreen = builder.enableAutoTrackScreen;
        this.basicUpdateDialog = builder.basicUpdateDialog;
        this.shakeFeedbackHandler = builder.shakeFeedbackHandler;
        this.crashHandling = builder.crashHandling;
        this.isNonFatalIssuesEnabled = builder.isNonFatalIssuesEnabled;
        this.debugEnabled = builder.debugEnabled;
        this.isAnalyticsEnabled = builder.isAnalyticsEnabled;
        this.uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
        this.isAutomaticUpdateCheckEnabled = builder.isAutomaticUpdateCheckEnabled;
    }

    @NonNull
    public String getServerUrl() {
        return serverUrl;
    }

    @NonNull
    public String getProjectUuid() {
        return projectUuid;
    }

    @NonNull
    public LoggingDestination getLoggingDestination() {
        return loggingDestination;
    }

    @NonNull
    public LogLevel getMinimumLogLevelConsole() {
        return minimumLogLevelConsole;
    }

    @NonNull
    public LogLevel getMinimumLogLevelRemote() {
        return minimumLogLevelRemote;
    }

    @NonNull
    public FeedbackInvocation getFeedbackInvocation() {
        return feedbackInvocation;
    }

    public boolean autoTrackScreen() {
        return enableAutoTrackScreen;
    }

    public boolean useBasicUpdateDialog() {
        return basicUpdateDialog;
    }

    @Nullable
    public ShakeFeedbackHandler getShakeFeedbackHandler() {
        return shakeFeedbackHandler;
    }

    @NonNull
    public CrashHandling getCrashHandling() {
        return crashHandling;
    }

    public boolean isNonFatalIssuesEnabled() {
        return isNonFatalIssuesEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isAnalyticsEnabled() {
        return isAnalyticsEnabled;
    }

    @Nullable
    public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    void clearUncaughtExceptionHandler() {
        uncaughtExceptionHandler = null;
    }

    public boolean isAutomaticUpdateCheckEnabled() {
        return isAutomaticUpdateCheckEnabled;
    }

    public static class Builder {

        private boolean debugEnabled = false;                                                           // Optional
        @NonNull
        private String serverUrl;                                                                       // required
        @NonNull
        private String projectUuid;                                                                     // required
        @NonNull
        private LoggingDestination loggingDestination = LoggingDestination.CONSOLE;                    // optional
        @NonNull
        private LogLevel minimumLogLevelConsole = LogLevel.DEBUG;                                       // optional
        @NonNull
        private LogLevel minimumLogLevelRemote = LogLevel.DEBUG;                                        // optional
        @NonNull
        private FeedbackInvocation feedbackInvocation = FeedbackInvocation.DISABLED;                    // optional
        private boolean enableAutoTrackScreen = false;                                                  // optional
        @Nullable
        private ShakeFeedbackHandler shakeFeedbackHandler = null;                                       // optional
        private boolean basicUpdateDialog = false;                                                      // optional
        private boolean isAnalyticsEnabled = false;                                                     // optional
        @NonNull
        private CrashHandling crashHandling = CrashHandling.DISABLED;                                   // optional
        private boolean isNonFatalIssuesEnabled = false;                                                // optional
        @Nullable
        private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;                               // optional
        private boolean isAutomaticUpdateCheckEnabled = false;                                          // optional

        public Builder(@NonNull String projectUuid, @NonNull String serverUrl) {
            this.serverUrl = serverUrl;
            this.projectUuid = projectUuid;
        }

        /**
         * @param api14 true of your app targets Android API level 14 or above. False otherwise. <strong>Default:</strong> true
         *
         * @deprecated This option does nothing, as we no longer support API versions below 14.
         * Specify if your app targets Android API version 14 or later.<br><br>
         * If you set this to false you must handle app life cycle manually:<br>
         * <p>
         * Implement the callbacks by adding the following idiom to all your activities:
         *
         * <pre>
         * <code>
         * {@literal @}Override
         * public void onResume() {
         *     super.onResume();
         *     AppLifeCycle.getInstance().resumed(this);
         * }
         *
         * {@literal @}Override
         * public void onPause() {
         *     super.onPause();
         *     AppLifeCycle.getInstance().paused(this);
         * }
         *
         * {@literal @}Override
         * public void onStop() {
         *     super.onStop();
         *     AppLifeCycle.getInstance().stopped(this);
         * }
         * </code>
         * </pre>
         */
        @Deprecated
        @NonNull
        public Builder useApi14(boolean api14) {
            return this;
        }

        /**
         * Specify logging destination.
         *
         * @param loggingDestination {@link LoggingDestination#NONE NONE}, {@link LoggingDestination#CONSOLE CONSOLE}, {@link LoggingDestination#REMOTE REMOTE} or {@link LoggingDestination#BOTH BOTH}. <strong>Default:</strong> CONSOLE
         */
        @NonNull
        public Builder setLoggingDestination(@NonNull LoggingDestination loggingDestination) {
            this.loggingDestination = loggingDestination;
            return this;
        }

        /**
         * Sets the minimum log level for console logs. If logging destination is set to {@link LoggingDestination#NONE NONE} this has no effect.
         *
         * @param logLevel the minimum logging level for console logging. <strong>Default:</strong> DEBUG
         */
        @NonNull
        public Builder setMinimumLogLevelConsole(@NonNull LogLevel logLevel) {
            this.minimumLogLevelConsole = logLevel;
            return this;
        }

        /**
         * Sets the minimum log level for remote logs. If logging destination is set to {@link LoggingDestination#NONE NONE} or {@link LoggingDestination#CONSOLE CONSOLE} this has no effect.
         *
         * @param logLevel the minimum logging level for remote logging. <strong>Default:</strong> DEBUG
         */
        @NonNull
        public Builder setMinimumLogLevelRemote(@NonNull LogLevel logLevel) {
            this.minimumLogLevelRemote = logLevel;
            return this;
        }

        /**
         * @param interval interval in minutes
         *
         * @deprecated this option is no longer available
         * Minimum delay between update checks. To check for updates manually see {@link TPAManager#checkForUpdates(android.app.Activity activity) checkForUpdates()}
         */
        @NonNull
        @Deprecated
        public Builder updateInterval(int interval) {
            return this;
        }

        /**
         * Enable debug with this setting.
         *
         * @param enabled enable debug or not
         */
        @NonNull
        public Builder enableDebug(boolean enabled) {
            this.debugEnabled = enabled;
            return this;
        }

        /**
         * Basic update dialog will not offer the details button and hence, will not allow
         * the user to see release notes. Basic update dialog keeps all update handling as dialogs
         * whereas the Details/Release notes opens up a new Activity.
         *
         * @param basicDialog Use basic update dialog. <strong>Default:</strong> false
         */
        @NonNull
        public Builder basicUpdateDialog(boolean basicDialog) {
            this.basicUpdateDialog = basicDialog;
            return this;
        }

        /**
         * @param shakeFeedback whether or not to use shake feedback. <strong>Default:</strong> false
         *
         * @deprecated use {@link Builder#setFeedbackInvocation(FeedbackInvocation)}
         * Whether or not to use shake feedback.
         * <p>
         * You can optionally add a handler so you are notified whenever the users activates
         * shake feedback. See {@link #useShakeFeedback(boolean, ShakeFeedbackHandler)}
         */
        @NonNull
        @Deprecated
        public Builder useShakeFeedback(boolean shakeFeedback) {
            return setFeedbackInvocation(shakeFeedback ? FeedbackInvocation.EVENT_SHAKE : FeedbackInvocation.DISABLED);
        }

        /**
         * @param shakeFeedback whether or not to use shake feedback
         * @param handler       handler that are called when shake feedback is activated
         *
         * @deprecated use {@link Builder#setFeedbackInvocation(FeedbackInvocation)}
         * You can optionally add a handler that are called whenever the users activates
         * shake feedback. <strong>Note: </strong>When you specify a custom handler, {@link TPAManager#startFeedback() startFeedback()}
         * is not called automatically.
         */
        @NonNull
        @Deprecated
        public Builder useShakeFeedback(boolean shakeFeedback, @Nullable ShakeFeedbackHandler handler) {
            this.shakeFeedbackHandler = handler;
            return setFeedbackInvocation(shakeFeedback ? FeedbackInvocation.EVENT_SHAKE : FeedbackInvocation.DISABLED);
        }

        /**
         * Specify how feedback should be invoked.
         *
         * @param feedbackInvocation {@link FeedbackInvocation#EVENT_SHAKE}, {@link FeedbackInvocation#ENABLED} or {@link FeedbackInvocation#DISABLED}, . <strong>Default:</strong> DISABLED
         */
        @NonNull
        public Builder setFeedbackInvocation(@NonNull FeedbackInvocation feedbackInvocation) {
            this.feedbackInvocation = feedbackInvocation;
            return this;
        }

        /**
         * If you want to track screen usage automatically.
         * If you need to add any meta data to screen usage,
         * set this to false and track screen usage manually.
         *
         * @param enableAutoTrackScreen whether or not to track screen usage automatically. <strong>Default:</strong> false
         */
        @NonNull
        public Builder enableAutoTrackScreen(boolean enableAutoTrackScreen) {
            this.enableAutoTrackScreen = enableAutoTrackScreen;
            return this;
        }

        /**
         * Enable analytics for TPA. Enabling this will enable:<br/>
         * - Session recording. These are start and end events for your user.<br/>
         * - Tracking events. Events sent using {@link TPA#trackEvent(String, String)} and overloads.<br/>
         * - Timing events. Timing events sent using {@link TPA#trackTimingEvent(TpaTimingEvent)} and overloads.<br/>
         * - App events. App events such as {@link TPA#trackScreenAppearing(String)} and {@link TPA#trackScreenDisappearing(String)}.
         *
         * @param enableAnalytics whether analytics are enabled or not. <strong>Default:</strong> false
         */
        @NonNull
        public Builder enableAnalytics(boolean enableAnalytics) {
            this.isAnalyticsEnabled = enableAnalytics;
            return this;
        }

        /**
         * Specify how to handle crashes. TPA supports silent crash reporting, asking the user if they want to send the crash report or crash reporting disabled.
         *
         * @param crashHandling {@link CrashHandling#ALWAYS_SEND ALWAYS_SEND}, {@link CrashHandling#ALWAYS_ASK ALWAYS_ASK} or {@link CrashHandling#DISABLED DISABLED}, . <strong>Default:</strong> DISABLED
         */
        @NonNull
        public Builder setCrashHandling(@NonNull CrashHandling crashHandling) {
            this.crashHandling = crashHandling;
            return this;
        }

        /**
         * Set a custom uncaught exception handler. If this is specified TPA will not handle crashes automatically.
         *
         * @param uncaughtExceptionHandler the exception handler to use
         */
        @NonNull
        public Builder setUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
            this.uncaughtExceptionHandler = uncaughtExceptionHandler;
            return this;
        }

        /**
         * Enable or disable non fatal issues. When disabled calls to {@link TPA#reportNonFatalIssue(Throwable) reportNonFatalIssue} are ignored.
         *
         * @param nonFatalIssuesEnabled whether non fatal issue are enabled or not. <strong>Default:</strong> false
         */
        @NonNull
        public Builder setNonFatalIssuesEnabled(boolean nonFatalIssuesEnabled) {
            isNonFatalIssuesEnabled = nonFatalIssuesEnabled;
            return this;
        }

        /**
         * Enable or disable automatic update checks. When enabled the app will automatically poll TPA for updates on startup.
         * This has no effect if the TPA Distribution library is not included. When disabled updates can still be checked by calling {@link TPA#checkForUpdates(Activity) checkForUpdates}.
         *
         * @param automaticUpdateCheckEnabled whether automatic updates are enabled or not. <strong>Default:</strong> false
         */
        @NonNull
        public Builder setAutomaticUpdateCheckEnabled(boolean automaticUpdateCheckEnabled) {
            this.isAutomaticUpdateCheckEnabled = automaticUpdateCheckEnabled;
            return this;
        }

        @NonNull
        public TpaConfiguration build() {
            return new TpaConfiguration(this);
        }
    }
}
