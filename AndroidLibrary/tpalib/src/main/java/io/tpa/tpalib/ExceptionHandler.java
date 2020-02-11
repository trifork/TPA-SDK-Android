package io.tpa.tpalib;

import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.Thread.UncaughtExceptionHandler;

/*
 * LICENSE INFORMATION
 * <p>
 * Copyright (c) 2009 nullwire aps
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * Contributors:
 * Mads Kristiansen, mads.kristiansen@nullwire.com
 * Glen Humphrey
 * Evan Charlton
 * Peter Hewitt
 * Thomas Dohmke, thomas@dohmke.de
 */

class ExceptionHandler implements UncaughtExceptionHandler {

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
        android.util.Log.e("ERROR", e.getMessage(), e); // Log complete trace to log. Useful when developing, as the other stacktrace is often incomplete.

        crashReporting.saveCrashReportToFile(thread, e);

        StringBuilder report = new StringBuilder();

        appendDeviceInfo(report);

        report.append("-------------------------------\n");

        Throwable t = new Throwable(report.toString(), e);
        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(thread, t);
        }
    }

    private void appendDeviceInfo(@NonNull StringBuilder report) {
        report.append("----- Device information ------\n");
        report.append("    CPU_ABI: ").append(android.os.Build.CPU_ABI).append("\n");
        report.append("    MANUFACTURER: ").append(android.os.Build.MANUFACTURER).append("\n");
        report.append("    MODEL: ").append(android.os.Build.MODEL).append("\n");
        report.append("    PRODUCT: ").append(android.os.Build.PRODUCT).append("\n");

        report.append("    VERSION.CODENAME: ").append(android.os.Build.VERSION.CODENAME).append("\n");
        report.append("    VERSION.INCREMENTAL: ").append(android.os.Build.VERSION.INCREMENTAL).append("\n");
        report.append("    VERSION.RELEASE: ").append(android.os.Build.VERSION.RELEASE).append("\n");
        report.append("    VERSION.SDK_INT: ").append(android.os.Build.VERSION.SDK_INT).append("\n");

        report.append("    NativeHeapAllocatedSize: ").append(Debug.getNativeHeapAllocatedSize()).append("\n");
        report.append("         NativeHeapFreeSize: ").append(Debug.getNativeHeapFreeSize()).append("\n");
    }
}