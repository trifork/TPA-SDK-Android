package io.tpa.tpalib.distribution;

/**
 * Provides a common interface between an async task
 * call back and a plain dialog displaying previously
 * retrieved data.
 */
public interface ShowsUpdate {

    enum State {
        RUN,
        DISPLAY,
        DONE
    }

    AppList getUpdateInfo();

    State getState();

    void safeDismiss();
}
