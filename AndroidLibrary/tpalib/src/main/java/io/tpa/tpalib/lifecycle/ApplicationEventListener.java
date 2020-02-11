package io.tpa.tpalib.lifecycle;

/**
 * Interface for monitoring start and stop of an
 * Adndroid application.
 */
public interface ApplicationEventListener {

    /**
     * Called when the applications first onResumed()
     * has been called. It is NOT called when the
     * transition in from onPaused() to onResumed() i.e.
     * when the phone has been inactive for a while.
     */
    void started();

    /**
     * Called after the application last activity has
     * been stopped.
     */
    void ending();
}
