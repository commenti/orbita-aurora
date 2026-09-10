package com.orbita.aurora.settings;

/** Small settings model. UI and persistence are added in the advanced-settings phase. */
public final class BrowserSettings {
    private String selectedProfileId = "generic-mobile-gecko";
    private boolean temporarySession;

    public String getSelectedProfileId() { return selectedProfileId; }
    public void setSelectedProfileId(String value) { selectedProfileId = value; }
    public boolean isTemporarySession() { return temporarySession; }
    public void setTemporarySession(boolean value) { temporarySession = value; }
}
