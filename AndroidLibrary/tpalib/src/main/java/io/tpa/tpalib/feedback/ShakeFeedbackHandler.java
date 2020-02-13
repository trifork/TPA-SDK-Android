package io.tpa.tpalib.feedback;

/**
 * Implement this interface to implement handle the shake feedback manually.
 *
 * @see io.tpa.tpalib.TpaConfiguration.Builder#useShakeFeedback(boolean, ShakeFeedbackHandler)
 */
public interface ShakeFeedbackHandler {

    void handleShakeFeedback();
}
