package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

/**
 * Custom Updater for the TPA website. Use subclasses of this instead of the
 * standard CheckUpdateTask.
 */
abstract class CheckUpdateTask extends AsyncTask<String, String, AppList> {

    private static final String TAG = "CheckUpdateTask";

    private WeakReference<Activity> activityReference;
    private String urlString;
    private String baseUrl;
    private String userAgent;
    private int versionCode;

    /**
     * Check for new updates.
     *
     * @param activity Calling activity
     */
    public CheckUpdateTask(Activity activity) {
        if (UpdateConfig.debug()) {
            Log.d(TAG, "Created CheckUpdateTask with activity: " + activity.getClass().getSimpleName());
        }
        this.versionCode = parseVersionCode(activity);
        activityReference = new WeakReference<>(activity);
        this.urlString = UpdateConfig.getVersionsUrl();
        userAgent = UserAgent.get(activity);
        setBaseUrl(urlString);
    }

    /**
     * Extract base url from download url so we can use it later
     */
    private void setBaseUrl(String url) {
        try {
            URL u = new URL(url);
            baseUrl = u.getProtocol() + "://" + u.getHost();

        } catch (MalformedURLException e) {
            if (UpdateConfig.debug()) {
                Log.e(TAG, "Wrong url", e);
            }
            baseUrl = null;
        }
    }

    private int parseVersionCode(Activity activity) {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    public int getVersionCode() {
        return versionCode;
    }

    protected Activity getActivity() {
        return activityReference.get();
    }

    /**
     * Attach the current activty to the AsyncTask.
     * Only needed if you need to handle orientation changes and use in-app
     * dialogs instead of notifications.
     */
    public void attach(Activity activity) {
        activityReference = new WeakReference<>(activity);
        //Constants.loadFromContext(activity);
    }

    /**
     * Detach the current activty from the AsyncTask.
     * Only needed if you plan to show a dialog message and the activity is
     * removed before the network finished. Remember to attach the new activity.
     */
    public void detach() {
        activityReference.clear();
    }

    @Override
    protected AppList doInBackground(String... args) {
        if (UpdateConfig.debug()) {
            Log.d(TAG, "Getting AppList in background");
        }

        if (baseUrl == null) {
            if (UpdateConfig.debug()) {
                Log.e(TAG, "baseUrl is null!");
            }
            return null;
        }

        // We'll start by removing old apk-files.
        try {
            cleanupOldApks(getActivity());
        } catch (SecurityException e) {
            if (UpdateConfig.debug()) {
                Log.e(TAG, "Was not allowed to find or delete TPA apk files to clean up", e);
            }
        }

        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", userAgent);
            connection.addRequestProperty("Connection", "close");
            connection.connect();

            inputStream = new BufferedInputStream(connection.getInputStream());
            String jsonString = convertStreamToString(inputStream);
            return new AppList(jsonString, baseUrl);
        } catch (Exception e) {
            if (UpdateConfig.debug()) {
                Log.e(TAG, "Error contacting TPA server", e);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }

    private void cleanupOldApks(Context context) throws SecurityException {
        if (context == null) {
            return;
        }

        PackageManager packageManager = context.getPackageManager();
        File downloadPath = DownloadFileTask.getDownloadPath(context);
        File[] files = downloadPath.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                //match file name with regex for saved apk filenames
                return name.toLowerCase().matches(DownloadFileTask.filenameMatcherRegex);
            }
        });
        if (files == null) {
            if (UpdateConfig.debug()) {
                Log.e(TAG, "Could not gain access to files to clean up TPA apk files - most likely missing storage permission");
            }
            return;
        }

        String packageName = context.getPackageName();
        for (File f : files) {
            PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(f.getPath(), 0);
            if (packageArchiveInfo != null && packageArchiveInfo.packageName != null) {
                if (packageArchiveInfo.packageName.equals(packageName)) {
                    deleteApkFile(context, f);
                }
            } else {
                //if we could not get package info (could be a corrupted download of file), look in the filename for package info (this only works from version 1.6.6 and forward)
                String fileName = f.getName();
                if (fileName.startsWith(DownloadFileTask.APK_FILE_NAME_PREFIX + DownloadFileTask.getFileNameLengthLimitedPackageName(packageName))) {
                    deleteApkFile(context, f);
                }
            }
        }
    }

    private void deleteApkFile(Context context, File f) {
        Date lastModDate = new Date(f.lastModified());
        Calendar calendarAfterNow = Calendar.getInstance();
        calendarAfterNow.setTime(lastModDate);
        calendarAfterNow.add(Calendar.HOUR, 2);

        Calendar calendarBeforeNow = Calendar.getInstance();
        calendarBeforeNow.setTime(lastModDate);
        calendarBeforeNow.add(Calendar.HOUR, -2);

        if (lastModDate.after(calendarBeforeNow.getTime()) && lastModDate.before(calendarAfterNow.getTime())) {
            //don't delete if it is today, we may still need it
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!context.deleteFile(f.getName())) {
                if (UpdateConfig.debug()) {
                    Log.e(TAG, "Could not delete TPA apk file to clean up");
                }
            }
        } else {
            if (!f.delete()) {
                if (UpdateConfig.debug()) {
                    Log.e(TAG, "Could not delete TPA apk file to clean up");
                }
            }
        }
    }

    @Override
    abstract protected void onPostExecute(AppList updateInfo);

    /**
     * Read HTTP response from TPA server
     */
    private static String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 1024);
        StringBuilder stringBuilder = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            if (UpdateConfig.debug()) {
                e.printStackTrace();
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                if (UpdateConfig.debug()) {
                    Log.e(TAG, "Error in TPA response", e);
                }
            }
        }
        return stringBuilder.toString();
    }
}