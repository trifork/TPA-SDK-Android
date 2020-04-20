package io.tpa.tpalib;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import io.tpa.tpalib.feedback.FeedbackInvocation;
import io.tpa.tpalib.feedback.ShakeFeedbackHandler;

class Config {

    private static final String TAG = "Config";

    @NonNull
    private final static String FCT_BASE_V5 = "/api/v5/project/";

    @NonNull
    private String packageName;
    @NonNull
    private TpaConfiguration configuration;
    @NonNull
    private String apiURL;

    Config(@NonNull String packageName) throws ConfigInitializationException {
        try {
            configuration = loadFromProperties();
        } catch (Exception ex) {
            throw new ConfigInitializationException(ex);
        }
        this.packageName = packageName;
        this.apiURL = apiURLFromServerURL(configuration.getServerUrl());
    }

    Config(@NonNull TpaConfiguration configuration, @NonNull String packageName) throws ConfigInitializationException {
        this.configuration = configuration;
        this.packageName = packageName;
        this.apiURL = apiURLFromServerURL(configuration.getServerUrl());
    }

    @NonNull
    private String apiURLFromServerURL(@NonNull String serverUrl) {
        try {
            URL url = new URL(serverUrl);
            String apiHost = url.getHost().replaceFirst("\\w*\\.", "api.");
            return serverUrl.replace(url.getHost(), apiHost);
        } catch (MalformedURLException ex) {
            return serverUrl;
        }
    }

    @NonNull
    private String trimTrailingSlash(@NonNull String str) {
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    @NonNull
    private TpaConfiguration loadFromProperties() throws InterruptedException, ExecutionException, ConfigInitializationException {
        AsyncTask<Void, Void, Properties> propertiesAsyncTask = AsyncHelper.executeAsyncTask(new LoadPropertiesTask());
        if (propertiesAsyncTask == null) {
            throw new ConfigInitializationException("Could not create AsyncTask to read app properties, ignoring.");
        }
        Properties p = propertiesAsyncTask.get();
        if (p == null) {
            throw new ConfigInitializationException("Could not read app properties, ignoring.");
        }

        String projectUUID = p.getProperty("tpa.crashreporting.uuid");
        if (projectUUID == null) {
            throw new ConfigInitializationException("Missing ProjectUUID in build.properties. Make sure it is set with the key 'tpa.crashreporting.uuid'");
        }
        String serverURL = p.getProperty("tpa.crashreporting.server_url");
        if (serverURL == null) {
            throw new ConfigInitializationException("Missing ServerURL in build.properties. Make sure it is set with the key 'tpa.crashreporting.server_url'");
        }

        TpaConfiguration.Builder configurationBuilder = new TpaConfiguration.Builder(projectUUID, serverURL);

        String debug_txt = p.getProperty("tpa.debug");
        boolean debug = false;
        if (debug_txt != null && "TRUE".equalsIgnoreCase(debug_txt.trim())) {
            debug = true;
        }
        configurationBuilder.enableDebug(debug);

        configurationBuilder.setLoggingDestination(LoggingDestination.fromString(p.getProperty("tpa.crashreporting.logging"), debug));

        String feedbackInvocation_txt = p.getProperty("tpa.feedback.invocation");
        FeedbackInvocation feedbackInvocation = FeedbackInvocation.DISABLED;
        if (feedbackInvocation_txt != null) {
            if ("EVENT_SHAKE".equalsIgnoreCase(feedbackInvocation_txt.trim()) ||
                    "EVENT-SHAKE".equalsIgnoreCase(feedbackInvocation_txt.trim()) ||
                    "EVENTSHAKE".equalsIgnoreCase(feedbackInvocation_txt.trim()) ||
                    "SHAKE".equalsIgnoreCase(feedbackInvocation_txt.trim())) {
                feedbackInvocation = FeedbackInvocation.EVENT_SHAKE;
            } else if ("ENABLED".equalsIgnoreCase(feedbackInvocation_txt.trim())) {
                feedbackInvocation = FeedbackInvocation.ENABLED;
            }
        }
        configurationBuilder.setFeedbackInvocation(feedbackInvocation);

        String crashHandling_txt = p.getProperty("tpa.crashreporting.handling");
        CrashHandling crashHandling = CrashHandling.DISABLED;
        if (crashHandling_txt != null) {
            if ("ALWAYS_SEND".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "ALWAYS-SEND".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "ALWAYSSEND".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "SEND".equalsIgnoreCase(crashHandling_txt.trim())) {
                crashHandling = CrashHandling.ALWAYS_SEND;
            } else if ("ALWAYS_ASK".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "ALWAYS-ASK".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "ALWAYSASK".equalsIgnoreCase(crashHandling_txt.trim()) ||
                    "ASK".equalsIgnoreCase(crashHandling_txt.trim())) {
                crashHandling = CrashHandling.ALWAYS_ASK;
            }
        }
        configurationBuilder.setCrashHandling(crashHandling);

        return configurationBuilder.build();
    }

    @Nullable
    private String getProjectUUID() {
        return configuration.getProjectUuid();
    }

    @NonNull
    private String getServerURL() {
        return trimTrailingSlash(configuration.getServerUrl());
    }

    @NonNull
    private String getApiURL() {
        return trimTrailingSlash(apiURL);
    }

    @NonNull
    String getVersionsUrl() {
        return getServerURL() + FCT_BASE_V5 + getProjectUUID() + "/app/"
                + packageName + "/versions";
    }

    @Nullable
    URL getProtobufPostUrl() {
        String url = getApiURL() + FCT_BASE_V5 + getProjectUUID() + "/app/" + packageName + "/upload";
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            if (debug()) {
                Log.e(TAG, "Malformed URL: ", e);
            }
        }
        return null;
    }

