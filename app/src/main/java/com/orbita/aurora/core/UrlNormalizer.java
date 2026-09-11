package com.orbita.aurora.core;

import android.net.Uri;

/** Converts user-entered address/search text into a loadable URI. */
public final class UrlNormalizer {
    private static final String DEFAULT_SEARCH = "https://www.google.com/search?q=";

    private UrlNormalizer() {}

    public static String normalize(String raw) {
        String input = raw == null ? "" : raw.trim();
        if (input.isEmpty()) {
            return "about:blank";
        }
        Uri parsed = Uri.parse(input);
        if (parsed.getScheme() != null) {
            return input;
        }
        if (input.contains(" ")) {
            return DEFAULT_SEARCH + Uri.encode(input);
        }
        return "https://" + input;
    }
}
