package com.pradeep.healing.core;

/** Remembers which test is running on this thread, so each healing event is tagged with its test. */
public final class HealingContext {

    private static final ThreadLocal<String> CURRENT_TEST = ThreadLocal.withInitial(() -> "unknown");

    private HealingContext() { }

    public static void setCurrentTest(String name) { CURRENT_TEST.set(name); }
    public static String currentTest() { return CURRENT_TEST.get(); }
}
