package com.orbita.aurora.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable, engine-honest predefined browser profile. Physical hardware is never claimed to change. */
public final class DeviceProfile {
    public static final class ResolutionPreset {
        private final int width;
        private final int height;

        public ResolutionPreset(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() { return width; }
        public int getHeight() { return height; }
        @Override public String toString() { return width + " x " + height; }
    }

    public static final class UserAgentVariant {
        private final String id;
        private final String displayName;
        private final String value;

        public UserAgentVariant(String id, String displayName, String value) {
            this.id = id;
            this.displayName = displayName;
            this.value = value;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getValue() { return value; }
    }

    private final String id;
    private final String displayName;
    private final String deviceClass;
    private final String osFamily;
    private final String browserFamily;
    private final String userAgent;
    private final int viewportWidth;
    private final int viewportHeight;
    private final float devicePixelRatio;
    private final String orientation;
    private final List<ResolutionPreset> resolutions;
    private final List<UserAgentVariant> userAgentVariants;

    public DeviceProfile(String id, String displayName, String deviceClass, String osFamily,
                         String browserFamily, String userAgent, int viewportWidth, int viewportHeight,
                         float devicePixelRatio, String orientation,
                         List<ResolutionPreset> resolutions, List<UserAgentVariant> userAgentVariants) {
        this.id = id;
        this.displayName = displayName;
        this.deviceClass = deviceClass;
        this.osFamily = osFamily;
        this.browserFamily = browserFamily;
        this.userAgent = userAgent;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.devicePixelRatio = devicePixelRatio;
        this.orientation = orientation;
        this.resolutions = Collections.unmodifiableList(new ArrayList<>(resolutions));
        this.userAgentVariants = Collections.unmodifiableList(new ArrayList<>(userAgentVariants));
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDeviceClass() { return deviceClass; }
    public String getOsFamily() { return osFamily; }
    public String getBrowserFamily() { return browserFamily; }
    public String getUserAgent() { return userAgent; }
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }
    public float getDevicePixelRatio() { return devicePixelRatio; }
    public String getOrientation() { return orientation; }
    public List<ResolutionPreset> getResolutionPresets() { return resolutions; }
    public List<UserAgentVariant> getUserAgentVariants() { return userAgentVariants; }
}
