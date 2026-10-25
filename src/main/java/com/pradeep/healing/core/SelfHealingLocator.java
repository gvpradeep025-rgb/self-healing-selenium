package com.pradeep.healing.core;

import com.pradeep.healing.report.HealingReport;
import com.pradeep.healing.report.ScreenshotService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pradeep.healing.core.ElementFingerprint.Attr.*;

/**
 * Finds elements and recovers when the primary locator breaks.
 *
 * Rules:
 * 1. Even the primary match is validated against the fingerprint, so a reused id pointing at a different element is caught.
 * 2. Fallbacks are tried in order. A strategy wins only when exactly one candidate scores at or above the threshold.
 * 3. Two or more strong candidates means AMBIGUOUS, and a best score below the threshold means LOW_CONFIDENCE. Both stop the test.
 * 4. Every recovery and every refusal is reported, with before and after screenshots.
 */
public class SelfHealingLocator {

    public static final double DEFAULT_THRESHOLD = 0.80;

    private final WebDriver driver;
    private final double threshold;
    private final ScreenshotService screenshots;

    public SelfHealingLocator(WebDriver driver) {
        this(driver, DEFAULT_THRESHOLD);
    }

    public SelfHealingLocator(WebDriver driver, double threshold) {
        this.driver = driver;
        this.threshold = threshold;
        this.screenshots = new ScreenshotService(driver);
    }

    public WebElement find(ElementDescriptor d) {
        String primaryProblem = checkPrimary(d);
        if (primaryProblem == null) {
            HealingReport.recordPrimaryHit();
            return driver.findElement(d.primary());
        }

        String test = HealingContext.currentTest();
        String before = screenshots.capture(test, d.name(), "before");
        System.out.printf("[HEALING] %s: primary locator %s failed (%s). Trying %d fallbacks.%n",
                d.name(), d.primary(), primaryProblem, d.fallbacks().size());

        int checked = 0;
        double bestRejected = 0;
        StringBuilder trail = new StringBuilder();

        for (By strategy : d.fallbacks()) {
            List<WebElement> found = driver.findElements(strategy);
            List<WebElement> strong = new ArrayList<>();
            double bestHere = 0;
            for (WebElement candidate : found) {
                if (!candidate.isDisplayed()) {
                    continue;
                }
                checked++;
                double s = SimilarityScorer.score(d.fingerprint(), readAttributes(candidate));
                bestHere = Math.max(bestHere, s);
                if (s >= threshold) {
                    strong.add(candidate);
                } else {
                    bestRejected = Math.max(bestRejected, s);
                }
            }
            trail.append(strategy).append(" -> ").append(found.size()).append(" found, best score ")
                    .append(bestHere).append("; ");

            if (strong.size() > 1) {
                throw refuse(d, primaryProblem, HealingOutcome.AMBIGUOUS, checked, bestHere, before,
                        strong.size() + " elements match equally via " + strategy + ". Refusing to guess. Trail: " + trail);
            }
            if (strong.size() == 1) {
                WebElement healed = strong.get(0);
                double score = SimilarityScorer.score(d.fingerprint(), readAttributes(healed));
                String after = screenshots.captureHighlighted(healed, test, d.name());
                HealingReport.record(new HealingEvent(test, d.name(), d.primary().toString(), primaryProblem,
                        HealingOutcome.HEALED, strategy.toString(), score, checked, trail.toString(),
                        before, after, LocalDateTime.now()));
                System.out.printf("[HEALING] %s healed via %s (score %.3f)%n", d.name(), strategy, score);
                return healed;
            }
        }

        HealingOutcome outcome = checked == 0 ? HealingOutcome.ELEMENT_MISSING : HealingOutcome.LOW_CONFIDENCE;
        String why = checked == 0
                ? "No fallback found any visible element. Trail: " + trail
                : "Best candidate scored " + bestRejected + ", below the " + threshold + " threshold. Trail: " + trail;
        throw refuse(d, primaryProblem, outcome, checked, bestRejected, before, why);
    }

    /** Returns null when the primary locator finds one element that matches the fingerprint, otherwise the reason it can't be trusted. */
    private String checkPrimary(ElementDescriptor d) {
        List<WebElement> found = driver.findElements(d.primary());
        if (found.isEmpty()) {
            return "not found";
        }
        if (found.size() > 1) {
            return found.size() + " elements match the primary locator";
        }
        double s = SimilarityScorer.score(d.fingerprint(), readAttributes(found.get(0)));
        if (s < threshold) {
            return "found an element that does not match the fingerprint (score " + s + ")";
        }
        return null;
    }

    private HealingFailedException refuse(ElementDescriptor d, String primaryProblem, HealingOutcome outcome,
                                          int checked, double score, String before, String details) {
        HealingEvent event = new HealingEvent(HealingContext.currentTest(), d.name(), d.primary().toString(),
                primaryProblem, outcome, "-", score, checked, details, before, null, LocalDateTime.now());
        HealingReport.record(event);
        System.out.printf("[HEALING] %s REFUSED: %s%n", d.name(), outcome);
        return new HealingFailedException(event);
    }

    /** Reads the attributes a fingerprint can compare, in one browser round trip. */
    @SuppressWarnings("unchecked")
    Map<String, String> readAttributes(WebElement el) {
        Object raw = ((JavascriptExecutor) driver).executeScript(
                "var e = arguments[0];" +
                "return { tag: e.tagName.toLowerCase(), text: (e.innerText || '').trim()," +
                " 'aria-label': e.getAttribute('aria-label'), name: e.getAttribute('name')," +
                " type: e.getAttribute('type'), placeholder: e.getAttribute('placeholder') };", el);
        Map<String, String> attrs = new HashMap<>();
        if (raw instanceof Map) {
            ((Map<String, Object>) raw).forEach((k, v) -> attrs.put(k, v == null ? null : v.toString()));
        }
        return attrs;
    }
}
