package com.orbita.aurora.browser;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.WebResponse;

import java.util.List;

/** Owns one GeckoSession. One instance maps to one browser tab. */
public final class BrowserSessionController {
    public interface Listener {
        void onLocationChanged(BrowserSessionController source, String url);
        void onTitleChanged(BrowserSessionController source, String title);
        void onNavigationStateChanged(BrowserSessionController source, boolean canGoBack, boolean canGoForward);
        void onPageLoading(BrowserSessionController source, boolean loading);
        void onExternalResponse(BrowserSessionController source, WebResponse response);
        void onCloseRequest(BrowserSessionController source);
    }

    private final GeckoSession session;
    private Listener listener;
    private boolean opened;
    private boolean lastCanGoBack;
    private boolean lastCanGoForward;

    public BrowserSessionController(GeckoSessionSettings settings, Listener listener) {
        this.listener = listener;
        session = new GeckoSession(settings);
        installDelegates();
    }

    private void installDelegates() {
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public void onLocationChange(GeckoSession session, String url,
                                          List<GeckoSession.PermissionDelegate.ContentPermission> perms,
                                          Boolean hasUserGesture) {
                if (listener != null) listener.onLocationChanged(BrowserSessionController.this, url == null ? "" : url);
            }

            @Override
            public void onCanGoBack(GeckoSession session, boolean canGoBack) {
                lastCanGoBack = canGoBack;
                notifyNavigationState();
            }

            @Override
            public void onCanGoForward(GeckoSession session, boolean canGoForward) {
                lastCanGoForward = canGoForward;
                notifyNavigationState();
            }
        });

        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(GeckoSession session, String url) {
                if (listener != null) {
                    listener.onPageLoading(BrowserSessionController.this, true);
                    if (url != null) listener.onLocationChanged(BrowserSessionController.this, url);
                }
            }

            @Override
            public void onPageStop(GeckoSession session, boolean success) {
                if (listener != null) listener.onPageLoading(BrowserSessionController.this, false);
            }
        });

        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession session, String title) {
                if (listener != null) listener.onTitleChanged(BrowserSessionController.this, title == null ? "" : title);
            }

            @Override
            public void onExternalResponse(GeckoSession session, WebResponse response) {
                if (listener != null) listener.onExternalResponse(BrowserSessionController.this, response);
            }

            @Override
            public void onCloseRequest(GeckoSession session) {
                if (listener != null) listener.onCloseRequest(BrowserSessionController.this);
            }
        });
    }

    private void notifyNavigationState() {
        if (listener != null) listener.onNavigationStateChanged(this, lastCanGoBack, lastCanGoForward);
    }

    public void open(GeckoRuntime runtime) {
        if (!opened) {
            session.open(runtime);
            opened = true;
        }
    }

    public GeckoSession getSession() { return session; }

    public void loadUri(String uri) {
        if (!opened) throw new IllegalStateException("GeckoSession must be opened before loading");
        session.loadUri(uri);
    }

    public void back() { if (lastCanGoBack) session.goBack(true); }
    public void forward() { if (lastCanGoForward) session.goForward(true); }
    public void reload() { session.reload(GeckoSession.LOAD_FLAGS_NONE); }
    public boolean isOpen() { return opened; }
    public void setListener(Listener listener) { this.listener = listener; }

    public void applyUserAgent(String override, int mode) {
        if (override != null && !override.trim().isEmpty()) {
            session.getSettings().setUserAgentOverride(override);
        } else {
            session.getSettings().setUserAgentOverride(null);
            session.getSettings().setUserAgentMode(mode);
        }
        if (opened) reload();
    }

    public void applyViewportMode(int mode) {
        session.getSettings().setViewportMode(mode);
        if (opened) reload();
    }

    public void setTrackingProtection(boolean enabled) {
        session.getSettings().setUseTrackingProtection(enabled);
    }

    public void setJavascriptEnabled(boolean enabled) {
        session.getSettings().setAllowJavascript(enabled);
        if (opened) reload();
    }

    public String getContextId() { return session.getSettings().getContextId(); }

    public void close() {
        if (opened) {
            session.close();
            opened = false;
        }
    }
}
