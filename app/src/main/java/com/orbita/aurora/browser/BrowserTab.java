package com.orbita.aurora.browser;

import com.orbita.aurora.profile.DeviceProfile;

/** Browser tab state surrounding a single GeckoSession. */
public final class BrowserTab {
    private final String id;
    private final BrowserSessionController controller;
    private DeviceProfile profile;
    private String title = "New tab";
    private String url = "about:blank";
    private boolean temporary;
    private DeviceProfile.ResolutionPreset resolution;

    public BrowserTab(String id, BrowserSessionController controller, DeviceProfile profile,
                      boolean temporary, DeviceProfile.ResolutionPreset resolution) {
        this.id = id;
        this.controller = controller;
        this.profile = profile;
        this.temporary = temporary;
        this.resolution = resolution;
    }

    public String getId() { return id; }
    public BrowserSessionController getController() { return controller; }
    public DeviceProfile getProfile() { return profile; }
    public void setProfile(DeviceProfile profile) { this.profile = profile; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public boolean isTemporary() { return temporary; }
    public void setTemporary(boolean temporary) { this.temporary = temporary; }
    public DeviceProfile.ResolutionPreset getResolution() { return resolution; }
    public void setResolution(DeviceProfile.ResolutionPreset resolution) { this.resolution = resolution; }
}
