package com.orbita.aurora.browser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Owns tab ordering and the active tab; UI is kept out of this class. */
public final class TabManager {
    public interface Listener { void onTabsChanged(); }

    private final List<BrowserTab> tabs = new ArrayList<>();
    private int activeIndex = -1;
    private Listener listener;

    public void setListener(Listener listener) { this.listener = listener; }
    public List<BrowserTab> getTabs() { return Collections.unmodifiableList(tabs); }
    public BrowserTab getActiveTab() { return activeIndex < 0 ? null : tabs.get(activeIndex); }
    public int getActiveIndex() { return activeIndex; }

    public void add(BrowserTab tab) {
        tabs.add(tab);
        activeIndex = tabs.size() - 1;
        changed();
    }

    public void activate(int index) {
        if (index < 0 || index >= tabs.size()) return;
        activeIndex = index;
        changed();
    }

    public BrowserTab remove(int index) {
        if (index < 0 || index >= tabs.size()) return null;
        BrowserTab removed = tabs.remove(index);
        removed.getController().close();
        if (tabs.isEmpty()) activeIndex = -1;
        else if (activeIndex > index) activeIndex--;
        else if (activeIndex >= tabs.size()) activeIndex = tabs.size() - 1;
        changed();
        return removed;
    }

    public void removeAll() {
        for (BrowserTab tab : tabs) tab.getController().close();
        tabs.clear();
        activeIndex = -1;
        changed();
    }

    private void changed() { if (listener != null) listener.onTabsChanged(); }
}
