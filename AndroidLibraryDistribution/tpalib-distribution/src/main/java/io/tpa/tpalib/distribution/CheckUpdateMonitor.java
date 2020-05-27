package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;

/**
 * Provides checks for application up dates.
 */
public class CheckUpdateMonitor {

    private static final String TAG = "CheckUpdateMonitor";

    private ShowsUpdate checkUpdateTask = null;
    private int currentActivityHashCode; // Only store the hash code to avoid context memory leaks

    private AppList redoList;
    private boolean simpleUpdate;
    private boolean isAutomaticUpdateCheckEnabled;
    private boolean shouldCheckUpdate;

    public CheckUpdateMonitor(String url, boolean simpleUpdate, boolean debug, boolean isAutomaticUpdateCheckEnabled) {
        this.simpleUpdate = simpleUpdate;
        this.isAutomaticUpdateCheckEnabled = isAutomaticUpdateCheckEnabled;
        this.shouldCheckUpdate = false;
        init(url, debug);
    }

    private void init(String url, boolean debug) {
        UpdateConfig.setVersionsUrl(url);
        UpdateConfig.setDebug(debug);
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void onActivityCreated(Bundle arg1) {
        if (arg1 == null) {
            return;
        }

        Serializable ser = arg1.getSerializable(this.getClass().getName());

        if (ser instanceof AppList) {
            if (UpdateConfig.debug()) {
                Log.d(TAG, "On activity create: setting redoList");
            }
            redoList = (AppList) ser;
        }
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void onActivitySaveInstanceState(Activity activity, Bundle arg1) {
        if (checkUpdateTask != null && currentActivityHashCode == activity.hashCode()) {
            if (checkUpdateTask.getState() == ShowsUpdate.State.DISPLAY) {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "On activity save: Saving update dialog data");
                }

                checkUpdateTask.safeDismiss();
                arg1.putSerializable(this.getClass().getName(), checkUpdateTask.getUpdateInfo());
            } else { //run it again later
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "On activity save: nothing to save");
                }

                currentActivityHashCode = 0;
            }
        }
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void onAppStarted() {
        shouldCheckUpdate = true;
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void onActivityResumed(Activity activity) {
        if (checkUpdateTask != null && checkUpdateTask.getState() != ShowsUpdate.State.DONE) {
            if (redoList != null) {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "resume dialog, state: " + checkUpdateTask.getState());
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkUpdateTask = UpdateDialogFragment.newInstance(redoList, ShowsUpdate.State.DISPLAY, simpleUpdate);
                    activity.getFragmentManager().beginTransaction().add((UpdateDialogFragment) checkUpdateTask, "UPDATE_FRAGMENT").commitAllowingStateLoss();
                    // ((UpdateDialogFragment) checkUpdateTask).show(activity.getFragmentManager(), "updateFragment");
                } else {
                    checkUpdateTask = new UpdateDialogLegacy(activity, redoList, ShowsUpdate.State.DISPLAY, simpleUpdate);
                    ((UpdateDialogLegacy) checkUpdateTask).show();
                    currentActivityHashCode = activity.hashCode();
                }
            } else {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "checkUpdate continue");
                }

                checkUpdateTask = new DialogCheckUpdateTask(activity, simpleUpdate);
                AsyncHelper.executeAsyncTask((DialogCheckUpdateTask) checkUpdateTask);
                currentActivityHashCode = activity.hashCode();
            }
        } else if (isAutomaticUpdateCheckEnabled && shouldCheckUpdate) {
            shouldCheckUpdate = false;
            if (checkUpdateTask != null) {
                checkUpdateTask.safeDismiss();
            }

            if (UpdateConfig.debug()) {
                Log.d(TAG, "Running automatic update check");
            }
            checkUpdateTask = new DialogCheckUpdateTask(activity, simpleUpdate);
            AsyncHelper.executeAsyncTask((DialogCheckUpdateTask) checkUpdateTask);
            currentActivityHashCode = activity.hashCode();
        }
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void checkForUpdates(Activity activity) {
        if (checkUpdateTask != null) {
            checkUpdateTask.safeDismiss();
        }

        if (UpdateConfig.debug()) {
            Log.d(TAG, "checkUpdate manual");
        }
        checkUpdateTask = new DialogCheckUpdateTask(activity, simpleUpdate);
        AsyncHelper.executeAsyncTask((DialogCheckUpdateTask) checkUpdateTask);
        currentActivityHashCode = activity.hashCode();
    }

    @SuppressWarnings("unused") //Called from TPA-Lib
    public void checkForUpdateAvailable(Activity activity, UpdateAvailableCallback updateAvailableCallback) {
        if (UpdateConfig.debug()) {
            Log.d(TAG, "checkUpdateAvailable manual");
        }
        CheckUpdateAvailableTask checkUpdateTask = new CheckUpdateAvailableTask(activity, updateAvailableCallback);
        AsyncHelper.executeAsyncTask(checkUpdateTask);
        currentActivityHashCode = activity.hashCode();
    }

    @SuppressWarnings("unused") //Called from TPA-Lib //I think?
    public void onActivityPaused(Activity activity) {
        // Nothing to do here
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
