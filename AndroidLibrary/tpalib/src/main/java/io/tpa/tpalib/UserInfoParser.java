package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

class UserInfoParser {

    /**
     * Parses userInfo into a JSONObject
     *
     * @param userInfo map of additional info for non-fatal issue. Must contain only valid json types: String, Integer, Long, Double, Boolean, List and Map
     *
     * @return a valid JSONObject or null if the supplied map is null or contains invalid items
     */
    @Nullable
    static JSONObject parseUserInfo(@Nullable Map<String, Object> userInfo) {
        if (userInfo == null) {
            return null;
        }

        JSONObject root = new JSONObject();

        for (Map.Entry<String, Object> keyValue : userInfo.entrySet()) {
            try {
                Object validJsonObject = validJSONObject(keyValue.getValue());

                root.put(keyValue.getKey(), validJsonObject);
            } catch (JSONException ex) {
                return null; //Children must be valid JSON objects
            }
        }

        return root;
    }

    @NonNull
    private static Object validJSONObject(@Nullable Object base) throws JSONException {
        if (base == null) {
            return JSONObject.NULL;
        }

        if (base instanceof String || base instanceof Double || base instanceof Integer || base instanceof Long || base instanceof Boolean) {
            return base;
        }

        if (base instanceof Map) {
            Map map = (Map) base;
            JSONObject jsonObject = new JSONObject();

            for (Object key : map.keySet()) {
                if (!(key instanceof String)) {
                    throw new JSONException("Not a valid json object");
                }

                jsonObject.put((String) key, validJSONObject(map.get(key)));
            }

            return jsonObject;
        }

        if (base instanceof List) {
            List list = (List) base;
            JSONArray jsonArray = new JSONArray();

            for (Object item : list) {
                jsonArray.put(validJSONObject(item));
            }

            return jsonArray;
        }

        throw new JSONException("Not a valid json object");
    }
}
