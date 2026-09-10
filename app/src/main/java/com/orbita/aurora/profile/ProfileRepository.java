package com.orbita.aurora.profile;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Reads the predefined profile source from assets; parsing/validation is expanded in the profile phase. */
public final class ProfileRepository {
    private static final String ASSET = "device_profiles.json";

    private ProfileRepository() {}

    public static String readRawJson(Context context) throws IOException {
        try (InputStream input = context.getAssets().open(ASSET)) {
            byte[] bytes = new byte[input.available()];
            int offset = 0;
            int read;
            while (offset < bytes.length && (read = input.read(bytes, offset, bytes.length - offset)) > 0) {
                offset += read;
            }
            return new String(bytes, 0, offset, StandardCharsets.UTF_8);
        }
    }
}
