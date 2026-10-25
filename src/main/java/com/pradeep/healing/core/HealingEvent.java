package com.pradeep.healing.core;

import java.time.LocalDateTime;

/** One reported lookup: what broke, what was tried, what was decided and why. */
public record HealingEvent(
        String testName,
        String elementName,
        String primaryLocator,
        String primaryProblem,
        HealingOutcome outcome,
        String healedBy,
        double score,
        int candidatesChecked,
        String details,
        String beforeScreenshot,
        String afterScreenshot,
        LocalDateTime time) {
}
