package com.pradeep.healing.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the element is supposed to look like, independent of any locator.
 * A recovered element is only accepted when its real attributes match this fingerprint closely enough.
 */
public final class ElementFingerprint {

    private final Map<String, String> expected;

    private ElementFingerprint(Map<String, String> expected) {
        this.expected = Collections.unmodifiableMap(expected);
    }

    public Map<String, String> expected() {
        return expected;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return expected.toString();
    }

    public static final class Builder {
        private final Map<String, String> values = new LinkedHashMap<>();

        public Builder tag(String tag) { return put(Attr.TAG, tag); }
        public Builder text(String text) { return put(Attr.TEXT, text); }
        public Builder ariaLabel(String label) { return put(Attr.ARIA_LABEL, label); }
        public Builder name(String name) { return put(Attr.NAME, name); }
        public Builder type(String type) { return put(Attr.TYPE, type); }
        public Builder placeholder(String placeholder) { return put(Attr.PLACEHOLDER, placeholder); }

        private Builder put(String key, String value) {
            values.put(key, value);
            return this;
        }

        public ElementFingerprint build() {
            if (values.isEmpty()) {
                throw new IllegalStateException("A fingerprint needs at least one attribute");
            }
            return new ElementFingerprint(new LinkedHashMap<>(values));
        }
    }

    /** Attribute keys used by fingerprints and the scorer. */
    public static final class Attr {
        public static final String TAG = "tag";
        public static final String TEXT = "text";
        public static final String ARIA_LABEL = "aria-label";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String PLACEHOLDER = "placeholder";

        private Attr() { }
    }
}
