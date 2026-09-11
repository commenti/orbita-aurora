package com.orbita.aurora.profile;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;

/** Maps a predefined profile to GeckoView settings without pretending to change physical hardware. */
public final class ProfileApplier {
    private ProfileApplier() {}

    public static GeckoSessionSettings createSettings(DeviceProfile profile, String contextId,
                                                       boolean privateMode, boolean trackingProtection) {
        GeckoSessionSettings.Builder builder = new GeckoSessionSettings.Builder()
                .usePrivateMode(privateMode)
                .contextId(contextId)
                .useTrackingProtection(trackingProtection)
                .viewportMode(isDesktop(profile)
                        ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                        : GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
                .userAgentMode(isDesktop(profile)
                        ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                        : GeckoSessionSettings.USER_AGENT_MODE_MOBILE);

        if (profile.getUserAgent() != null && !profile.getUserAgent().trim().isEmpty()) {
            builder.userAgentOverride(profile.getUserAgent());
        }
        return builder.build();
    }

    public static void applyRuntimeSettings(GeckoSession session, DeviceProfile profile,
                                             boolean trackingProtection) {
        if (profile.getUserAgent() != null && !profile.getUserAgent().trim().isEmpty()) {
            session.getSettings().setUserAgentOverride(profile.getUserAgent());
        } else {
            session.getSettings().setUserAgentOverride(null);
            session.getSettings().setUserAgentMode(isDesktop(profile)
                    ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                    : GeckoSessionSettings.USER_AGENT_MODE_MOBILE);
        }
        session.getSettings().setViewportMode(isDesktop(profile)
                ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                : GeckoSessionSettings.VIEWPORT_MODE_MOBILE);
        session.getSettings().setUseTrackingProtection(trackingProtection);
    }

    private static boolean isDesktop(DeviceProfile profile) {
        return "desktop".equalsIgnoreCase(profile.getDeviceClass());
    }
}
