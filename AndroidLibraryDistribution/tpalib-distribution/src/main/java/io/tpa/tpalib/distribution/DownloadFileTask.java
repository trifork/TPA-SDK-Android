package io.tpa.tpalib.distribution;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class DownloadFileTask extends ContextRetainingTask<String, Integer, Boolean> {

    private static final String TAG = DownloadFileTask.class.getSimpleName();
    private String urlString;
    private String filename;
    private ProgressDialog progressDialog;
    protected static final String APK_FILE_NAME_PREFIX = "TPA_";
    //arbitrary max length of package name to include in file name. Useful to limit file name length for e.g. copying saved file to Windows file system.
    private static final int MAX_PACKAGE_NAME_LENGTH = 100;
    public final static String filenameMatcherRegex = ".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.apk";
    private File downloadPath;

    DownloadFileTask(Context context, String urlString) {
        super(context);
        this.urlString = urlString;
        this.downloadPath = getDownloadPath(context);
        this.filename = APK_FILE_NAME_PREFIX + getFileNameLengthLimitedPackageName(context.getPackageName()) + "_" + UUID.randomUUID() + ".apk";
    }

    protected static String getFileNameLengthLimitedPackageName(String packageName) {
        return packageName.replace(".", "-").substring(0, Math.min(packageName.length(), MAX_PACKAGE_NAME_LENGTH));
    }

    public void attach(Context context) {
        setContext(context);
    }

    public void detach() {
        setContext(null);
        progressDialog = null;
    }

    @Override
    protected Boolean doInBackground(String... args) {
        try {
            URL url = new URL(getURLString());
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("connection", "close");
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            File file = new File(downloadPath, filename);

            InputStream input = new BufferedInputStream(connection.getInputStream());
            FileOutputStream output = new FileOutputStream(file);

            byte[] data = new byte[8192];
            int count;
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int) (total * 100 / lengthOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            return (total > 0);
        } catch (Exception e) {
            if (UpdateConfig.debug()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... args) {
        if (progressDialog == null) {
            Context context = getContext();
            if (context == null) {
                return;
            }
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        progressDialog.setProgress(args[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                if (UpdateConfig.debug()) {
                    Log.w(TAG, "progressDialog was already destroyed.");
                }
            }
        }

        Context context = getContext();
        if (context != null) {
            if (result) {
                startInstall(context);
            } else {
                presentFailureDialog(context);
            }
        }

        super.onPostExecute(result);
    }

    private void presentFailureDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.download_failed_dialog_title);
        builder.setMessage(R.string.download_failed_dialog_message);

        builder.setNegativeButton(R.string.download_failed_dialog_negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setPositiveButton(R.string.download_failed_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retry
                retry(context);
            }
        });

        try {
            builder.create().show();
        } catch (Exception e) {
            //Might fail with showing the dialog if the app is shutting down when we reach this code (In which case the user wouldn't see the dialog anyways)
            if (UpdateConfig.debug()) {
                e.printStackTrace();
            }
        }
    }

    private void retry(Context context) {
        AsyncHelper.executeAsyncTask(new DownloadFileTask(context, urlString));
    }

    protected static File getDownloadPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Use internal storage
            return context.getNoBackupFilesDir();
        } else {
            // Use external storage
            return new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
        }
    }

    private void startInstall(Context context) {
        if (context == null || filename == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startInstallPostNougat(context, filename);
        } else {
            startInstallLegacy(context, filename);
        }
    }

    private void startInstallPostNougat(Context context, String filename) {
        if (UpdateConfig.debug()) {
            Log.d(TAG, "Starting installation post Nougat");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriFromFilename(context, filename), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void startInstallLegacy(Context context, String filename) {
        if (UpdateConfig.debug()) {
            Log.d(TAG, "Starting installation legacy");
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(getDownloadPath(context), filename)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private String getURLString() {
        return urlString;
    }
}