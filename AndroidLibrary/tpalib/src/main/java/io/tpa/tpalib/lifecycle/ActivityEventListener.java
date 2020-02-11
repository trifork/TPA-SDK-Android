package io.tpa.tpalib.lifecycle;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ActivityEventListener {

    void onActivityCreated(@NonNull Activity activity, @Nullable Bundle arg1);

    void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle arg1);

    void onActivityResumed(@NonNull Activity activity);

    void onActivityPaused(@NonNull Activity activity);
}
