package com.pradeep.healing.unit;

import com.pradeep.healing.core.ElementFingerprint;
import com.pradeep.healing.core.SelfHealingLocator;
import com.pradeep.healing.core.SimilarityScorer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/** Fast unit tests for the scoring rules. No browser needed. */
public class SimilarityScorerTest {

    private final ElementFingerprint loginButton = ElementFingerprint.builder()
            .tag("button").type("submit").ariaLabel("Log in").text("Log in").build();

    private static Map<String, String> attrs(String tag, String type, String aria, String text) {
        Map<String, String> m = new HashMap<>();
        m.put("tag", tag);
        m.put("type", type);
        m.put("aria-label", aria);
        m.put("text", text);
        return m;
    }

    @Test
    public void identicalElementScoresOne() {
        Assert.assertEquals(SimilarityScorer.score(loginButton, attrs("button", "submit", "Log in", "Log in")), 1.0);
    }

    @Test
    public void caseAndSpacesDoNotMatter() {
        Assert.assertEquals(SimilarityScorer.score(loginButton, attrs("BUTTON", "submit", " log  in ", "LOG IN")), 1.0);
    }

    @Test
    public void smallWordingChangeStillPasses() {
        double s = SimilarityScorer.score(loginButton, attrs("button", "submit", "Log in", "Log in now"));
        Assert.assertTrue(s >= SelfHealingLocator.DEFAULT_THRESHOLD, "score " + s);
    }

    @Test
    public void deleteAccountButtonIsRejected() {
        double s = SimilarityScorer.score(loginButton, attrs("button", "submit", "Delete account", "Delete account"));
        Assert.assertTrue(s < SelfHealingLocator.DEFAULT_THRESHOLD, "score " + s);
    }

    @Test
    public void missingAttributesCountAsMismatch() {
        double s = SimilarityScorer.score(loginButton, attrs("button", null, null, null));
        Assert.assertTrue(s < 0.5, "score " + s);
    }

    @Test
    public void stringSimilarityBasics() {
        Assert.assertEquals(SimilarityScorer.stringSimilarity("Username", "username"), 1.0);
        Assert.assertEquals(SimilarityScorer.stringSimilarity("abc", null), 0.0);
        Assert.assertEquals(SimilarityScorer.stringSimilarity("kitten", "sitting"), 1.0 - 3.0 / 7, 0.0001);
    }
}
