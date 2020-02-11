package io.tpa.tpalib;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public enum LogLevel {

    DEBUG,
    INFO,
    WARNING,
    ERROR;

    boolean shouldLog(@NonNull LogLevel minimumLogLevel) {
        List<LogLevel> logLevels = Arrays.asList(values());

        return logLevels.indexOf(minimumLogLevel) <= logLevels.indexOf(this);
    }
}
