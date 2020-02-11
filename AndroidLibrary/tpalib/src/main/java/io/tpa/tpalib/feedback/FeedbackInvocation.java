package io.tpa.tpalib.feedback;

import io.tpa.tpalib.TPA;

public enum FeedbackInvocation {
    /**
     * All feedback is disabled, calls to {@link TPA#startFeedback()} will be ignored.
     */
    DISABLED,
    /**
     * Feedback is enabled, activate by calling {@link TPA#startFeedback()}.
     */
    ENABLED,
    /**
     * Feedback is enabled, activate by shaking the device or calling {@link TPA#startFeedback()}.
     */
    EVENT_SHAKE
}
