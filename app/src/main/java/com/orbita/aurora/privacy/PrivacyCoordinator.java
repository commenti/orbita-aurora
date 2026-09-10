package com.orbita.aurora.privacy;

/** Contract for app-controlled browser cleanup. OS/engine-retained forensic data is outside this contract. */
public interface PrivacyCoordinator {
    void clearSessionData(String sessionId);
}
