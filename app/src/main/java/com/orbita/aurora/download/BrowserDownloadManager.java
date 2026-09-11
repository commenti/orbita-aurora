package com.orbita.aurora.download;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Environment;
import android.provider.MediaStore;

import org.mozilla.geckoview.WebResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Receives GeckoView external responses and persists user downloads. */
public final class BrowserDownloadManager {
    public interface Listener { void onDownloadsChanged(); }

    private final Context context;
    private final List<DownloadItem> items = new ArrayList<>();
    private Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BrowserDownloadManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void setListener(Listener listener) { this.listener = listener; }
    public List<DownloadItem> getItems() { return Collections.unmodifiableList(items); }

    public boolean handleExternalResponse(WebResponse response) {
        if (response == null || response.body == null) return false;
        String url = response.uri == null ? "download" : response.uri;
        String fileName = resolveFileName(response, url);
        DownloadItem item = new DownloadItem(UUID.randomUUID().toString(), url, fileName, -1);
        synchronized (items) { items.add(0, item); }
        notifyChanged();

        new Thread(() -> saveResponse(item, response), "orbita-download").start();
        return true;
    }

    public void clearCompleted() {
        synchronized (items) {
            items.removeIf(item -> item.getState() == DownloadItem.State.COMPLETE
                    || item.getState() == DownloadItem.State.FAILED
                    || item.getState() == DownloadItem.State.CANCELLED);
        }
        notifyChanged();
    }

    private void saveResponse(DownloadItem item, WebResponse response) {
        item.setState(DownloadItem.State.RUNNING);
        notifyChanged();
        try (InputStream input = response.body) {
            if (Build.VERSION.SDK_INT >= 29) {
                saveViaMediaStore(item, input);
            } else {
                saveToAppDownloads(item, input);
            }
            item.setState(DownloadItem.State.COMPLETE);
        } catch (Exception e) {
            item.setState(DownloadItem.State.FAILED);
        }
        notifyChanged();
    }

    private void saveViaMediaStore(DownloadItem item, InputStream input) throws Exception {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, item.getFileName());
        values.put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Orbita Aurora");
        values.put(MediaStore.Downloads.IS_PENDING, 1);
        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IllegalStateException("Unable to create download destination");
        try (OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException("Unable to open download destination");
            copy(input, output, item);
        }
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        resolver.update(uri, values, null, null);
        item.setLocalPath(uri.toString());
    }

    private void saveToAppDownloads(DownloadItem item, InputStream input) throws Exception {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (directory == null) throw new IllegalStateException("Downloads directory unavailable");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create downloads directory");
        }
        File target = new File(directory, item.getFileName());
        try (OutputStream output = new FileOutputStream(target)) {
            copy(input, output, item);
        }
        item.setLocalPath(target.getAbsolutePath());
    }

    private static void copy(InputStream input, OutputStream output, DownloadItem item) throws Exception {
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
            total += read;
            item.setDownloadedBytes(total);
        }
        output.flush();
    }

    private static String resolveFileName(WebResponse response, String url) {
        String disposition = response.headers == null ? null : response.headers.get("content-disposition");
        if (disposition != null) {
            String lower = disposition.toLowerCase();
            int index = lower.indexOf("filename=");
            if (index >= 0) {
                String value = disposition.substring(index + 9).trim().replace("\"", "");
                if (!value.isEmpty()) return sanitize(value);
            }
        }
        try {
            String path = new URL(url).getPath();
            if (path != null && !path.isEmpty()) {
                int slash = path.lastIndexOf('/');
                if (slash >= 0 && slash + 1 < path.length()) return sanitize(path.substring(slash + 1));
            }
        } catch (Exception ignored) { }
        return "download-" + System.currentTimeMillis();
    }

    private static String sanitize(String value) {
        String clean = value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return clean.isEmpty() ? "download" : clean;
    }

    private void notifyChanged() {
        if (listener != null) mainHandler.post(listener::onDownloadsChanged);
    }
}
