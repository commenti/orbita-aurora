package com.orbita.aurora.browser;

import android.content.Context;

import org.mozilla.geckoview.GeckoRuntime;

/** Owns the process-wide GeckoRuntime. GeckoView permits one runtime per process. */
public final class BrowserRuntimeManager {
    private static GeckoRuntime runtime;

    private BrowserRuntimeManager() {}

    public static synchronized GeckoRuntime getOrCreate(Context context) {
        if (runtime == null) {
            runtime = GeckoRuntime.create(context.getApplicationContext());
        }
        return runtime;
    }
}
