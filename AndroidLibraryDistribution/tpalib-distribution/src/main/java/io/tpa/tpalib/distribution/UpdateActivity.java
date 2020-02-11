package io.tpa.tpalib.distribution;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateActivity extends ListActivity implements OnClickListener, InternalActivity {

    private static final int PERMISSION_REQUEST_CODE = 1337;

    private DownloadFileTask downloadTask;
    private UpdateInfoAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        init();
    }

    private void init() {
        setTitle("Application Update");
        setContentView(R.layout.update_view);

        ImageView iconView = findViewById(R.id.icon_view);
        try {
            Drawable d = getPackageManager().getApplicationIcon(getApplicationInfo());
            iconView.setBackgroundDrawable(d);
        } catch (Exception e) {
            // Ignore that the icon wasn't found
        }

        moveViewBelowOrBesideHeader(this, android.R.id.list, R.id.header_view, 23);

        adapter = new UpdateInfoAdapter(this, getIntent().getStringExtra("json"));
        getListView().setDivider(null);
        setListAdapter(adapter);
        configureView();

        downloadTask = (DownloadFileTask) getLastNonConfigurationInstance();
        if (downloadTask != null) {
            downloadTask.attach(this);
        }
    }

    private void configureView() {
        TextView appName = findViewById(R.id.name_label);
        appName.setText(adapter.getAppNameString());

        TextView versionLabel = findViewById(R.id.version_label);
        String versionText = "Version " + adapter.getVersionString() + "\n" + adapter.getFileInfoString();
        versionLabel.setText(versionText);

        TextView relNotes = findViewById(R.id.release_label);
        relNotes.setText(adapter.getNotesString());
    }

    private static void moveViewBelowOrBesideHeader(Activity activity, int viewID, int headerID, float offset) {
        ViewGroup headerView = activity.findViewById(headerID);
        View view = activity.findViewById(viewID);
        float density = activity.getResources().getDisplayMetrics().density;

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, activity.getWindowManager().getDefaultDisplay().getHeight() - headerView.getHeight() + (int) (offset * density));
        if (((String) view.getTag()).equalsIgnoreCase("right")) {
            layoutParams.addRule(RelativeLayout.RIGHT_OF, headerID);
            layoutParams.setMargins(-(int) (offset * density), 0, 0, (int) (10 * density));
        } else {
            layoutParams.addRule(RelativeLayout.BELOW, headerID);
            //layoutParams.setMargins(0, -(int)(offset * density), 0, (int)(10 * density));
        }
        view.setLayoutParams(layoutParams);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        if (downloadTask != null) {
            downloadTask.detach();
        }
        return downloadTask;
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT == 23) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onClick(View v) {
        onClickUpdate(v);
    }

    public void onClickUpdate(View v) {
        startDownloadTask();
    }

    private void startDownloadTask() {
        String updateUrl = getIntent().getStringExtra("url");
        if (PermissionUtils.hasPermissions(this)) {
            // Download!
            AsyncHelper.executeAsyncTask(new DownloadFileTask(this, updateUrl));
        } else {
            // Acquire permissions
            getPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String updateUrl = getIntent().getStringExtra("url");
                    AsyncHelper.executeAsyncTask(new DownloadFileTask(this, updateUrl));
                } else {
                    Toast.makeText(this, R.string.download_storage_permission_denied, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
