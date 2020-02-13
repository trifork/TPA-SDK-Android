package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public enum LoggingDestination {
    /**
     * TpaLogs are ignored.
     */
    NONE,

    /**
     * TpaLogs are sent to logcat
     *
     * @deprecated use {@link #CONSOLE}
     */
    @Deprecated
    LOGCAT,

    /**
     * TpaLogs are sent to logcat
     */
    CONSOLE,

    /**
     * TpaLogs are sent to the TPA backend.
     *
     * @deprecated use {@link #REMOTE}
     */
    @Deprecated
    FILE,

    /**
     * TpaLogs are sent to the TPA backend
     */
    REMOTE,

    /**
     * TpaLogs are sent to both logcat and the TPA backend.
     */
    BOTH;

    @NonNull
    static LoggingDestination fromString(@Nullable String loggingSettings, boolean debug) {
        if (loggingSettings == null) {
            return CONSOLE;
        }

        switch (loggingSettings) {
            case "NONE":
                return NONE;
            case "BOTH":
                return BOTH;
            case "LOGCAT":
                return CONSOLE;
            case "FILE":
                return REMOTE;
            case "REMOTE":
                return REMOTE;
            default:
                if (debug) {
                    Log.d("LoggingDestination", "Unrecognized logging parameter " + loggingSettings);
                }
                return CONSOLE;
        }
    }
}
