package io.tpa.tpalib;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import static io.tpa.tpalib.TPACrashReporting.TPA_CRASHREPORT_POSTFIX;
import static io.tpa.tpalib.TPACrashReporting.TPA_CRASHREPORT_PREFIX;

final class MigrationHelper {

    private static final String TAG = "MigrationHelper";

    private static final int CURRENT_MIGRATION_VERSION = 1;
    private final static String downloadFilenameMatcherRegex = ".*[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}.apk";

    static void migrate(final @NonNull Context context, @NonNull Constants constants, @NonNull Runnable runAfterMigrating) {
        if (PreferencesHelper.getMigrationVersion(context) == CURRENT_MIGRATION_VERSION) {
            //if TPA is already up to date, no migration is needed
            runAfterMigrating.run();
            return;
        }

        TpaDebugging.log.d(TAG, "Migrating TPA to version " + CURRENT_MIGRATION_VERSION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //context.getFilesDir() (has INSTALLATION_FILE_NAME FILE and possibly apk files. Constants.FILES_PATH was this before)

            //first move the installation file if it exists
            File newInstallation = new File(constants.FILES_PATH, Installation.INSTALLATION_FILE_NAME);
            File origInstallation = new File(context.getFilesDir(), Installation.INSTALLATION_FILE_NAME);
            try {
                if (origInstallation.exists()) {
                    String installationFileContent = Installation.readInstallationFile(origInstallation);
                    Installation.writeInstallationFile(newInstallation, installationFileContent);
                    if (!origInstallation.delete()) {
                        failedMigration(runAfterMigrating);
                        return;
                    }
                }
            } catch (IOException e) {
                failedMigration(runAfterMigrating);
                TpaDebugging.log.e(TAG, "Migration failed", e);
                return;
            }

            //cleanup old crash reports
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(TPA_CRASHREPORT_PREFIX) && name.endsWith(TPA_CRASHREPORT_POSTFIX);
                }
            };
            File[] files = context.getFilesDir().listFiles(filter);
            if (files != null) {
                for (File file : files) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            failedMigration(runAfterMigrating);
                            return;
                        }
                    }
                }
            }

            //cleanup old log messages, both ready to be sent and not yet set to done files
            final File[] storedProtobufFiles = context.getFilesDir().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename != null && (filename.endsWith(TpaConstants.PROTOBUF_MESSAGE_EXTENSION) || filename.endsWith(TpaConstants.PROTOBUF_MESSAGE_EXTENSION + TpaConstants.PROTOBUF_MESSAGE_READY_EXTENSION));
                }
            });
            if (storedProtobufFiles != null) {
                for (File file : storedProtobufFiles) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            failedMigration(runAfterMigrating);
                            return;
                        }
                    }
                }
            }

            //cleanup download files (only from 24 and up because download works differently due to other requirements for install files)
            // Use internal storage
            File[] downloadFiles = context.getFilesDir().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    //match file name with regex for saved apk filenames
                    return name.toLowerCase().matches(downloadFilenameMatcherRegex);
                }
            });
            if (downloadFiles != null) {
                for (File file : downloadFiles) {
                    if (file.exists()) {
                        if (!file.delete()) {
                            failedMigration(runAfterMigrating);
                            return;
                        }
                    }
                }
            }
        }

        succeededMigration(context, runAfterMigrating);
    }

    private static void failedMigration(@NonNull Runnable runAfterMigrating) {
        TpaDebugging.log.e(TAG, "TPA Migration failed, initializing anyway.");
        runAfterMigrating.run();
    }

    private static void succeededMigration(@NonNull Context context, @NonNull Runnable runAfterMigrating) {
        //when everything is done, update the saved migration version
        PreferencesHelper.setMigrationVersion(context, CURRENT_MIGRATION_VERSION);
        runAfterMigrating.run();
    }
}
