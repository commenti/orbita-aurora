package com.orbita.aurora.profile;

/** Immutable, engine-honest browser profile data. Physical hardware is deliberately not represented. */
public final class DeviceProfile {
    private final String id;
    private final String displayName;
    private final String deviceClass;
    private final String osFamily;
    private final String browserFamily;
    private final int viewportWidth;
    private final int viewportHeight;
    private final float devicePixelRatio;
    private final String orientation;

    public DeviceProfile(String id, String displayName, String deviceClass, String osFamily,
                         String browserFamily, int viewportWidth, int viewportHeight,
                         float devicePixelRatio, String orientation) {
        this.id = id;
        this.displayName = displayName;
        this.deviceClass = deviceClass;
        this.osFamily = osFamily;
        this.browserFamily = browserFamily;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.devicePixelRatio = devicePixelRatio;
        this.orientation = orientation;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDeviceClass() { return deviceClass; }
    public String getOsFamily() { return osFamily; }
    public String getBrowserFamily() { return browserFamily; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }
    public float getDevicePixelRatio() { return devicePixelRatio; }
    public String getOrientation() { return orientation; }
}
