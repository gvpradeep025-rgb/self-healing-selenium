package com.pradeep.healing.core;

/** How a lookup ended. Every outcome except PRIMARY_OK is written to the healing report. */
public enum HealingOutcome {
    /** The primary locator found the element and it matched the fingerprint. */
    PRIMARY_OK,
    /** The primary locator broke; a fallback found exactly one element that matched the fingerprint. */
    HEALED,
    /** No locator found any element at all. */
    ELEMENT_MISSING,
    /** Elements were found, but none looked enough like the expected element. Refused. */
    LOW_CONFIDENCE,
    /** More than one element matched equally well. Picking one would be a guess. Refused. */
    AMBIGUOUS
}
