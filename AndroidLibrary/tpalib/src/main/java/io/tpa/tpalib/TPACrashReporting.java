package io.tpa.tpalib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import io.tpa.tpalib.android.R;
import io.tpa.tpalib.lifecycle.TpaNoCheck;

class TPACrashReporting extends TpaFeature {

    private static final String TAG = "TPACrashReporting";

    static final String TPA_CRASHREPORT_POSTFIX = ".todo";
    static final String TPA_CRASHREPORT_PREFIX = "tpa-crashreport-";

    @NonNull
    private File crashReportDir;

    private static Semaphore uploadSemaphore = new Semaphore(1);

    private static final String CRASH_REPORTING_KIND_ANDROID = "Native";

    TPACrashReporting(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
        crashReportDir = new File(constants.FILES_PATH);
        setupExceptionHandler(config.popUncaughtExceptionHandler());
    }

    private void setupExceptionHandler(@Nullable Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        if (getConfig().getCrashHandling().isDisabled()) {
            return;
        }

        // Get current handler
        Thread.UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (currentHandler != null) {
            TpaDebugging.log.d(Constants.TAG, "Current handler class = " + currentHandler.getClass().getName());
        }

        Thread.UncaughtExceptionHandler exceptionHandler = uncaughtExceptionHandler;
        if (exceptionHandler == null) {
            exceptionHandler = new ExceptionHandler(currentHandler, this);
        }

        // Register if not already registered
        if (currentHandler == null || currentHandler.getClass() != exceptionHandler.getClass()) {
            TpaDebugging.log.d(Constants.TAG, "Installing new handler of class = " + exceptionHandler.getClass().getName());
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
        }
    }

    @Override
    void onActivityResumed(@NonNull Activity activity) {
        TpaDebugging.log.i(TAG, "OnActivityResumed");

        //check first if we are allowed to show pop-ups in current activity, or if crash does not show pop-ups
        if (!(activity instanceof TpaNoCheck) || getConfig().getCrashHandling() != CrashHandling.ALWAYS_ASK) {
            // Check for cReport crashes if relevant
            AsyncHelper.executeAsyncTask(new CheckForCrashesTask(this));
        }
    }

    private boolean hasStackTraces() {
        return listErrorReports().length > 0;
    }

