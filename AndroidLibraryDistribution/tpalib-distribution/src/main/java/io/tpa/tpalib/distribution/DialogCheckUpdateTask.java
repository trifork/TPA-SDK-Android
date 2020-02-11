package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

/**
 * Updater that displays a in-app dialog that leads to a Update page with
 * additional information.
 * <p>
 * Note: Requires the android.permission.WRITE_EXTERNAL_STORAGE permission
 */
public class DialogCheckUpdateTask extends CheckUpdateTask implements ShowsUpdate {

    private final static String TAG = "DialogCheckUpdateTask";

    private State state = State.RUN;

    private ShowsUpdate updateDialog;
    private AppList updateInfo;
    private boolean simpleUpdateDialog;

    public DialogCheckUpdateTask(Activity activity, boolean simpleUpdateDialog) {
        super(activity);
        this.simpleUpdateDialog = simpleUpdateDialog;
    }

    @Override
    public State getState() {
        if (state == State.RUN || state == State.DONE) {
            return state;
        } else if (updateDialog != null) {
            return updateDialog.getState();
        } else {
            return state;
        }
    }

    @Override
    public AppList getUpdateInfo() {
        return updateInfo;
    }

    @Override
    public void safeDismiss() {
        if (updateDialog != null) {
            updateDialog.safeDismiss();
            updateDialog = null;
        }
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
            this.updateInfo = updateInfo;
            if (updateInfo.getLatestApp().getVersion() > getVersionCode()) {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "Newer version found: " + updateInfo.getLatestApp().getVersion() + " > " + getVersionCode());
                }

                // We don't show dialog if activity is gone,
                // or on Internal Activities (which are used during upgrade process)
                if (isActivityGone(activity) || activity instanceof InternalActivity) {
                    if (UpdateConfig.debug()) {
                        Log.d(TAG, "Activity is gone, cancelling update dialog");
                    }
                    return;
                }

                state = State.DISPLAY;
                if (Build.VERSION.SDK_INT >= 23) {
                    if (UpdateConfig.debug()) {
                        Log.d(TAG, "Showing update dialog (fragment)");
                    }
                    UpdateDialogFragment updateDialogFragment = UpdateDialogFragment.newInstance(updateInfo, state, simpleUpdateDialog);
                    updateDialog = updateDialogFragment;
                    String updateDialogFragmentTag = "UPDATE_DIALOG";
                    if (activity.getFragmentManager().findFragmentByTag(updateDialogFragmentTag) == null) {
                        try {
                            activity.getFragmentManager().beginTransaction().add(updateDialogFragment, updateDialogFragmentTag).commitAllowingStateLoss();
                        } catch (IllegalStateException ex) {
                            if (UpdateConfig.debug()) {
                                Log.e(TAG, "UpdateDialogFragment was committed after onSaveInstanceState", ex);
                            }
                        }
                    }
                } else {
                    if (UpdateConfig.debug()) {
                        Log.d(TAG, "Showing update dialog (legacy)");
                    }
                    if (updateDialog != null) {
                        updateDialog.safeDismiss();
                    }
                    UpdateDialogLegacy updateDialogLegacy = new UpdateDialogLegacy(activity, updateInfo, state, simpleUpdateDialog);
                    updateDialog = updateDialogLegacy;
                    updateDialogLegacy.show();
                }
            } else {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "No new version found");
                }
                state = State.DONE;
            }
        } else {
            if (UpdateConfig.debug()) {
                Log.d(TAG, "updateInfo or updateInfo.latestApp was null");
            }
        }
    }
}
