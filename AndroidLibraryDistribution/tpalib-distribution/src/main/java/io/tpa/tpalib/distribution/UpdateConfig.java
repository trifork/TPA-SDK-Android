package io.tpa.tpalib.distribution;

public class UpdateConfig {

    private static boolean debug = false;

    private static String versionsUrl = null;

    public static String getVersionsUrl() {
        return versionsUrl;
    }

    public static void setVersionsUrl(String url) {
        versionsUrl = url;
    }

    public static boolean debug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        UpdateConfig.debug = debug;
    }
}
