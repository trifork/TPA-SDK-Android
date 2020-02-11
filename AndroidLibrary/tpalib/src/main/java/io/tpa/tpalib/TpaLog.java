package io.tpa.tpalib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Provides logging in a similar manner to android.util.Log but adds
 * three features: 1) logging to file, 2) String.format() handling
 * of the texts to log, and 3) sending logs to TPA.
 */
class TpaLog extends TpaFeature {

    TpaLog(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
    }

    /**
     * Send a debug log message
     *
     * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param format Message to send. Supports standard format strings.
     * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
     */
    public void d(final @NonNull String tag, final @NonNull String format, @Nullable final Object... args) {
        addLog(LogLevel.DEBUG, tag, format, args);
    }

    /**
     * Send an info log message
     *
     * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param format Message to send. Supports standard format strings.
     * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
     */
    public void i(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args) {
        addLog(LogLevel.INFO, tag, format, args);
    }

    /**
     * Send a warning log message
     *
     * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param format Message to send. Supports standard format strings.
     * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
     */
    public void w(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args) {
        addLog(LogLevel.WARNING, tag, format, args);
    }

    /**
     * Send an error log message
     *
     * @param tag    Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param format Message to send. Supports standard format strings.
     * @param args   arguments list used in the format. If there are more arguments than those specified by the format string, the additional arguments are ignored.
     */
    public void e(final @NonNull String tag, final @NonNull String format, final @Nullable Object... args) {
        addLog(LogLevel.ERROR, tag, format, args);
    }

    /**
     * Log to remote and/or local logcat depending on settings
     *
     * @param level  Log level (Debug, warning, etc...)
     * @param tag    first parameter to Android.util.Log
     * @param format first parameter to String.format(format, args)
     * @param args   second parameter to String.format(format, args)
     */
    private void addLog(final @NonNull LogLevel level, final @NonNull String tag, final @NonNull String format, final @Nullable Object... args) {
        if (isLoggingEnabled()) {
            String logText;

            if (args != null && args.length > 0) {
                logText = String.format(format, args);
            } else {
                logText = format;
            }

            if (shouldLogToConsole(level)) {
                switch (level) {
                    case DEBUG:
                        Log.d(tag, logText);
                        break;
                    case INFO:
                        Log.i(tag, logText);
                        break;
                    case WARNING:
                        Log.w(tag, logText);
                        break;
                    case ERROR:
                        Log.e(tag, logText);
                        break;
                    default:
                        Log.d(tag, level.name() + " : " + logText);
                }
            }

            if (shouldLogToRemote(level)) {
                sendLog(logText, tag, level.name());
            }
        }
    }

    private boolean isLoggingEnabled() {
        return getConfig().getLoggingDestination() != LoggingDestination.NONE;
    }

    private boolean shouldLogToConsole(@NonNull LogLevel logLevel) {
        return (getConfig().getLoggingDestination() == LoggingDestination.BOTH || getConfig().getLoggingDestination() == LoggingDestination.CONSOLE) && logLevel.shouldLog(getConfig().getMinimumLogLevelConsole());
    }

    private boolean shouldLogToRemote(@NonNull LogLevel logLevel) {
        return (getConfig().getLoggingDestination() == LoggingDestination.BOTH || getConfig().getLoggingDestination() == LoggingDestination.REMOTE) && logLevel.shouldLog(getConfig().getMinimumLogLevelRemote());
    }
}
