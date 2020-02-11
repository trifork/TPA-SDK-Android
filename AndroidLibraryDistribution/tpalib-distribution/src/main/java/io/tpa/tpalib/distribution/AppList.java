package io.tpa.tpalib.distribution;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class AppList implements Serializable {

    private AppEntry latestVersion = null;
    private String jsonInput;
    private ArrayList<AppEntry> apps = new ArrayList<>();

    public AppList(String json, String baseUrl) throws JSONException {
        try {
            jsonInput = json;
            JSONArray list = new JSONArray(json);
            for (int index = 0; index < list.length(); index++) {
                JSONObject obj = list.getJSONObject(index);
                if (obj == null) {
                    continue;
                }

                AppEntry entry = new AppEntry(obj, baseUrl);
                apps.add(entry);

                if (latestVersion == null) {
                    latestVersion = entry;
                } else if (entry.getVersion() > latestVersion.getVersion()) {
                    latestVersion = entry;
                }
            }
        } catch (JSONException e) {
            if (UpdateConfig.debug()) {
                e.printStackTrace();
            }
        }
    }

    public AppEntry getLatestApp() {
        return latestVersion;
    }

    /**
     * Returns a json string
     */
    public String getJSONList() {
        return jsonInput;
    }
}