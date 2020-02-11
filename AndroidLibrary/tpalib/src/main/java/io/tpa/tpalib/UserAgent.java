package io.tpa.tpalib;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.tpa.tpalib.android.BuildConfig;

public class UserAgent {

    @Nullable
    private static String value;

    @NonNull
    public static String get(@NonNull Constants constants) {
        if (value == null) {
            value = "TPA-Android/" + BuildConfig.VERSION_NAME + "_" + BuildConfig.VERSION_CODE + " ("
                    + constants.APP_PACKAGE + "/" + constants.APP_VERSION_NAME + "_" + constants.APP_VERSION + ";"
                    + Build.MANUFACTURER + Build.MODEL + ";"
                    + "Android/" + Build.VERSION.RELEASE + ")";
        }
        return value;
    }
}
