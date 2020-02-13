package io.tpa.tpalib;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.HashSet;
import java.util.Set;

import io.tpa.tpalib.lifecycle.ActivityEventListener;
import io.tpa.tpalib.lifecycle.ApplicationEventListener;

/**
 * Utility class for monitoring application start and stop.
 * <p>
 * The method used here is simply to register active (resumed
 * or paused) versus stopped events: when the App has its first
 * active activity, the application has started; when there
 * become no active activities the application is ending.
 */
public class AppLifeCycle {

    enum State {

        RESUMED,
        PAUSED
    }

    private SparseArray<State> activityStates = new SparseArray<>();
    private Set<ApplicationEventListener> applicationListeners = new HashSet<>();
    private Set<ActivityEventListener> activityListeners = new HashSet<>();

    /**
     * Currently shown activity, MUST be cleared in onStopped as this will leak the Activity context otherwise.
     * This is a temporary solution that will be fixed in a later release.
     */
    @Nullable
    private Activity currentActivity;

    AppLifeCycle(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(new AppEventListener(this));
    }

    /**
     * Add a listener for start and end of the application.
     *
     * @param listener interested in start and stop events.
     */
    public void add(@NonNull ApplicationEventListener listener) {
        applicationListeners.add(listener);
    }

    /**
     * Add a listener for Activity level events.
     *
     * @param listener interested in activity events.
     */
    public void add(@NonNull ActivityEventListener listener) {
        activityListeners.add(listener);
    }

    @Nullable
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public static boolean isActivityValid(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isFinishing() && !activity.isDestroyed();
        } else {
            return !activity.isFinishing();
        }
    }

    public void created(@NonNull Activity activity, @Nullable Bundle bundle) {
        for (ActivityEventListener listener : activityListeners) {
            listener.onActivityCreated(activity, bundle);
        }
    }

    /**
     * To be called when an activity is resumed.
     */
    public void resumed(@NonNull Activity activity) {
        if (activityStates.size() == 0) {
            for (ApplicationEventListener listener : applicationListeners) {
                listener.started();
            }
        }
        for (ActivityEventListener listener : activityListeners) {
            listener.onActivityResumed(activity);
        }
        activityStates.put(activity.hashCode(), State.RESUMED);
        currentActivity = activity;
    }

    public void saveInstanceState(@NonNull Activity activity, @Nullable Bundle bundle) {
        for (ActivityEventListener listener : activityListeners) {
            listener.onActivitySaveInstanceState(activity, bundle);
        }
    }

    /**
     * To be called when an activity is paused.
     */
    public void paused(@NonNull Activity activity) {
        activityStates.put(activity.hashCode(), State.PAUSED);

        for (ActivityEventListener listener : activityListeners) {
            listener.onActivityPaused(activity);
        }
    }

    /**
     * To be called when an activity is stopped.
     */
    public void stopped(@NonNull Activity activity) {
        if (currentActivity != null && currentActivity == activity) {
            currentActivity = null;
        }
        activityStates.delete(activity.hashCode());
        if (activityStates.size() == 0) {
            for (ApplicationEventListener listener : applicationListeners) {
                listener.ending();
            }
        }
    }

    private static class AppEventListener implements Application.ActivityLifecycleCallbacks {

        @NonNull
        private AppLifeCycle appLifeCycle;

        AppEventListener(@NonNull AppLifeCycle appLifeCycle) {
            this.appLifeCycle = appLifeCycle;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle arg1) {
            if (activity != null) {
                appLifeCycle.created(activity, arg1);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (activity != null) {
                appLifeCycle.paused(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (activity != null) {
                appLifeCycle.resumed(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle arg1) {
            if (activity != null) {
                appLifeCycle.saveInstanceState(activity, arg1);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity != null) {
                appLifeCycle.stopped(activity);
            }
        }
    }
}
