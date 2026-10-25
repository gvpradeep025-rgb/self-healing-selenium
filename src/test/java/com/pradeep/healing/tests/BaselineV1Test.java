package com.pradeep.healing.tests;

import com.pradeep.healing.pages.BaseUiTest;
import com.pradeep.healing.pages.DashboardPage;
import com.pradeep.healing.pages.LoginPage;
import com.pradeep.healing.report.HealingReport;
import org.testng.Assert;
import org.testng.annotations.Test;

/** v1 is the original app. Every primary locator should work, so nothing should need healing. */
public class BaselineV1Test extends BaseUiTest {

    @Test(description = "Valid login on v1 works with primary locators and zero healing events")
    public void validLoginNeedsNoHealing() {
        openVersion("v1");
        new LoginPage(healer).loginAs("demo_user", "secret123");

        Assert.assertEquals(new DashboardPage(healer).welcomeText(), "Welcome, demo_user");
        Assert.assertTrue(HealingReport.eventsFor(currentTest()).isEmpty(),
                "No healing should happen on the unchanged app");
    }

    @Test(description = "Invalid login on v1 shows the error message")
    public void invalidLoginShowsError() {
        openVersion("v1");
        new LoginPage(healer).loginAs("demo_user", "wrong-password");

        Assert.assertEquals(new LoginPage(healer).errorText(), "Invalid username or password");
        Assert.assertTrue(HealingReport.eventsFor(currentTest()).isEmpty());
    }
}
