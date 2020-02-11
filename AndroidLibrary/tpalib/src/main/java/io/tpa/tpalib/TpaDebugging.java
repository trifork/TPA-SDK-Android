package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.util.Log;

final class TpaDebugging {

    private final static String TAG = "TPADebug";
    private static boolean enabled = false;

    static final DebugLog log = new DebugLog() {
        @Override
        public void d(@NonNull String tag, @NonNull String message) {
            if (enabled) {
                Log.d(TAG, tag + ": " + message);
            }
        }

        @Override
        public void d(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable) {
            if (enabled) {
                Log.d(TAG, tag + ": " + message, throwable);
            }
        }

        @Override
        public void e(@NonNull String tag, @NonNull String message) {
            if (enabled) {
                Log.e(TAG, tag + ": " + message);
            }
        }

        @Override
        public void e(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable) {
            if (enabled) {
                Log.e(TAG, tag + ": " + message, throwable);
            }
        }

        @Override
        public void w(@NonNull String tag, @NonNull String message) {
            if (enabled) {
                Log.w(TAG, tag + ": " + message);
            }
        }

        @Override
        public void w(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable) {
            if (enabled) {
                Log.w(TAG, tag + ": " + message, throwable);
            }
        }

        @Override
        public void i(@NonNull String tag, @NonNull String message) {
            if (enabled) {
                Log.i(TAG, tag + ": " + message);
            }
        }

        @Override
        public void i(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable) {
            if (enabled) {
                Log.i(TAG, tag + ": " + message, throwable);
            }
        }
    };

    static void setEnabled(boolean enabled) {
        TpaDebugging.enabled = enabled;
    }

    static boolean isEnabled() {
        return enabled;
    }

    interface DebugLog {

        void d(@NonNull String tag, @NonNull String message);

        void d(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable);

        void e(@NonNull String tag, @NonNull String message);

        void e(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable);

        void w(@NonNull String tag, @NonNull String message);

        void w(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable);

        void i(@NonNull String tag, @NonNull String message);

        void i(@NonNull String tag, @NonNull String message, @NonNull Throwable throwable);
    }
}
