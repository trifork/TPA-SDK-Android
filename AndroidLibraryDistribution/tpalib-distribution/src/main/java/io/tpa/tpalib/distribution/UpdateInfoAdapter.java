package io.tpa.tpalib.distribution;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class UpdateInfoAdapter extends BaseAdapter {

    private Activity activity;
    private JSONObject newest;
    private ArrayList<JSONObject> sortedVersions;

    public UpdateInfoAdapter(Activity activity, String infoJSON) {
        super();

        this.activity = activity;

        loadVersions(infoJSON);
        sortVersions();
    }

    private void loadVersions(String infoJSON) {
        this.newest = new JSONObject();

        try {
            JSONArray versions = new JSONArray(infoJSON);
            this.sortedVersions = new ArrayList<>();

            int versionCode = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_META_DATA).versionCode;
            for (int index = 0; index < versions.length(); index++) {
                JSONObject entry = versions.getJSONObject(index);
                if (entry.getInt("version") > versionCode) {
                    newest = entry;
                    versionCode = entry.getInt("version");
                }
                sortedVersions.add(entry);
            }
            sortedVersions.remove(newest);
        } catch (JSONException ignored) {
        } catch (NameNotFoundException ignored) {
        }
    }

    private void sortVersions() {
        Collections.sort(sortedVersions, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject object1, JSONObject object2) {
                try {
                    if (object1.getInt("version") > object2.getInt("version")) {
                        return 0;
                    }
                } catch (JSONException ignored) {
                }

                return 0;
            }
        });
    }

    @Override
    public int getCount() {
        return 2 * sortedVersions.size();
    }

    @Override
    public Object getItem(int position) {
        int currentVersionCode = -1;
        try {
            currentVersionCode = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_META_DATA).versionCode;
        } catch (NameNotFoundException ignored) {
        }

        JSONObject version = sortedVersions.get(position / 2);
        int versionCode = 0;
        String versionName = "";
        try {
            versionCode = version.getInt("version");
            versionName = version.getString("shortversion");
        } catch (JSONException ignored) {
        }

        String item = null;
        switch (position % 2) {
            case 0:
                item = "Version " + versionName + " (" + versionCode + "): " + (versionCode == currentVersionCode ? "[INSTALLED]" : "");
                break;
            case 1:
                item = failSafeGetStringFromJSON(version, "notes", "");
                break;
            case 2:
        }
        return item;
    }

    public String getAppNameString() {
        return failSafeGetStringFromJSON(newest, "title", "no name");
    }

    public String getVersionString() {
        return failSafeGetStringFromJSON(newest, "shortversion", "") + " (" + failSafeGetStringFromJSON(newest, "version", "") + ")";
    }

    public String getNotesString() {
        return failSafeGetStringFromJSON(newest, "notes", "no name");
    }

    public String getFileInfoString() {
        int appSize = failSafeGetIntFromJSON(newest, "appsize", 0);
        long timestamp = failSafeGetIntFromJSON(newest, "timestamp", 0);
        Date date = new Date(timestamp * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(date) + " - " + String.format("%.2f", appSize / 1024F / 1024F) + " MB";
    }

    private static String failSafeGetStringFromJSON(JSONObject json, String name, String defaultValue) {
        try {
            return json.getString(name);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    private static int failSafeGetIntFromJSON(JSONObject json, String name, int defaultValue) {
        try {
            return json.getInt(name);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    @Override
    public long getItemId(int position) {
        return Integer.valueOf(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (position % 2) {
            case 0:
                return getSimpleView(position, convertView, parent);
            case 1:
                return getWebView(position, convertView, parent);
            default:
                return null;
        }
    }

    private View getSimpleView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (!(row instanceof TextView)) {
            //LayoutInflater inflater = activity.getLayoutInflater();
            //row = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            row = getSimpleTextView();
        }

        String item = (String) getItem(position);

        TextView textView = (TextView) row.findViewById(android.R.id.text1);
        //    TextView textView = (TextView)row.findViewById(6666);
        float scale = activity.getResources().getDisplayMetrics().density;
        boolean leftPadding = (parent.getTag().equals("right"));
        int offset = (int) (20 * scale);
        textView.setPadding(offset * (leftPadding ? 2 : 1), offset * (!leftPadding ? 1 : 0), offset, 0);
        textView.setText(item);
        textView.setTextColor(Color.DKGRAY);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        return row;
    }

    private View getSimpleTextView() {
        RelativeLayout layout = new RelativeLayout(activity);
        layout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
        TextView textView = new TextView(activity);
        textView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textView.setId(android.R.id.text1);
        layout.addView(textView);
        return layout;
    }

    private View getWebView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if ((row == null)) { // || (row.findViewById(1337) == null)) {
            //RelativeLayout layout = new RelativeLayout(activity);
            //layout.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
            //row = layout;

            //WebView webView = new WebView(activity);
            //webView.setId(1337);
            row = getSimpleTextView();
            //layout.addView(webView);
        }

        TextView textView = (TextView) row.findViewById(android.R.id.text1);
        float scale = activity.getResources().getDisplayMetrics().density;
        boolean leftPadding = (parent.getTag().equals("right"));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) (20 * scale) * (leftPadding ? 3 : 2), (int) (0 * scale), (int) (20 * scale), 0);
        textView.setLayoutParams(params);
        textView.setText((String) getItem(position));

        //    String item = (String)getItem(position);
        //    if (item.trim().length() == 0) {
        //      webView.loadData("<em>No information.</em>", "text/html", "utf-8");
        //    }
        //    else {
        //      webView.loadData(item, "text/html", "utf-8");
        //    }

        return row;
    }
}
