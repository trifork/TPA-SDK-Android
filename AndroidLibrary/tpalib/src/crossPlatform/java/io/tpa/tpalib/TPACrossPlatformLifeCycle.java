package io.tpa.tpalib;

import android.app.Activity;
import android.support.annotation.NonNull;

public class TPACrossPlatformLifeCycle {

    public static void initLifecycle(@NonNull Activity activity) {
        TPAManager.getInstance().resumeActivityLifecycleIfNeeded(activity);
    }
}
