package com.orbita.aurora.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;

import com.orbita.aurora.R;
import com.orbita.aurora.browser.BrowserRuntimeManager;
import com.orbita.aurora.browser.BrowserSessionController;
import com.orbita.aurora.browser.BrowserTab;
import com.orbita.aurora.browser.TabManager;
import com.orbita.aurora.core.UrlNormalizer;
import com.orbita.aurora.download.BrowserDownloadManager;
import com.orbita.aurora.download.DownloadItem;
import com.orbita.aurora.privacy.PrivacyCoordinator;
import com.orbita.aurora.profile.DeviceProfile;
import com.orbita.aurora.profile.ProfileApplier;
import com.orbita.aurora.profile.ProfileRepository;
import com.orbita.aurora.settings.BrowserSettings;
import com.orbita.aurora.session.TemporarySessionManager;

import java.util.List;
import java.util.UUID;

/** Main browser UI wiring tabs, profiles, downloads, sessions, privacy and settings. */
public final class MainActivity extends Activity
        implements BrowserSessionController.Listener, TabManager.Listener,
        BrowserDownloadManager.Listener {

    private static final String START_URL = "https://www.mozilla.org/";
    private static final String NORMAL_CONTEXT = "orbita-normal";

    private GeckoView geckoView;
    private LinearLayout tabBar;
    private EditText addressBar;
    private Button backButton;
    private Button forwardButton;
    private Button reloadButton;
    private Button menuButton;
    private TextView statusText;
    private FrameLayout viewportHost;

    private GeckoRuntime runtime;
    private TabManager tabManager;
    private BrowserDownloadManager downloadManager;
    private PrivacyCoordinator privacyCoordinator;
    private BrowserSettings settings;
    private ViewportController viewportController;
    private TemporarySessionManager sessionManager;
    private DeviceProfile activeProfile;
    private List<DeviceProfile> profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runtime = BrowserRuntimeManager.getOrCreate(getApplicationContext());
        settings = new BrowserSettings(this);
        tabManager = new TabManager();
        tabManager.setListener(this);
        downloadManager = new BrowserDownloadManager(this, this);
        privacyCoordinator = new PrivacyCoordinator(runtime);

        geckoView = findViewById(R.id.geckoview);
        viewportHost = findViewById(R.id.viewport_host);
        tabBar = findViewById(R.id.tab_bar);
        addressBar = findViewById(R.id.address_bar);
        backButton = findViewById(R.id.back_button);
        forwardButton = findViewById(R.id.forward_button);
        reloadButton = findViewById(R.id.reload_button);
        menuButton = findViewById(R.id.menu_button);
        statusText = findViewById(R.id.status_text);
        Button newTabButton = findViewById(R.id.new_tab_button);

        viewportController = new ViewportController(this, viewportHost);
        sessionManager = new TemporarySessionManager();
        loadProfiles();
        wireUi(newTabButton);

        DeviceProfile selected = findProfile(settings.getSelectedProfileId());
        activeProfile = selected == null ? profiles.get(0) : selected;
        settings.setSelectedProfileId(activeProfile.getId());
        createTab(START_URL, false);
    }

    private void wireUi(Button newTabButton) {
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                loadAddress();
                return true;
            }
            return false;
        });
        backButton.setOnClickListener(v -> activeController().back());
        forwardButton.setOnClickListener(v -> activeController().forward());
        reloadButton.setOnClickListener(v -> activeController().reload());
        newTabButton.setOnClickListener(v -> createTab("about:blank", false));
        menuButton.setOnClickListener(v -> showBrowserMenu());
        addressBar.setSelectAllOnFocus(true);
    }

    private void loadProfiles() {
        try {
            profiles = ProfileRepository.loadProfiles(this);
            if (profiles.isEmpty()) throw new IllegalStateException("No device profiles found");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load device_profiles.json", e);
        }
    }

    private BrowserSessionController activeController() {
        BrowserTab tab = tabManager.getActiveTab();
        if (tab == null) throw new IllegalStateException("No active tab");
        return tab.getController();
    }

    private void createTab(String uri, boolean forceTemporary) {
        boolean temporary = forceTemporary || settings.isTemporarySession();
        String contextId = temporary ? sessionManager.createContextId() : sessionManager.normalContextId();
        GeckoSessionSettings sessionSettings = ProfileApplier.createSettings(
                activeProfile, contextId, temporary, settings.isTrackingProtectionEnabled());
        BrowserSessionController controller = new BrowserSessionController(sessionSettings, this);
        BrowserTab tab = new BrowserTab(
                UUID.randomUUID().toString(), controller, activeProfile, temporary,
                activeProfile.getResolutionPresets().isEmpty()
                        ? new DeviceProfile.ResolutionPreset(activeProfile.getViewportWidth(), activeProfile.getViewportHeight())
                        : activeProfile.getResolutionPresets().get(0));
        controller.open(runtime);
        controller.setJavascriptEnabled(settings.isJavascriptEnabled());
        tabManager.add(tab);
        showActiveTab();
        if (!"about:blank".equals(uri)) controller.loadUri(UrlNormalizer.normalize(uri));
    }

    private void showActiveTab() {
        BrowserTab tab = tabManager.getActiveTab();
        if (tab == null) return;
        geckoView.releaseSession();
        geckoView.setSession(tab.getController().getSession());
        tab.getController().setListener(this);
        addressBar.setText(tab.getUrl());
        statusText.setText(tab.isTemporary() ? "Temporary session" : tab.getProfile().getDisplayName());
        updateNavigationButtons(false, false);
        renderTabs();
        applyViewport(tab.getResolution());
    }

    private void applyViewport(DeviceProfile.ResolutionPreset preset) {
        BrowserTab tab = tabManager.getActiveTab();
        if (tab == null) return;
        tab.setResolution(preset);
        viewportController.resetToScreen();
        // Width/height are logical viewport layout values; the physical Android display is untouched.
        viewportController.apply(preset);
        statusText.setText(tab.getProfile().getDisplayName() + " • viewport " + preset);
    }

    private void renderTabs() {
        tabBar.removeAllViews();
        List<BrowserTab> tabs = tabManager.getTabs();
        int active = tabManager.getActiveIndex();
        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            BrowserTab tab = tabs.get(i);
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.HORIZONTAL);
            cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
            cell.setPadding(8, 0, 4, 0);
            cell.setBackgroundColor(i == active ? Color.DKGRAY : Color.TRANSPARENT);

            TextView title = new TextView(this);
            title.setText(tab.getTitle().isEmpty() ? "New tab" : tab.getTitle());
            title.setTextColor(Color.WHITE);
            title.setSingleLine(true);
            title.setMaxWidth(dp(170));
            title.setPadding(4, 0, 8, 0);
            title.setOnClickListener(v -> tabManager.activate(index));

            Button close = smallButton("×");
            close.setOnClickListener(v -> closeTab(index));
            cell.addView(title, new LinearLayout.LayoutParams(0, dp(42), 1f));
            cell.addView(close, new LinearLayout.LayoutParams(dp(40), dp(42)));
            tabBar.addView(cell, new LinearLayout.LayoutParams(dp(230), dp(48)));
        }
    }

    private Button smallButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(18);
        button.setBackgroundColor(Color.TRANSPARENT);
        return button;
    }

    private void closeTab(int index) {
        BrowserTab tab = tabManager.remove(index);
        if (tab == null) return;
        if (tabManager.getTabs().isEmpty()) createTab(START_URL, false);
        else showActiveTab();
    }

    private void loadAddress() {
        String normalized = UrlNormalizer.normalize(addressBar.getText().toString());
        addressBar.setText(normalized);
        activeController().loadUri(normalized);
    }

    private void showBrowserMenu() {
        String[] options = {
                "New tab",
                "Device profile",
                "User-Agent",
                "Viewport / resolution",
                "Downloads",
                "Temporary session",
                "Privacy",
                "Settings",
                "Copy URL",
                "Paste & go"
        };
        new AlertDialog.Builder(this)
                .setTitle("Orbita Aurora")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: createTab("about:blank", false); break;
                        case 1: showProfileDialog(); break;
                        case 2: showUserAgentDialog(); break;
                        case 3: showResolutionDialog(); break;
                        case 4: showDownloadsDialog(); break;
                        case 5: toggleTemporarySession(); break;
                        case 6: showPrivacyDialog(); break;
                        case 7: showSettingsDialog(); break;
                        case 8: copyCurrentUrl(); break;
                        case 9: pasteAndGo(); break;
                    }
                }).show();
    }

    private void showProfileDialog() {
        String[] names = new String[profiles.size()];
        int checked = 0;
        for (int i = 0; i < profiles.size(); i++) {
            names[i] = profiles.get(i).getDisplayName();
            if (profiles.get(i).getId().equals(activeProfile.getId())) checked = i;
        }
        final int[] selected = {checked};
        new AlertDialog.Builder(this)
                .setTitle("Device profile")
                .setSingleChoiceItems(names, checked, (dialog, which) -> selected[0] = which)
                .setPositiveButton("Apply", (dialog, which) -> applyProfile(profiles.get(selected[0])))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyProfile(DeviceProfile profile) {
        activeProfile = profile;
        settings.setSelectedProfileId(profile.getId());
        for (BrowserTab tab : tabManager.getTabs()) {
            tab.setProfile(profile);
            ProfileApplier.applyRuntimeSettings(tab.getController().getSession(), profile,
                    settings.isTrackingProtectionEnabled());
        }
        BrowserTab active = tabManager.getActiveTab();
        if (active != null) {
            if (!profile.getResolutionPresets().isEmpty()) active.setResolution(profile.getResolutionPresets().get(0));
            showActiveTab();
            toast("Profile applied: " + profile.getDisplayName());
        }
    }

    private void showUserAgentDialog() {
        BrowserTab tab = tabManager.getActiveTab();
        if (tab == null) return;
        List<DeviceProfile.UserAgentVariant> variants = tab.getProfile().getUserAgentVariants();
        if (variants.isEmpty()) {
            new AlertDialog.Builder(this).setMessage("No predefined User-Agent variants are available for this profile.").setPositiveButton("OK", null).show();
            return;
        }
        String[] names = new String[variants.size()];
        for (int i = 0; i < variants.size(); i++) names[i] = variants.get(i).getDisplayName();
        new AlertDialog.Builder(this)
                .setTitle("User-Agent")
                .setItems(names, (dialog, which) -> {
                    DeviceProfile.UserAgentVariant variant = variants.get(which);
                    tab.getController().applyUserAgent(variant.getValue(),
                            "desktop".equalsIgnoreCase(tab.getProfile().getDeviceClass())
                                    ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                                    : GeckoSessionSettings.USER_AGENT_MODE_MOBILE);
                    toast("User-Agent changed: " + variant.getDisplayName());
                }).show();
    }

    private void showResolutionDialog() {
        BrowserTab tab = tabManager.getActiveTab();
        if (tab == null) return;
        List<DeviceProfile.ResolutionPreset> presets = tab.getProfile().getResolutionPresets();
        String[] names = new String[presets.size() + 1];
        names[0] = "Fit physical screen (no emulation)";
        for (int i = 0; i < presets.size(); i++) names[i + 1] = presets.get(i).toString();
        new AlertDialog.Builder(this)
                .setTitle("Viewport / resolution")
                .setItems(names, (dialog, which) -> {
                    if (which == 0) viewportController.resetToScreen();
                    else applyViewport(presets.get(which - 1));
                    toast("Physical display resolution was not changed.");
                }).show();
    }

    private void showDownloadsDialog() {
        List<DownloadItem> items = downloadManager.getItems();
        StringBuilder text = new StringBuilder();
        if (items.isEmpty()) text.append("No downloads yet.");
        for (DownloadItem item : items) {
            text.append(item.getFileName()).append(" — ").append(item.getState());
            if (item.getLocalPath() != null) text.append("\n").append(item.getLocalPath());
            text.append("\n\n");
        }
        new AlertDialog.Builder(this)
                .setTitle("Downloads")
                .setMessage(text.toString())
                .setPositiveButton("Clear list", (dialog, which) -> downloadManager.clearCompleted())
                .setNegativeButton("Close", null)
                .show();
    }

    private void toggleTemporarySession() {
        boolean enabled = !settings.isTemporarySession();
        settings.setTemporarySession(enabled);
        toast(enabled ? "New tabs will use Temporary Session" : "Temporary Session disabled for new tabs");
        if (enabled) createTab("about:blank", true);
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy")
                .setItems(new String[] {"Clear current temporary session data", "Clear all site data"}, (dialog, which) -> {
                    BrowserTab tab = tabManager.getActiveTab();
                    if (which == 0 && tab != null && tab.isTemporary()) {
                        String contextId = tab.getController().getContextId();
                        int index = tabManager.getActiveIndex();
                        tabManager.remove(index);
                        privacyCoordinator.clearSessionData(contextId);
                        if (tabManager.getTabs().isEmpty()) createTab(START_URL, false);
                        else showActiveTab();
                        toast("Temporary session data cleared");
                    } else if (which == 1) {
                        tabManager.removeAll();
                        privacyCoordinator.clearAllSiteData();
                        createTab(START_URL, false);
                        toast("Site data cleared");
                    }
                }).show();
    }

    private void showSettingsDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        box.setPadding(pad, 0, pad, 0);
        CheckBox tracking = new CheckBox(this);
        tracking.setText("Tracking protection");
        tracking.setChecked(settings.isTrackingProtectionEnabled());
        CheckBox javascript = new CheckBox(this);
        javascript.setText("JavaScript");
        javascript.setChecked(settings.isJavascriptEnabled());
        box.addView(tracking);
        box.addView(javascript);
        new AlertDialog.Builder(this)
                .setTitle("Browser settings")
                .setView(box)
                .setPositiveButton("Save", (dialog, which) -> {
                    settings.setTrackingProtectionEnabled(tracking.isChecked());
                    settings.setJavascriptEnabled(javascript.isChecked());
                    for (BrowserTab tab : tabManager.getTabs()) {
                        tab.getController().setTrackingProtection(tracking.isChecked());
                        tab.getController().setJavascriptEnabled(javascript.isChecked());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void copyCurrentUrl() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("URL", addressBar.getText().toString()));
        toast("URL copied");
    }

    private void pasteAndGo() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip()) return;
        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
        addressBar.setText(text);
        loadAddress();
    }



    private boolean isActive(BrowserSessionController controller) {
        BrowserTab active = tabManager.getActiveTab();
        return active != null && active.getController() == controller;
    }

    private BrowserTab findTab(BrowserSessionController controller) {
        for (BrowserTab tab : tabManager.getTabs()) {
            if (tab.getController() == controller) return tab;
        }
        return null;
    }

    private DeviceProfile findProfile(String id) {
        if (id == null || profiles == null) return null;
        for (DeviceProfile profile : profiles) if (profile.getId().equals(id)) return profile;
        return null;
    }

    private void updateNavigationButtons(boolean canGoBack, boolean canGoForward) {
        if (backButton != null) backButton.setEnabled(canGoBack);
        if (forwardButton != null) forwardButton.setEnabled(canGoForward);
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onLocationChanged(BrowserSessionController source, String url) {
        runOnUiThread(() -> {
            BrowserTab tab = findTab(source);
            if (tab != null) {
                tab.setUrl(url);
                if (isActive(source)) addressBar.setText(url);
            }
        });
    }

    @Override public void onTitleChanged(BrowserSessionController source, String title) {
        BrowserTab tab = findTab(source);
        if (tab != null) { tab.setTitle(title); renderTabs(); }
    }

    @Override public void onNavigationStateChanged(BrowserSessionController source, boolean canGoBack, boolean canGoForward) {
        if (isActive(source)) runOnUiThread(() -> updateNavigationButtons(canGoBack, canGoForward));
    }

    @Override public void onPageLoading(BrowserSessionController source, boolean loading) {
        if (!isActive(source)) return;
        runOnUiThread(() -> {
            reloadButton.setText(loading ? "×" : "↻");
            statusText.setText(loading ? "Loading…" : (tabManager.getActiveTab() == null ? "" : tabManager.getActiveTab().getProfile().getDisplayName()));
        });
    }

    @Override public void onExternalResponse(BrowserSessionController source, WebResponse response) {
        downloadManager.handleExternalResponse(response);
        toast("Download started");
    }

    @Override public void onCloseRequest(BrowserSessionController source) {
        BrowserTab tab = findTab(source);
        if (tab != null) closeTab(tabManager.getTabs().indexOf(tab));
    }

    @Override public void onTabsChanged() { renderTabs(); }
    @Override public void onDownloadsChanged() { }

    @Override protected void onDestroy() {
        if (tabManager != null) tabManager.removeAll();
        super.onDestroy();
    }
}
