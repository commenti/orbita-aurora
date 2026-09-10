package com.orbita.aurora.ui;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;


import org.mozilla.geckoview.GeckoView;

import com.orbita.aurora.R;
import com.orbita.aurora.browser.BrowserRuntimeManager;
import com.orbita.aurora.browser.BrowserSessionController;
import com.orbita.aurora.core.UrlNormalizer;

/** First runnable UI milestone: one GeckoView tab with address/navigation controls. */
public final class MainActivity extends android.app.Activity implements BrowserSessionController.Listener {
    private EditText addressBar;
    private Button backButton;
    private Button forwardButton;
    private BrowserSessionController browserSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GeckoView geckoView = findViewById(R.id.geckoview);
        addressBar = findViewById(R.id.address_bar);
        backButton = findViewById(R.id.back_button);
        forwardButton = findViewById(R.id.forward_button);
        Button reloadButton = findViewById(R.id.reload_button);

        browserSession = new BrowserSessionController(this);
        browserSession.open(BrowserRuntimeManager.getOrCreate(getApplicationContext()));
        geckoView.setSession(browserSession.getSession());

        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadAddress();
                return true;
            }
            return false;
        });
        backButton.setOnClickListener(v -> browserSession.back());
        forwardButton.setOnClickListener(v -> browserSession.forward());
        reloadButton.setOnClickListener(v -> browserSession.reload());

        updateNavigationButtons(false, false);
        addressBar.setText("https://www.mozilla.org/");
        browserSession.loadUri("https://www.mozilla.org/");
    }

    private void loadAddress() {
        String normalized = UrlNormalizer.normalize(addressBar.getText().toString());
        addressBar.setText(normalized);
        browserSession.loadUri(normalized);
    }

    @Override
    public void onLocationChanged(String url) {
        if (url == null || url.isEmpty()) return;
        addressBar.post(() -> addressBar.setText(url));
    }

    @Override
    public void onNavigationStateChanged(boolean canGoBack, boolean canGoForward) {
        updateNavigationButtons(canGoBack, canGoForward);
    }

    @Override
    public void onPageLoading(boolean loading) {
        // Loading indicator UI is intentionally deferred to the UI polish phase.
    }

    private void updateNavigationButtons(boolean canGoBack, boolean canGoForward) {
        if (backButton != null) backButton.setEnabled(canGoBack);
        if (forwardButton != null) forwardButton.setEnabled(canGoForward);
    }

    @Override
    protected void onDestroy() {
        if (browserSession != null) {
            GeckoView geckoView = findViewById(R.id.geckoview);
            if (geckoView != null && browserSession.getSession().isOpen()) {
                geckoView.releaseSession();
            }
            browserSession.close();
            browserSession = null;
        }
        super.onDestroy();
    }
}
