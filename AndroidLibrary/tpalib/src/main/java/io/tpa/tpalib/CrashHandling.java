package io.tpa.tpalib;

public enum CrashHandling {
    /**
     * When a crash is captured ask the user if they want to send it to the TPA server
     */
    ALWAYS_ASK,

    /**
     * Send captured crash reports silently to the TPA server without asking the user
     *
     * @deprecated use {@link CrashHandling#ALWAYS_SEND}
     */
    @Deprecated
    SILENT,
    /**
     * Always send captured crash reports to the TPA server without asking the user
     */
    ALWAYS_SEND,

    /**
     * Disable TPA crash report handling
     */
    DISABLED;

    boolean isDisabled() {
        return this == DISABLED || this == SILENT;
    }
}