    @NonNull
    LoggingDestination getLoggingDestination() {
        return configuration.getLoggingDestination();
    }

    LogLevel getMinimumLogLevelConsole() {
        return configuration.getMinimumLogLevelConsole();
    }

    LogLevel getMinimumLogLevelRemote() {
        return configuration.getMinimumLogLevelRemote();
    }

    boolean useSimpleUpdate() {
        return configuration.useBasicUpdateDialog();
    }

    @NonNull
    FeedbackInvocation getFeedbackInvocation() {
        return configuration.getFeedbackInvocation();
    }

    @NonNull
    CrashHandling getCrashHandling() {
        return configuration.getCrashHandling();
    }

    boolean isNonFatalIssuesEnabled() {
        return configuration.isNonFatalIssuesEnabled();
    }

    boolean debug() {
        return configuration.isDebugEnabled();
    }

    boolean isAnalyticsEnabled() {
        return configuration.isAnalyticsEnabled();
    }

    boolean shouldAutoTrackScreen() {
        return configuration.autoTrackScreen();
    }

    boolean isAutomaticUpdateCheckEnabled() {
        return configuration.isAutomaticUpdateCheckEnabled();
    }

    @Nullable
    ShakeFeedbackHandler getFeedbackHandler() {
        return configuration.getShakeFeedbackHandler();
    }

    @Nullable
    Thread.UncaughtExceptionHandler popUncaughtExceptionHandler() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = configuration.getUncaughtExceptionHandler();
        configuration.clearUncaughtExceptionHandler();
        return uncaughtExceptionHandler;
    }

    private static class LoadPropertiesTask extends AsyncTask<Void, Void, Properties> {

        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        @Override
        protected Properties doInBackground(Void... params) {
            Properties p = new Properties();
            InputStream inputStream = Config.class.getResourceAsStream("/build.properties");
            try {
                p.load(inputStream);
                return p;
            } catch (IOException e) {
                return null;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    static class ConfigInitializationException extends RuntimeException {

        ConfigInitializationException(String message) {
            super(message);
        }

        ConfigInitializationException(Throwable cause) {
            super(cause);
        }
    }
}
