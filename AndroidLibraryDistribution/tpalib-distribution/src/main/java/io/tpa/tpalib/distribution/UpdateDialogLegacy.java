package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class UpdateDialogLegacy implements ShowsUpdate {

    private AppList updateInfo;
    private State state;
    private AlertDialog alert;

    public UpdateDialogLegacy(final Activity activity, final AppList updateInfo, State state, boolean simpleUpdateDialog) {
        this.updateInfo = updateInfo;
        this.state = state;
        displayDialog(activity, updateInfo, simpleUpdateDialog);
    }

    @Override
    public AppList getUpdateInfo() {
        return updateInfo;
    }

    @Override
    public State getState() {
        return state;
    }

    public void show() {
        if (alert != null) {
            alert.show();
        }
    }

    @Override
    public void safeDismiss() {
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
        }
    }

    private void displayDialog(final Activity activity, final AppList updateInfo, boolean simpleUpdateDialog) {
        if ((activity == null) || (activity.isFinishing())) {
            return;
        }

        AppEntry newest = updateInfo.getLatestApp();
        String available = newest.getTitle() + " " + newest.getShortVersion() + " (" + newest.getVersion() + ")";
        String message = activity.getResources().getString(R.string.update_dialog_message, available);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.update_dialog_title);
        builder.setMessage(message);

        builder.setNegativeButton(R.string.update_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        state = State.DONE;
                        /* Ignore */
                    }
                });
        builder.setPositiveButton(R.string.update_dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        state = State.DONE;
                        AsyncHelper.executeAsyncTask((new DownloadFileTask(activity, updateInfo.getLatestApp().getInstallUrl())));
                    }
                });

        if (!simpleUpdateDialog) {
            builder.setNeutralButton(R.string.update_dialog_neutral_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //if (UpdateConfig.debug()) Log.d("UpdateDialogLegacy", "Latest update info: " + updateInfo.getLatestApp().toJsonString());
                    state = State.RUN;
                    Intent intent = new Intent(activity, UpdateActivity.class);
                    intent.putExtra("json", updateInfo.getJSONList());
                    intent.putExtra("url", updateInfo.getLatestApp().getInstallUrl());
                    activity.startActivity(intent);
                }
            });
        }

        alert = builder.create();
    }
}
