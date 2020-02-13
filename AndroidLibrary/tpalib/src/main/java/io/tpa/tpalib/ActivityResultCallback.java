package io.tpa.tpalib;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

final class ActivityResultCallback {

    @Nullable
    private static ActivityResultCallback instance;

    private SparseArray<Callback> callbacks;

    @NonNull
    static ActivityResultCallback getInstance() {
        if (instance == null) {
            instance = new ActivityResultCallback();
        }
        return instance;
    }

    private ActivityResultCallback() {
        callbacks = new SparseArray<>();
    }

    void registerCallback(int requestCode, @NonNull Callback callback) {
        callbacks.append(requestCode, callback);
    }

    void onActivityResult(@NonNull Activity activity, int requestCode, int resultCode, @Nullable Intent intent) {
        Callback callback = callbacks.get(requestCode);
        if (callback != null) {
            callback.onComplete(activity, resultCode, intent == null ? null : (Intent) intent.clone());
        }
        callbacks.remove(requestCode);
    }

    interface Callback {

        void onComplete(@NonNull Activity activity, int resultCode, @Nullable Intent data);
    }
}
