package com.orbita.aurora.download;

/** Download state model; Android storage integration is implemented in the download phase. */
public final class DownloadItem {
    public enum State { PENDING, RUNNING, COMPLETE, FAILED, CANCELLED }

    private final String id;
    private final String url;
    private State state;

    public DownloadItem(String id, String url, State state) {
        this.id = id;
        this.url = url;
        this.state = state;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
}
