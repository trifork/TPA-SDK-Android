package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.Thread.UncaughtExceptionHandler;

class ExceptionHandler implements UncaughtExceptionHandler {

    private static final String TAG = "ExceptionHandler";

    @Nullable
    private UncaughtExceptionHandler defaultExceptionHandler;

    @NonNull
    private TPACrashReporting crashReporting;

    ExceptionHandler(@Nullable UncaughtExceptionHandler defaultExceptionHandler, @NonNull TPACrashReporting crashReporting) {
        this.defaultExceptionHandler = defaultExceptionHandler;
        this.crashReporting = crashReporting;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        TpaDebugging.log.d(TAG, "TPA received an uncaught exception");

        crashReporting.saveCrashReportToFile(thread, e);

        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(thread, e);
        }
    }
}