package io.tpa.tpalib.analytics;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused") //Public API
public final class TagsBuilder {

    @NonNull
    private Map<String, String> tags;

    public TagsBuilder() {
        tags = new HashMap<>();
    }

    /**
     * Add tag to the tags map. Null values are ignored.
     *
     * @param key   the key for the tag
     * @param value the value of the tag
     */
    @NonNull
    public TagsBuilder addTag(@NonNull String key, @NonNull String value) {
        tags.put(key, value);
        return this;
    }

    /**
     * Build the tags map.
     *
     * @return the tags map
     */
    @NonNull
    public Map<String, String> build() {
        return tags;
    }
}
