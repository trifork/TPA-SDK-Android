package io.tpa.tpalib.distribution;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * JSON model for app entries from the TPA server.
 */
public class AppEntry implements Serializable {

    private static final long serialVersionUID = 7981337946436982483L;
    private final String title;
    private final int version;
    private final String shortVersion;
    private final long timestamp;
    private final long appSize;
    private final String installUrl;
    private final String date;
    private final String notes;

    private String json;

    public AppEntry(JSONObject json, String baseUrl) throws JSONException {
        title = json.has("title") ? json.getString("title") : "";
        version = json.getInt("version");
        shortVersion = json.has("shortversion") ? json.getString("shortversion") : "";
        timestamp = json.getLong("timestamp");
        appSize = json.getLong("appsize");

        if (json.has("install_url")) {
            installUrl = getCorrectInstallUrl(json.getString("install_url"), baseUrl);
        } else {
            installUrl = "";
        }

        date = json.has("date") ? json.getString("date") : "";
        notes = json.has("notes") ? json.getString("notes") : "";

        this.json = json.toString();
    }

    /**
     * Fix installUrl so it works correctly
     */
    private String getCorrectInstallUrl(String installUrlFromServer, String baseUrl) {
        String url;

        // Fix base url
        if (!installUrlFromServer.contains("://")) {
            url = baseUrl + installUrlFromServer;
        } else {
            url = installUrlFromServer;
        }

        // Return fixed url
        return url;
    }

    public String getTitle() {
        return title;
    }

    public int getVersion() {
        return version;
    }

    public String getShortVersion() {
        return shortVersion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAppSize() {
        return appSize;
    }

    public String getInstallUrl() {
        return installUrl;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    /**
     * Returns the data in json format
     */
    public String toJsonString() {
        return json;
    }
}
