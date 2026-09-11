package com.orbita.aurora.privacy;

import org.mozilla.geckoview.GeckoRuntime;

/** Coordinates app-managed and GeckoView session-context cleanup. */
public final class PrivacyCoordinator {
    private final GeckoRuntime runtime;

    public PrivacyCoordinator(GeckoRuntime runtime) {
        this.runtime = runtime;
    }

    public void clearSessionData(String sessionContextId) {
        if (sessionContextId == null || sessionContextId.isEmpty()) return;
        runtime.getStorageController().clearDataForSessionContext(sessionContextId);
    }

    public void clearAllSiteData() {
        runtime.getStorageController().clearData(
                org.mozilla.geckoview.StorageController.ClearFlags.SITE_DATA);
    }
}
