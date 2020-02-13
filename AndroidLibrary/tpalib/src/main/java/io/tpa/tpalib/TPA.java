package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

// This class is the public API, so all methods are theoretically used.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class TPA {

    // TPA should never be initialized
    private TPA() {
    }

    //region Initialize

    /**
     * Initialize tpa.
     *
     * @param context       Usually an Activity or an ApplicationContext
     * @param configuration A {@link TpaConfiguration TpaConfiguration} instance.
     */
    public static void initialize(@NonNull Context context, @Nullable TpaConfiguration configuration) {
        TPAManager.getInstance().initialize(context, configuration);
    }

    /**
     * Initialize tpa.<br><br>
     * This initialization uses the {@code build.properties} file.
     *
     * @param context Usually an Activity or an ApplicationContext
     */
    public static void initialize(@NonNull Context context) {
        initialize(context, null);
    }
    //endregion

    //region Log
    public interface Logger {

        /**
         * Send a debug log message
         *
         * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param format Message to send. Supports standard format strings.
         * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
         */
        void d(final @NonNull String tag, final @NonNull String format, @Nullable final Object... args);

        /**
         * Send an info log message
         *
         * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param format Message to send. Supports standard format strings.
         * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
         */
        void i(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args);

        /**
         * Send a warning log message
         *
         * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param format Message to send. Supports standard format strings.
         * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
         */
        void w(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args);

        /**
         * Send an error log message
         *
         * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
         * @param format Message to send. Supports standard format strings.
         * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
         */
        void e(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args);
    }

    public final static Logger log = new Logger() {
        @Override
        public void d(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
            TPAManager.getInstance().logDebug(tag, format, args);
        }

        @Override
        public void i(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
            TPAManager.getInstance().logInfo(tag, format, args);
        }

        @Override
        public void w(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
            TPAManager.getInstance().logWarning(tag, format, args);
        }

        @Override
        public void e(@NonNull String tag, @NonNull String format, @Nullable Object... args) {
            TPAManager.getInstance().logError(tag, format, args);
        }
    };
    //endregion

    //region Feedback

    /**
     * Start feedback using the build-in screenshot functionality
     */
    public static void startFeedback() {
        TPAManager.getInstance().startFeedback();
    }

    /**
     * Start feedback providing the path of the screenshot yourself
     *
     * @param screenshotFileLocation location of screenshot to use in the feedback functionality
     */
    public static void startFeedback(@Nullable String screenshotFileLocation) {
        TPAManager.getInstance().startFeedback(screenshotFileLocation);
    }
    //endregion

    //region Non Fatal Issue

    /**
     * Report a non-fatal issue to TPA
     *
     * @param throwable the throwable containing the stacktrace and message to report. If null, nothing will be reported.
     */
    public static void reportNonFatalIssue(@NonNull Throwable throwable) {
        reportNonFatalIssue(throwable, null);
    }

    /**
     * Report a non-fatal issue to TPA
     *
     * @param reason    text describing the reason for the non-fatal issue. Should always be the same for the non-fatal issue reported, as it is used to group together non-fatal issue reports on TPA.
     * @param throwable the throwable containing the stacktrace and message to report. If null, nothing will be reported.
     */
    public static void reportNonFatalIssue(@NonNull Throwable throwable, @Nullable String reason) {
        reportNonFatalIssue(throwable, reason, null);
    }

    /**
     * Report a non-fatal issue to TPA
     *
     * @param reason    text describing the reason for the non-fatal issue. Should always be the same for the non-fatal issue reported, as it is used to group together non-fatal issue reports on TPA.
     * @param userInfo  map with additional info for the non-fatal issue. Values for this and all children must be of the following types: String, Integer, Long, Double, Boolean, List and Map
     * @param throwable the throwable containing the stacktrace and message to report. If null, nothing will be reported.
     */
    public static void reportNonFatalIssue(@NonNull Throwable throwable, @Nullable String reason, @Nullable Map<String, Object> userInfo) {
        TPAManager.getInstance().reportNonFatalIssue(throwable, reason, userInfo);
    }
    //endregion

    //region Updates
    public static void checkForUpdates(@NonNull Activity activity) {
        TPAManager.getInstance().checkForUpdates(activity);
    }

    /**
     * @param updateAvailableCallback must be instance of io.tpa.tpalib.distribution.UpdateAvailableCallback
     *                                isUpdateAvailable will be called on the updateAvailableCallback when it has been determined if there is a new version to update to.
     */
    public static void checkForUpdateAvailable(@NonNull Activity activity, @NonNull UpdateAvailableCallback updateAvailableCallback) {
        TPAManager.getInstance().checkForUpdateAvailable(activity, updateAvailableCallback);
    }
    //endregion

    //region Tracking

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     */
    public static void trackEvent(@NonNull String category, @NonNull String name) {
        TPAManager.getInstance().trackEvent(category, name, null);
    }

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param tags     a map of key/value pairs
     */
    public static void trackEvent(@NonNull String category, @NonNull String name, @NonNull Map<String, String> tags) {
        TPAManager.getInstance().trackEvent(category, name, tags);
    }


    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param value    value of event
     */
    /*public static void trackEvent(@NonNull String category, @NonNull String name, double value) {
        TPAManager.getInstance().trackEvent(category, name, value, null);
    }*/

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param value    value of event
     * @param tags     a map of key/value pairs
     */
    /*public static void trackEvent(@NonNull String category, @NonNull String name, double value, @NonNull Map<String, String> tags) {
        TPAManager.getInstance().trackEvent(category, name, value, tags);
    }*/

    /**
     * Start a time measure. This call creates a timing event with start time at call time.
     * Without tags.
     *
     * @param category category of timing event
     * @param name     name of timing event
     *
     * @return {@link TpaTimingEvent} used when timing measure is done or null if tracking is disabled.
     */
    @Nullable
    public static TpaTimingEvent startTimingEvent(@NonNull String category, @NonNull String name) {
        return TPAManager.getInstance().startTimingEvent(category, name);
    }

    /**
     * Track the specified TimingEvent.
     * <p>
     * Duration is calculated as the time interval between calling {@link #startTimingEvent(String, String)}
     * and calling this method.
     * <p>
     * For specifying a manually measured time duration see {@link #trackTimingEvent(TpaTimingEvent, long)}
     *
     * @param timingEvent {@link TpaTimingEvent} to track.
     */
    public static void trackTimingEvent(@Nullable TpaTimingEvent timingEvent) {
        if (timingEvent != null) {
            TPAManager.getInstance().trackTimingEvent(timingEvent);
        }
    }

    /**
     * Track the specified Timing event with tags.
     * <p>
     * Duration is calculated as the time interval between calling {@link #startTimingEvent(String, String)}
     * and calling this method.
     * <p>
     * For specifying a manually measured time duration see {@link #trackTimingEvent(TpaTimingEvent, long, Map)}
     *
     * @param timingEvent {@link TpaTimingEvent} to track.
     * @param tags        a map of key/value pairs.
     */
    public static void trackTimingEvent(@Nullable TpaTimingEvent timingEvent, @NonNull Map<String, String> tags) {
        if (timingEvent != null) {
            TPAManager.getInstance().trackTimingEvent(timingEvent, tags);
        }
    }

    /**
     * Track the specified TimingEvent.
     * <p>
     * Duration is given as parameter and thus not calculated automatically.
     * For automatic time measure see {@link #trackTimingEvent(TpaTimingEvent)}
     *
     * @param timingEvent {@link TpaTimingEvent} to track.
     * @param duration    the duration of the TimingEvent, in milliseconds.
     */
    public static void trackTimingEvent(@Nullable TpaTimingEvent timingEvent, long duration) {
        if (timingEvent != null) {
            TPAManager.getInstance().trackTimingEvent(timingEvent, duration);
        }
    }

    /**
     * Track the specified TimingEvent with tags.
     * <p>
     * Duration is given as parameter thus not calculated automatically.
     * For automatic time measure see {@link #trackTimingEvent(TpaTimingEvent, Map)}
     *
     * @param timingEvent {@link TpaTimingEvent} to track.
     * @param duration    the duration of the TimingEvent, in milliseconds.
     * @param tags        a map of key/value pairs.
     */
    public static void trackTimingEvent(@Nullable TpaTimingEvent timingEvent, long duration, @NonNull Map<String, String> tags) {
        if (timingEvent != null) {
            TPAManager.getInstance().trackTimingEvent(timingEvent, duration, tags);
        }
    }

    /**
     * Track a screen appearing event
     *
     * @param screenName name of screen
     */
    public static void trackScreenAppearing(@NonNull String screenName) {
        TPAManager.getInstance().trackScreenAppearing(screenName, null);
    }

    /**
     * Track a screen appearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    public static void trackScreenAppearing(@NonNull String screenName, @NonNull Map<String, String> tags) {
        TPAManager.getInstance().trackScreenAppearing(screenName, tags);
    }

    /**
     * Track a screen disappearing event
     *
     * @param screenName name of screen
     */
    public static void trackScreenDisappearing(@NonNull String screenName) {
        TPAManager.getInstance().trackScreenDisappearing(screenName, null);
    }

    /**
     * Track a screen disappearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    public static void trackScreenDisappearing(@NonNull String screenName, @NonNull Map<String, String> tags) {
        TPAManager.getInstance().trackScreenDisappearing(screenName, tags);
    }
    //endregion
}
