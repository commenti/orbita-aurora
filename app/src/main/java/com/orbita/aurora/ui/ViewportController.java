package com.orbita.aurora.ui;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.orbita.aurora.profile.DeviceProfile;

/**
 * Controls the logical GeckoView viewport container. This never changes or claims to change
 * the Android device's physical display resolution.
 */
public final class ViewportController {
    private final FrameLayout host;
    private final android.content.Context context;

    public ViewportController(android.content.Context context, FrameLayout host) {
        this.context = context;
        this.host = host;
    }

    public void apply(DeviceProfile.ResolutionPreset preset) {
        if (preset == null) return;
        int width = dp(preset.getWidth());
        int height = dp(preset.getHeight());
        ViewGroup.LayoutParams params = host.getLayoutParams();
        params.width = width;
        params.height = height;
        host.setLayoutParams(params);
        host.requestLayout();
    }

    public void resetToScreen() {
        ViewGroup.LayoutParams params = host.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        host.setLayoutParams(params);
        host.requestLayout();
    }

    private int dp(int cssPx) {
        return Math.max(1, Math.round(cssPx * context.getResources().getDisplayMetrics().density));
    }
}
