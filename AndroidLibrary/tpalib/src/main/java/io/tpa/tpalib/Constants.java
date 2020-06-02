package io.tpa.tpalib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.annotation.NonNull;

class Constants {

    // Since the exception handler doesn't have access to the context,
    // or anything really, the library prepares these values for when
    // the handler needs them.
    @NonNull
    public final String FILES_PATH;
    @NonNull
    public final String APP_VERSION;
    @NonNull
    public final String APP_PACKAGE;
    @NonNull
    public final String APP_VERSION_NAME;

    @NonNull
    public final String ANDROID_VERSION;
    @NonNull
    public final String PHONE_MODEL;
    @NonNull
    public final String PHONE_MANUFACTURER;

    public static final String TAG = "TPAApp";

    Constants(@NonNull Context context) throws NameNotFoundException, VersionNameMissingException {
        ANDROID_VERSION = Build.VERSION.RELEASE;
        PHONE_MODEL = Build.MODEL;
        PHONE_MANUFACTURER = Build.MANUFACTURER;

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        APP_VERSION = "" + packageInfo.versionCode;
        String versionName = packageInfo.versionName;
        if (versionName == null) {
            throw new VersionNameMissingException("VersionName must not be null");
        }
        APP_VERSION_NAME = versionName;
        APP_PACKAGE = packageInfo.packageName;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FILES_PATH = context.getFilesDir().getAbsolutePath();
        } else {
            FILES_PATH = context.getNoBackupFilesDir().getAbsolutePath();
        }
    }

    static class VersionNameMissingException extends RuntimeException {

        VersionNameMissingException(String message) {
            super(message);
        }
    }
}