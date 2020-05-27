package io.tpa.tpalib.distribution;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtils {

    public static boolean hasPermissions(Activity activity) {
        // For SDK < 23 we get permissions at install
        // For SDK > 23 we don't need permission, as we download to internal storage
        // We only need to check for SDK == 23

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            int res = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return (res == PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }
}
