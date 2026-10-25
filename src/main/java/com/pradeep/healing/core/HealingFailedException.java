package com.pradeep.healing.core;

/** Thrown when the framework refuses to continue rather than act on an element it cannot trust. */
public class HealingFailedException extends RuntimeException {

    private final HealingEvent event;

    public HealingFailedException(HealingEvent event) {
        super(event.outcome() + " for '" + event.elementName() + "': " + event.details());
        this.event = event;
    }

    public HealingEvent event() {
        return event;
    }

    public HealingOutcome outcome() {
        return event.outcome();
    }
}
