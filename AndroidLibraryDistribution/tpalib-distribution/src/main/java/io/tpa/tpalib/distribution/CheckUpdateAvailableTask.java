package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

/**
 * Updater that gives a callback if an update is available.
 */
public class CheckUpdateAvailableTask extends CheckUpdateTask {

    private final static String TAG = "UpdateAvailableTask";

    private final UpdateAvailableCallback updateAvailableCallback;

    public CheckUpdateAvailableTask(Activity activity, UpdateAvailableCallback updateAvailableCallback) {
        super(activity);
        this.updateAvailableCallback = updateAvailableCallback;
    }

    private boolean isActivityGone(Activity activity) {
        if (Build.VERSION.SDK_INT >= 17) {
            return activity == null || activity.isDestroyed() || activity.isFinishing();
        } else {
            return activity == null || activity.isFinishing();
        }
    }

    @Override
    protected void onPostExecute(AppList updateInfo) {
        Activity activity = getActivity();

        if (updateInfo != null && updateInfo.getLatestApp() != null) {
            if (updateInfo.getLatestApp().getVersion() > getVersionCode()) {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "Newer version found: " + updateInfo.getLatestApp().getVersion() + " > " + getVersionCode());
                }

                // We don't show dialog if activity is gone,
                // or on Internal Activities (which are used during upgrade process)
                if (isActivityGone(activity) || activity instanceof InternalActivity) {
                    return;
                }

                updateAvailableCallback.isUpdateAvailable(true);
            } else {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "No new version found");
                }
                updateAvailableCallback.isUpdateAvailable(false);
            }
        } else {
            if (UpdateConfig.debug()) {
                Log.d(TAG, "updateInfo or updateInfo.latestApp was null");
            }
            updateAvailableCallback.isUpdateAvailable(false);
        }
    }
}
