package io.tpa.tpalib;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.support.annotation.NonNull;

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

class Constants {

    // Since the exception handler doesn't have access to the context,
    // or anything really, the library prepares these values for when
    // the handler needs them.
    @NonNull
    public final String FILES_PATH;
    @NonNull
    public final String APP_VERSION;
    @NonNull
    public final String APP_PACKAGE;
    @NonNull
    public final String APP_VERSION_NAME;

    @NonNull
    public final String ANDROID_VERSION;
    @NonNull
    public final String PHONE_MODEL;
    @NonNull
    public final String PHONE_MANUFACTURER;

    public static final String TAG = "TPAApp";

    Constants(@NonNull Context context) throws NameNotFoundException {
        ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
        PHONE_MODEL = android.os.Build.MODEL;
        PHONE_MANUFACTURER = android.os.Build.MANUFACTURER;

        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        APP_VERSION = "" + packageInfo.versionCode;
        APP_VERSION_NAME = packageInfo.versionName;
        APP_PACKAGE = packageInfo.packageName;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            FILES_PATH = context.getFilesDir().getAbsolutePath();
        } else {
            FILES_PATH = context.getNoBackupFilesDir().getAbsolutePath();
        }
    }
}