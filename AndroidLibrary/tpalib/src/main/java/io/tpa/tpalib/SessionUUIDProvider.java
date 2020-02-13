package io.tpa.tpalib;

import android.support.annotation.Nullable;

interface SessionUUIDProvider {

    /**
     * Returns the current SessionUUID or null if not in a session.
     *
     * @return the current SessionUUID
     */
    @Nullable
    String getSessionUUID();
}
