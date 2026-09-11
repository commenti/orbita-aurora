package com.orbita.aurora.download;

/** App-managed download state. */
public final class DownloadItem {
    public enum State { PENDING, RUNNING, COMPLETE, FAILED, CANCELLED }

    private final String id;
    private final String url;
    private final String fileName;
    private final long totalBytes;
    private State state;
    private long downloadedBytes;
    private String localPath;

    public DownloadItem(String id, String url, String fileName, long totalBytes) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.totalBytes = totalBytes;
        this.state = State.PENDING;
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getFileName() { return fileName; }
    public long getTotalBytes() { return totalBytes; }
    public State getState() { return state; }
    public long getDownloadedBytes() { return downloadedBytes; }
    public String getLocalPath() { return localPath; }
    public void setState(State state) { this.state = state; }
    public void setDownloadedBytes(long value) { downloadedBytes = value; }
    public void setLocalPath(String value) { localPath = value; }
}