    private void handleFoundCrashes(@Nullable Activity activity) {
        if (activity == null || !AppLifeCycle.isActivityValid(activity)) {
            return;
        }

        // Create ASyncTask to handle dialog response
        final AsyncTask<Boolean, Void, Void> deleteOrSendCrashesTask = new DeleteOrSendCrashesTask(this);

        if (getConfig().getCrashHandling() == CrashHandling.ALWAYS_ASK) {
            if (activity instanceof TpaNoCheck) {
                return;
            }

            // Find root activity
            Activity rootActivity = activity;
            while (rootActivity.getParent() != null) {
                TpaDebugging.log.d("CrashManager", "-- get root activity loop");
                rootActivity = rootActivity.getParent();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(rootActivity);
            builder.setTitle(R.string.crash_dialog_title);
            builder.setMessage(R.string.crash_dialog_message);

            builder.setNegativeButton(R.string.crash_dialog_negative_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AsyncHelper.executeAsyncTask(deleteOrSendCrashesTask, false);
                }
            });

            builder.setPositiveButton(R.string.crash_dialog_positive_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Thread() {
                        @Override
                        public void run() {
                            AsyncHelper.executeAsyncTask(deleteOrSendCrashesTask, true);
                        }
                    }.start();
                }
            });

            builder.create().show();
        } else if (getConfig().getCrashHandling() == CrashHandling.ALWAYS_SEND) {
            AsyncHelper.executeAsyncTask(deleteOrSendCrashesTask, true);
        } else {
            AsyncHelper.executeAsyncTask(deleteOrSendCrashesTask, false);
        }
    }

    private static class CheckForCrashesTask extends AsyncTask<Void, Void, Boolean> {

        @Nullable
        private TPACrashReporting crashReporting;

        CheckForCrashesTask(@NonNull TPACrashReporting crashReporting) {
            this.crashReporting = crashReporting;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return crashReporting != null && crashReporting.hasStackTraces();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Activity activity = TPAManager.getInstance().getCurrentActivity();
            if (result && crashReporting != null) {
                crashReporting.handleFoundCrashes(activity);
            }
            crashReporting = null;
        }
    }

    private static class DeleteOrSendCrashesTask extends AsyncTask<Boolean, Void, Void> {

        @Nullable
        private TPACrashReporting crashReporting;

        DeleteOrSendCrashesTask(@NonNull TPACrashReporting crashReporting) {
            this.crashReporting = crashReporting;
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            if (crashReporting == null) {
                return null;
            }

            if (params[0]) {
                // Send traces
                crashReporting.shipAllErrorReportsToTPA(crashReporting.listErrorReports());
            } else {
                // Dismiss called
                crashReporting.deleteStackTraces();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            crashReporting = null;
        }
    }

    @NonNull
    private String[] listErrorReports() {
        TpaDebugging.log.d(TAG, "Looking for exceptions in: " + crashReportDir.getAbsolutePath());

        if (crashReportDir.isDirectory()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(TPA_CRASHREPORT_PREFIX) && name.endsWith(TPA_CRASHREPORT_POSTFIX);
                }
            };
            String[] list = crashReportDir.list(filter);
            if (list != null) {
                return list;
            }
        }
        return new String[0];
    }

    private void shipAllErrorReportsToTPA(@NonNull String[] files) {
        try {
            uploadSemaphore.acquire(); //Only one upload at a time
            for (String fileName : files) {
                if (fileName.endsWith(TPA_CRASHREPORT_POSTFIX)) {
                    File file = new File(crashReportDir, fileName);

                    if (!file.exists()) { //File has already been uploaded and deleted
                        continue;
                    }

                    TpaDebugging.log.d(TAG, "Posting error report to TPA from file " + fileName);

                    try {
                        String report = new String(FileUtils.readFile(file), "UTF-8");
                        sendCrashReport(report, CRASH_REPORTING_KIND_ANDROID, true);

                        // Upload completed, delete report from file system.
                        boolean success = file.delete();
                        if (!success) {
                            TpaDebugging.log.d(TAG, "Failed to delete crash report file.");
                        }
                    } catch (IOException e) {
                        TpaDebugging.log.e(TAG, e.getMessage(), e);
                    }
                }
            }
            uploadSemaphore.release();
        } catch (InterruptedException e) {
            TpaDebugging.log.e(TAG, e.getMessage(), e);
        }
    }

    private void deleteStackTraces() {
        String[] files = listErrorReports();

        for (String fileName : files) {
            try {
                File file = new File(crashReportDir, fileName);
                boolean result = file.delete();
                TpaDebugging.log.d(Constants.TAG, "Deletion of file with name " + fileName + " completed with result: " + result);
            } catch (Exception ex) {
                TpaDebugging.log.e(Constants.TAG, "Error deleting file with name " + fileName);
            }
        }
    }

    @NonNull
    private JSONObject makeBaseReport(@NonNull String sessionUUID, @NonNull Throwable throwable) throws JSONException, UnsupportedEncodingException {
        JSONObject report = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
        report.put("when", sdf.format(new Date()));

        JSONObject app = new JSONObject();
        app.put("version", "" + getConstants().APP_VERSION);
        app.put("package-name", getConstants().APP_PACKAGE);
        app.put("session-uuid", sessionUUID);
        app.put("device-uuid", Installation.id(getConstants().FILES_PATH).getId());
        report.put("app", app);

        JSONObject additional = new JSONObject();
        Map<String, String> sysDetails = getSystemDetails();
        for (String key : sysDetails.keySet()) {
            additional.put(key, sysDetails.get(key));
        }
        report.put("additional-info", additional);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        throwable.printStackTrace(new PrintStream(out, true, "UTF-8"));
        String stacktrace = new String(out.toByteArray(), "UTF-8");
        report.put("stacktrace", stacktrace);

        return report;
    }

    @NonNull
    private JSONObject makeBaseReport(String sessionUUUID, String stacktrace) throws JSONException {
        JSONObject report = new JSONObject();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US);
        report.put("when", sdf.format(new Date()));

        JSONObject app = new JSONObject();
        app.put("version", "" + getConstants().APP_VERSION);
        app.put("package-name", getConstants().APP_PACKAGE);
        app.put("session-uuid", sessionUUUID);
        app.put("device-uuid", Installation.id(getConstants().FILES_PATH).getId());
        report.put("app", app);

        JSONObject additional = new JSONObject();
        Map<String, String> sysDetails = getSystemDetails();
        for (String key : sysDetails.keySet()) {
            additional.put(key, sysDetails.get(key));
        }
        report.put("additional-info", additional);
        report.put("stacktrace", stacktrace);

        return report;
    }

    @NonNull
    private String makeCrashReport(@NonNull Throwable throwable) throws JSONException, UnsupportedEncodingException {
        String sessionUUID = getSessionUUID();
        if (sessionUUID == null) {
            sessionUUID = UUID.randomUUID().toString();
        }

        return makeBaseReport(sessionUUID, throwable).toString(0);
    }

    // toString() on JSONObject does not uphold the contract for toString() and thus can return null if the data it contains is empty.
    @Nullable
    private String makeNonFatalIssue(@NonNull Throwable throwable, @Nullable String reason, @Nullable Map<String, Object> userInfo) throws JSONException, UnsupportedEncodingException {
        String sessionUUID = getSessionUUID();
        if (sessionUUID == null) {
            return null;
        }

        JSONObject report = makeBaseReport(sessionUUID, throwable);

        if (reason != null && !reason.trim().isEmpty()) {
            report.put("user-reason", reason);
        }

        if (userInfo != null) {
            report.put("user-info", UserInfoParser.parseUserInfo(userInfo));
        }

        return report.toString();
    }

    // toString() on JSONObject does not uphold the contract for toString() and thus can return null if the data it contains is empty.
    @Nullable
    private String makeNonFatalIssue(String stacktrace, String reason, Map<String, Object> userInfo) throws JSONException {
        String sessionUUID = getSessionUUID();
        if (sessionUUID == null) {
            return null;
        }

        JSONObject report = makeBaseReport(sessionUUID, stacktrace);

        if (reason != null && !reason.trim().isEmpty()) {
            report.put("user-reason", reason);
        }

        if (userInfo != null) {
            report.put("user-info", UserInfoParser.parseUserInfo(userInfo));
        }

        return report.toString();
    }

    /**
     * Report a non-fatal issue to TPA
     *
     * @param reason    text describing the reason for the non-fatal issue. Should always be the same for the non-fatal issue reported, as it is used to group together non-fatal issue reports on TPA.
     * @param userInfo  map with additional info for the non-fatal issue. Values for this and all children must be of the following types: String, Integer, Long, Double, Boolean, List and Map
     * @param throwable the throwable containing the stacktrace and message to report. If null, nothing will be reported.
     */
    void reportNonFatalIssue(@Nullable String reason, @Nullable Map<String, Object> userInfo, @NonNull Throwable throwable) {
        if (!getConfig().isNonFatalIssuesEnabled()) {
            return;
        }

        try {
            String report = makeNonFatalIssue(throwable, reason, userInfo);

            if (report == null) {
                return;
            }

            sendCrashReport(report, CRASH_REPORTING_KIND_ANDROID, false);
        } catch (JSONException ignored) {
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    void saveCrashReportToFile(@Nullable Thread thread, @NonNull Throwable throwable) {
        try {
            String filename = TPA_CRASHREPORT_PREFIX + UUID.randomUUID().toString() + TPA_CRASHREPORT_POSTFIX;
            TpaDebugging.log.d(TAG, "Writing unhandled exception to: " + crashReportDir.getAbsolutePath() + "/" + filename);

            File file = new File(crashReportDir, filename);
            String report = makeCrashReport(throwable);

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(report.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            TpaDebugging.log.e(TAG, "Exception happened while saving crash report", e);
        }
    }

    // Used by TPA CrossPlatform
    @SuppressWarnings("unused")
    void reportNonFatalIssue(@Nullable String stacktrace, @Nullable String reason, @Nullable Map<String, Object> userInfo, @Nullable String kind) {
        if (!getConfig().isNonFatalIssuesEnabled() || stacktrace == null || kind == null) {
            return;
        }

        try {
            String report = makeNonFatalIssue(stacktrace, reason, userInfo);

            if (report == null) {
                return;
            }

            sendCrashReport(report, kind, false);
        } catch (JSONException ex) {
            TpaDebugging.log.e(TAG, "Error parsing cross platform non fatal issue", ex);
        }
    }
}
