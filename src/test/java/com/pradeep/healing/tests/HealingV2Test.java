package com.pradeep.healing.tests;

import com.pradeep.healing.core.HealingEvent;
import com.pradeep.healing.core.HealingOutcome;
import com.pradeep.healing.core.SelfHealingLocator;
import com.pradeep.healing.pages.BaseUiTest;
import com.pradeep.healing.pages.DashboardPage;
import com.pradeep.healing.pages.LoginPage;
import com.pradeep.healing.report.HealingReport;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** v2 renamed every id. The same test code must still pass, and every recovery must be reported. */
public class HealingV2Test extends BaseUiTest {

    @Test(description = "Login still works on the redesigned v2 page, healing all four broken locators")
    public void loginSurvivesRedesign() {
        openVersion("v2");
        new LoginPage(healer).loginAs("demo_user", "secret123");
        Assert.assertEquals(new DashboardPage(healer).welcomeText(), "Welcome, demo_user");

        Map<String, HealingEvent> byElement = HealingReport.eventsFor(currentTest()).stream()
                .collect(Collectors.toMap(HealingEvent::elementName, e -> e));

        Assert.assertEquals(byElement.keySet(),
                java.util.Set.of("Username field", "Password field", "Log in button", "Welcome heading"));
        for (HealingEvent e : byElement.values()) {
            Assert.assertEquals(e.outcome(), HealingOutcome.HEALED, e.elementName());
            Assert.assertTrue(e.score() >= SelfHealingLocator.DEFAULT_THRESHOLD, e.elementName() + " score " + e.score());
            Assert.assertEquals(e.primaryProblem(), "not found");
        }
        Assert.assertEquals(byElement.get("Username field").healedBy(), "By.name: username");
        Assert.assertEquals(byElement.get("Log in button").healedBy(), "By.cssSelector: button[aria-label='Log in']");
    }

    @Test(description = "The healed element is really the right one: typed text lands in the renamed fields")
    public void healedElementsAreTheRealFields() {
        openVersion("v2");
        LoginPage login = new LoginPage(healer);
        login.enterUsername("demo_user");
        login.enterPassword("secret123");

        Assert.assertEquals(driver.findElement(By.id("txt-user-8812")).getAttribute("value"), "demo_user");
        Assert.assertEquals(driver.findElement(By.id("txt-pass-8813")).getAttribute("value"), "secret123");
    }

    @Test(description = "Every recovery has a before and an after screenshot on disk")
    public void recoveriesHaveScreenshots() throws Exception {
        openVersion("v2");
        new LoginPage(healer).enterUsername("demo_user");

        List<HealingEvent> events = HealingReport.eventsFor(currentTest());
        Assert.assertEquals(events.size(), 1);
        HealingEvent e = events.get(0);
        Assert.assertNotNull(e.beforeScreenshot());
        Assert.assertNotNull(e.afterScreenshot());
        Assert.assertTrue(Files.exists(HealingReport.REPORT_DIR.resolve(e.beforeScreenshot())));
        Assert.assertTrue(Files.exists(HealingReport.REPORT_DIR.resolve(e.afterScreenshot())));
    }
}
