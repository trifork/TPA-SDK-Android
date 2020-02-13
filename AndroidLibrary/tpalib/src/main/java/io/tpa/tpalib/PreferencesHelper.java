package io.tpa.tpalib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by repse on 27/02/2018.
 */

class PreferencesHelper {

    private static final String TPA_SHARED_PREFERENCES_NAME = "TPA_SHARED_PREFERENCES";
    private static final String PREF_KEY_MIGRATION_VERSION = "PREF_KEY_MIGRATION_VERSION";

    private static final int DEFAULT_MIGRATION_VERSION = -1;

    @NonNull
    private static SharedPreferences getTpaPreferences(@NonNull Context context) {
        return context.getSharedPreferences(TPA_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    static int getMigrationVersion(@NonNull Context context) {
        return getTpaPreferences(context).getInt(PREF_KEY_MIGRATION_VERSION, DEFAULT_MIGRATION_VERSION);
    }

    static void setMigrationVersion(@NonNull Context context, int migrationVersion) {
        getTpaPreferences(context).edit().putInt(PREF_KEY_MIGRATION_VERSION, migrationVersion).apply();
    }
}