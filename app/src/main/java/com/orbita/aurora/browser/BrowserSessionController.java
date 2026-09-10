package com.orbita.aurora.browser;


import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSession;

import java.util.List;

/** Owns one browser tab/session and its GeckoView delegates. */
public final class BrowserSessionController {
    public interface Listener {
        void onLocationChanged(String url);
        void onNavigationStateChanged(boolean canGoBack, boolean canGoForward);
        void onPageLoading(boolean loading);
    }

    private final GeckoSession session = new GeckoSession();
    private Listener listener;
    private boolean opened;

    public BrowserSessionController(Listener listener) {
        this.listener = listener;
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public void onLocationChange(GeckoSession session, String url,
                                          List<GeckoSession.PermissionDelegate.ContentPermission> perms,
                                          Boolean hasUserGesture) {
                if (BrowserSessionController.this.listener != null) {
                    BrowserSessionController.this.listener.onLocationChanged(url == null ? "" : url);
                }
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
                if (BrowserSessionController.this.listener != null) {
                    BrowserSessionController.this.listener.onPageLoading(true);
                    BrowserSessionController.this.listener.onLocationChanged(url);
                }
            }

            @Override
            public void onPageStop(GeckoSession session, boolean success) {
                if (BrowserSessionController.this.listener != null) {
                    BrowserSessionController.this.listener.onPageLoading(false);
                }
            }
        });
        // Mozilla documents this empty delegate as a workaround for GeckoView bug 1758212.
        session.setContentDelegate(new GeckoSession.ContentDelegate() {});
    }

    private boolean lastCanGoBack;
    private boolean lastCanGoForward;

    private void notifyNavigationState() {
        if (listener != null) {
            listener.onNavigationStateChanged(lastCanGoBack, lastCanGoForward);
        }
    }

    public void open(GeckoRuntime runtime) {
        if (!opened) {
            session.open(runtime);
            opened = true;
        }
    }

    public GeckoSession getSession() {
        return session;
    }

    public void loadUri(String uri) {
        if (!opened) {
            throw new IllegalStateException("GeckoSession must be opened before loading a URI");
        }
        session.loadUri(uri);
    }

    public void back() {
        session.goBack(true);
    }

    public void forward() {
        session.goForward(true);
    }

    public void reload() {
        session.reload(GeckoSession.LOAD_FLAGS_NONE);
    }

    public boolean isOpen() {
        return opened;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void close() {
        if (opened) {
            session.close();
            opened = false;
        }
    }
}
