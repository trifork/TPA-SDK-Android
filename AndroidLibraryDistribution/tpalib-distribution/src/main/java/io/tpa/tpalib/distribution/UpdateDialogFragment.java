package io.tpa.tpalib.distribution;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.M)
public class UpdateDialogFragment extends DialogFragment implements ShowsUpdate {

    private static final int PERMISSION_REQUEST_CODE = 1337;

    private AppList updateInfo;
    private State state;

    public static UpdateDialogFragment newInstance(final AppList updateInfo, State state, boolean simpleUpdateDialog) {
        UpdateDialogFragment frag = new UpdateDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("simpleUpdateDialog", simpleUpdateDialog);
        args.putSerializable("updateInfo", updateInfo);
        args.putSerializable("state", state);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            // Handle the positive button here, in order for the dialog not to close in case of a requestPermission
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    state = State.DONE;
                    if (PermissionUtils.hasPermissions(getActivity())) {
                        // Download!
                        AsyncHelper.executeAsyncTask(new DownloadFileTask(getActivity(), updateInfo.getLatestApp().getInstallUrl()));
                        dismissAllowingStateLoss();
                    } else {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                }
            });
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        updateInfo = (AppList) getArguments().getSerializable("updateInfo");
        state = (State) getArguments().getSerializable("state");
        boolean simpleUpdateDialog = getArguments().getBoolean("simpleUpdateDialog");

        final Activity activity = getActivity();

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
                        // Dummy listener - handled in onStart() to prevent premature dismiss
                    }
                });

        if (!simpleUpdateDialog) {
            builder.setNeutralButton(R.string.update_dialog_neutral_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    state = State.RUN;
                    Intent intent = new Intent(activity, UpdateActivity.class);
                    intent.putExtra("json", updateInfo.getJSONList());
                    intent.putExtra("url", updateInfo.getLatestApp().getInstallUrl());
                    activity.startActivity(intent);
                }
            });
        }

        return builder.create();
    }

    @Override
    public AppList getUpdateInfo() {
        return updateInfo;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void safeDismiss() {
        dismissAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AsyncHelper.executeAsyncTask(new DownloadFileTask(getActivity(), updateInfo.getLatestApp().getInstallUrl()));
                    dismissAllowingStateLoss();
                } else {
                    Toast.makeText(getActivity(), R.string.download_storage_permission_denied, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
