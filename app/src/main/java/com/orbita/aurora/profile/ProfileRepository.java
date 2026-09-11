package com.orbita.aurora.profile;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Loads and validates the predefined profile source from assets. */
public final class ProfileRepository {
    private static final String ASSET = "device_profiles.json";

    private ProfileRepository() {}

    public static String readRawJson(Context context) throws IOException {
        try (InputStream input = context.getAssets().open(ASSET)) {
            byte[] buffer = new byte[8192];
            StringBuilder result = new StringBuilder();
            int read;
            while ((read = input.read(buffer)) != -1) {
                result.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
            return result.toString();
        }
    }

    public static List<DeviceProfile> loadProfiles(Context context) throws IOException {
        try {
            JSONObject root = new JSONObject(readRawJson(context));
            JSONArray array = root.optJSONArray("profiles");
            if (array == null) return Collections.emptyList();

            List<DeviceProfile> profiles = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                if (object == null) continue;
                String id = required(object, "id");
                JSONObject viewport = object.optJSONObject("viewport");
                if (viewport == null) throw new IOException("Profile " + id + " has no viewport");

                List<DeviceProfile.ResolutionPreset> resolutions = new ArrayList<>();
                JSONArray resolutionArray = object.optJSONArray("supportedResolutionPresets");
                if (resolutionArray != null) {
                    for (int r = 0; r < resolutionArray.length(); r++) {
                        JSONObject preset = resolutionArray.optJSONObject(r);
                        if (preset == null) continue;
                        resolutions.add(new DeviceProfile.ResolutionPreset(
                                preset.optInt("width", viewport.optInt("width", 390)),
                                preset.optInt("height", viewport.optInt("height", 844))));
                    }
                }

                List<DeviceProfile.UserAgentVariant> variants = new ArrayList<>();
                JSONArray userAgents = object.optJSONArray("userAgentVariants");
                if (userAgents != null) {
                    for (int u = 0; u < userAgents.length(); u++) {
                        JSONObject variant = userAgents.optJSONObject(u);
                        if (variant == null) continue;
                        String value = variant.optString("value", "");
                        if (value.isEmpty()) continue;
                        variants.add(new DeviceProfile.UserAgentVariant(
                                variant.optString("id", "ua-" + u),
                                variant.optString("displayName", "User Agent " + (u + 1)),
                                value));
                    }
                }

                profiles.add(new DeviceProfile(
                        id,
                        required(object, "displayName"),
                        required(object, "deviceClass"),
                        required(object, "osFamily"),
                        required(object, "browserFamily"),
                        object.isNull("userAgent") ? null : object.optString("userAgent", null),
                        viewport.optInt("width", 390),
                        viewport.optInt("height", 844),
                        (float) viewportValue(object, "devicePixelRatio", 1.0),
                        object.optString("orientation", "portrait"),
                        resolutions,
                        variants));
            }
            return Collections.unmodifiableList(profiles);
        } catch (Exception e) {
            if (e instanceof IOException) throw (IOException) e;
            throw new IOException("Invalid device_profiles.json", e);
        }
    }

    public static DeviceProfile findById(Context context, String id) throws IOException {
        for (DeviceProfile profile : loadProfiles(context)) {
            if (profile.getId().equals(id)) return profile;
        }
        return null;
    }

    private static String required(JSONObject object, String key) throws IOException {
        String value = object.optString(key, "").trim();
        if (value.isEmpty()) throw new IOException("Missing profile field: " + key);
        return value;
    }

    private static double viewportValue(JSONObject object, String key, double fallback) {
        return object.has(key) ? object.optDouble(key, fallback) : fallback;
    }
}
