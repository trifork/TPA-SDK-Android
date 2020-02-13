package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Map;

class TpaTracker extends TpaFeature {

    private static final String TAG = "EventTracking";
    private static final String SCREEN_APPEARING = "[SCREEN_APPEARING]";
    private static final String SCREEN_DISAPPEARING = "[SCREEN_DISAPPEARING]";

    TpaTracker(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
    }

    //region TpaFeature overrides
    @Override
    void onActivityResumed(@NonNull Activity activity) {
        if (getConfig().shouldAutoTrackScreen()) {
            autoTrackScreen(SCREEN_APPEARING, activity.getLocalClassName());
        }
    }

    @Override
    void onActivityPaused(@NonNull Activity activity) {
        if (getConfig().shouldAutoTrackScreen()) {
            autoTrackScreen(SCREEN_DISAPPEARING, activity.getLocalClassName());
        }
    }
    //endregion

    //region Events

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     */
    void trackEvent(@NonNull String category, @NonNull String name) {
        TpaEvent event = new TpaEvent.Builder().setCategory(category).setName(name).build();
        trackEvent(event);
    }

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param tags     a map of key/value pairs
     */
    void trackEvent(@NonNull String category, @NonNull String name, @NonNull Map<String, String> tags) {
        TpaEvent event = new TpaEvent.Builder().setCategory(category).setName(name).addTags(tags).build();
        trackEvent(event);
    }

    private void trackEvent(@NonNull TpaEvent event) {
        TpaDebugging.log.d(TAG, "Tracking: " + event.getCategory() + ", " + event.getName());

        if (event.hasNullValues()) {
            TpaDebugging.log.e(TAG, "Attempting to track TpaEvent with null values. The event is not saved or sent.");
            return;
        }

        sendTrackingEvent(event);
    }

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param value    value of event
     */
    void trackEvent(@NonNull String category, @NonNull String name, double value) {
        TpaNumberEvent event = new TpaNumberEvent.Builder().setCategory(category).setName(name).setNumberValue(value).build();
        trackEvent(event);
    }

    /**
     * Track event with specified category and name. Without tags
     *
     * @param category event category
     * @param name     name of event
     * @param value    value of event
     * @param tags     a map of key/value pairs
     */
    void trackEvent(@NonNull String category, @NonNull String name, double value, @NonNull Map<String, String> tags) {
        TpaNumberEvent event = new TpaNumberEvent.Builder().setCategory(category).setName(name).setNumberValue(value).addTags(tags).build();
        trackEvent(event);
    }

    private void trackEvent(@NonNull TpaNumberEvent event) {
        TpaDebugging.log.d(TAG, "Tracking: " + event.getCategory() + ", " + event.getName() + ", " + event.getValue());

        if (event.hasNullValues()) {
            TpaDebugging.log.e(TAG, "Attempting to track TpaNumberEvent with null values. The event is not saved or sent.");
            return;
        }

        sendTrackingNumberEvent(event);
    }
    //endregion

    //region Timing Events

    /**
     * Start a time measure. This call creates a timing event with start time at call time.
     * Without tags.
     *
     * @param category category of timing event
     * @param name     name of timing event
     *
     * @return {@link TpaTimingEvent} used when timing measure is done.
     */
    TpaTimingEvent startTimingEvent(@NonNull String category, @NonNull String name) {
        return new TpaTimingEvent(category, name);
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
    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent) {
        sendTimingEvent(timingEvent, null);
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
    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, @NonNull Map<String, String> tags) {
        timingEvent.addTags(tags);
        sendTimingEvent(timingEvent, null);
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
    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, long duration) {
        sendTimingEvent(timingEvent, duration);
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
    void trackTimingEvent(@NonNull TpaTimingEvent timingEvent, long duration, @NonNull Map<String, String> tags) {
        timingEvent.addTags(tags);
        sendTimingEvent(timingEvent, duration);
    }
    //endregion

    //region Screen tracking

    /**
     * Track a screen appearing event
     *
     * @param screenName name of screen
     */
    void trackScreenAppearing(@NonNull String screenName) {
        trackEvent(new TpaEvent.Builder().setCategory(SCREEN_APPEARING).setName(screenName).build());
    }

    /**
     * Track a screen appearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    void trackScreenAppearing(@NonNull String screenName, @NonNull Map<String, String> tags) {
        trackEvent(new TpaEvent.Builder().setCategory(SCREEN_APPEARING).setName(screenName).addTags(tags).build());
    }

    /**
     * Track a screen disappearing event
     *
     * @param screenName name of screen
     */
    void trackScreenDisappearing(@NonNull String screenName) {
        trackEvent(new TpaEvent.Builder().setCategory(SCREEN_DISAPPEARING).setName(screenName).build());
    }

    /**
     * Track a screen disappearing event
     *
     * @param screenName name of screen
     * @param tags       Map of key/value
     */
    void trackScreenDisappearing(@NonNull String screenName, @NonNull Map<String, String> tags) {
        trackEvent(new TpaEvent.Builder().setCategory(SCREEN_DISAPPEARING).setName(screenName).addTags(tags).build());
    }
    //endregion

    //region Auto tracking
    private void autoTrackScreen(@NonNull String category, @NonNull String screenName) {
        trackEvent(new TpaEvent.Builder()
                .setCategory(category)
                .setName(screenName)
                .build());
    }
    //endregion
}
