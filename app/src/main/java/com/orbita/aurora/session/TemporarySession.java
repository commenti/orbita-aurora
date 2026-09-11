package com.orbita.aurora.session;

/** Domain model for a temporary browser session. Persistence and data cleanup are phase-owned. */
public final class TemporarySession {
    private final String id;
    private final long createdAtEpochMs;

    public TemporarySession(String id, long createdAtEpochMs) {
        this.id = id;
        this.createdAtEpochMs = createdAtEpochMs;
    }

    public String getId() { return id; }
    public long getCreatedAtEpochMs() { return createdAtEpochMs; }
}
