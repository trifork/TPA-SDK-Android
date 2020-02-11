package io.tpa.tpalib;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class TpaTimingEvent {

    @NonNull
    private String category;
    @NonNull
    private String name;
    @NonNull
    private Long startTimestamp;
    @NonNull
    private Map<String, String> tags = new HashMap<>();

    TpaTimingEvent(@NonNull String category, @NonNull String name) {
        this.category = category;
        this.name = name;
        startTimestamp = System.nanoTime();
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Long getDuration() {
        return (System.nanoTime() - startTimestamp) / 1000000; // nano -> ms
    }

    @SuppressWarnings("unused")
    @NonNull
    public Map<String, String> getTags() {
        //This makes sure that if a user calls getTags, and modifies the returned map, it doesn't change our internal map
        return new HashMap<>(tags);
    }

    @NonNull
    Map<String, String> getTagsInternal() {
        return tags;
    }

    private void addTag(@NonNull String key, @NonNull String value) {
        tags.put(key, value);
    }

    void addTags(@NonNull Map<String, String> tags) {
        for (String key : tags.keySet()) {
            String value = tags.get(key);
            if (value != null) {
                addTag(key, value);
            }
        }
    }

    boolean hasNullValues() {
        boolean tagsHasNullKey = tags.containsKey(null);
        boolean tagsHasNullValue = tags.containsValue(null);
        return getCategory() == null || getName() == null || tagsHasNullKey || tagsHasNullValue;
    }
}
