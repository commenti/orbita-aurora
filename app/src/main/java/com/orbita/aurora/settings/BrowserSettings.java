package com.orbita.aurora.settings;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent user-facing browser settings. */
public final class BrowserSettings {
    private static final String PREFS = "orbita_browser_settings";
    private static final String PROFILE = "selected_profile";
    private static final String TEMPORARY = "temporary_session";
    private static final String TRACKING = "tracking_protection";
    private static final String JAVASCRIPT = "javascript_enabled";

    private final SharedPreferences preferences;

    public BrowserSettings(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getSelectedProfileId() {
        return preferences.getString(PROFILE, "generic-mobile-gecko");
    }

    public void setSelectedProfileId(String value) {
        preferences.edit().putString(PROFILE, value).apply();
    }

    public boolean isTemporarySession() {
        return preferences.getBoolean(TEMPORARY, false);
    }

    public void setTemporarySession(boolean value) {
        preferences.edit().putBoolean(TEMPORARY, value).apply();
    }

    public boolean isTrackingProtectionEnabled() {
        return preferences.getBoolean(TRACKING, true);
    }

    public void setTrackingProtectionEnabled(boolean value) {
        preferences.edit().putBoolean(TRACKING, value).apply();
    }

    public boolean isJavascriptEnabled() {
        return preferences.getBoolean(JAVASCRIPT, true);
    }

    public void setJavascriptEnabled(boolean value) {
        preferences.edit().putBoolean(JAVASCRIPT, value).apply();
    }
}
