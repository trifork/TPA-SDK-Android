package io.tpa.tpalib;

import java.util.Map;

public class TPACrossPlatformIssueReporting {

    public static void reportNonFatalIssue(String stacktrace, TPASupportedPlatforms kind) {
        reportNonFatalIssue(stacktrace, null, kind);
    }

    public static void reportNonFatalIssue(String stacktrace, String reason, TPASupportedPlatforms kind) {
        reportNonFatalIssue(stacktrace, reason, null, kind);
    }

    public static void reportNonFatalIssue(String stacktrace, String reason, Map<String, Object> userInfo, TPASupportedPlatforms kind) {
        if (kind == null) {
            return;
        }

        TPAManager.getInstance().reportNonFatalIssue(stacktrace, reason, userInfo, kind.name());
    }
}
