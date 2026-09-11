package com.orbita.aurora.session;

import java.util.UUID;

/** Creates isolated temporary Gecko session contexts. */
public final class TemporarySessionManager {
    public String createContextId() {
        return "tmp-" + UUID.randomUUID();
    }

    public String normalContextId() {
        return "orbita-normal";
    }
}
