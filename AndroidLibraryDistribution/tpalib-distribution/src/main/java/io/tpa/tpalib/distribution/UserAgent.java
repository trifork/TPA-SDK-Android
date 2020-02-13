package io.tpa.tpalib.distribution;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

class UserAgent {

    private static final String TAG = "UserAgent";

    static String get(Context context) {
        AppInfo appInfo = new AppInfo(context);

        return "TPA-Android/" + BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE + " ("
                + appInfo.appName + "/" + appInfo.versionName + "_" + appInfo.versionCode + ";"
                + Build.MANUFACTURER + Build.MODEL + ";"
                + "Android/" + Build.VERSION.RELEASE + ")";
    }

    private static class AppInfo {

        private String appName = "";
        private String versionName = "";
        private String versionCode = "";

        AppInfo(Context context) {
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                appName = packageInfo.packageName;
                versionName = packageInfo.versionName;
                versionCode = "" + packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException ex) {
                if (UpdateConfig.debug()) {
                    Log.d(TAG, "Cannot resolve package info");
                }
            }
        }
    }
}
