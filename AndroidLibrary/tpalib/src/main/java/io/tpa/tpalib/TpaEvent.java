package io.tpa.tpalib;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class TpaEvent {

    private final static String TAG = "TpaEvent";

    @Nullable
    private String category;
    @Nullable
    private String name;
    @NonNull
    private Map<String, String> tags;

    private TpaEvent(@NonNull Builder builder) {
        this.category = builder.category;
        this.name = builder.name;
        this.tags = builder.tags;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    @Nullable
    public String getName() {
        return name;
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

    boolean hasNullValues() {
        boolean tagsHasNullKey = tags.containsKey(null);
        boolean tagsHasNullValue = tags.containsValue(null);
        return getCategory() == null || getName() == null || tagsHasNullKey || tagsHasNullValue;
    }

    static class Builder {

        @Nullable
        private String category = null;
        @Nullable
        private String name = null;
        @NonNull
        private Map<String, String> tags = new HashMap<>();

        /**
         * Set the analytics event's category.
         *
         * @param category event category
         */
        Builder setCategory(@NonNull String category) {
            this.category = category;
            return this;
        }

        /**
         * Set the analytics event's name.
         *
         * @param name event name
         */
        Builder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        /**
         * Add a key/value pair tag to the event.
         *
         * @param key   key string
         * @param value value string
         */
        Builder addTag(@NonNull String key, @NonNull String value) {
            tags.put(key, value);
            return this;
        }

        /**
         * Add a map of key/value tag to the event.
         *
         * @param tags Map of key/value tags
         */
        Builder addTags(@NonNull Map<String, String> tags) {
            for (String key : tags.keySet()) {
                String value = tags.get(key);
                if (value != null) {
                    addTag(key, value);
                }
            }
            return this;
        }

        TpaEvent build() {
            if (this.name == null) {
                TpaDebugging.log.e(TAG, "Building TpaEvent with empty name");
            }

            if (this.category == null) {
                TpaDebugging.log.e(TAG, "Building TpaEvent with empty category");
            }

            return new TpaEvent(this);
        }
    }
}
