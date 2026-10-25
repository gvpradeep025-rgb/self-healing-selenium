package com.pradeep.healing.core;

import org.openqa.selenium.By;

import java.util.List;

/**
 * One element as a page object knows it: a readable name, the primary locator,
 * ordered fallback locators (most trustworthy first) and the fingerprint used to validate any match.
 */
public final class ElementDescriptor {

    private final String name;
    private final By primary;
    private final List<By> fallbacks;
    private final ElementFingerprint fingerprint;

    public ElementDescriptor(String name, By primary, List<By> fallbacks, ElementFingerprint fingerprint) {
        this.name = name;
        this.primary = primary;
        this.fallbacks = List.copyOf(fallbacks);
        this.fingerprint = fingerprint;
    }

    public String name() { return name; }
    public By primary() { return primary; }
    public List<By> fallbacks() { return fallbacks; }
    public ElementFingerprint fingerprint() { return fingerprint; }

    @Override
    public String toString() {
        return name + " " + primary;
    }
}
