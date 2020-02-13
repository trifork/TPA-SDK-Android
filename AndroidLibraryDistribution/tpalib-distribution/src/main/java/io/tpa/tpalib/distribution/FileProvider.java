package io.tpa.tpalib.distribution;

 /*
    Copyright (c) 2008-2012 CommonsWare, LLC
    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
    by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
    OF ANY KIND, either express or implied. See the License for the specific
    language governing permissions and limitations under the License.

    From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Simple FileProvider allowing external apps to read downloaded apk-files.
 */
public class FileProvider extends ContentProvider {

    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put(".apk", "application/vnd.android.package-archive");
    }

    public static Uri getUriFromFilename(Context ctx, String filename) {
        return Uri.parse("content://" + ctx.getPackageName() + ".io.tpa.tpalib.distribution/" + filename);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String path = uri.toString();

        for (String extension : MIME_TYPES.keySet()) {
            if (path.endsWith(extension)) {
                return (MIME_TYPES.get(extension));
            }
        }

        return (null);
    }

    @Override
    @SuppressLint("NewApi")
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        File f = new File(getContext().getNoBackupFilesDir(), uri.getPath());

        if (f.exists()) {
            return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY));
        }

        throw new FileNotFoundException(uri.getPath());
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs, String sort) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }
}