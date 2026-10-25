package com.pradeep.healing.tests;

import com.pradeep.healing.core.HealingFailedException;
import com.pradeep.healing.core.HealingOutcome;
import com.pradeep.healing.pages.BaseUiTest;
import com.pradeep.healing.pages.LoginPage;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * v3 is a trap: "Delete account" now reuses the id "login-button" and is the only submit button in the form.
 * A naive framework would click it. This one must refuse and leave the account untouched.
 */
public class SafetyTrapTest extends BaseUiTest {

    @Test(description = "A reused id on a different element is caught, and the dangerous button is never clicked")
    public void refusesToClickLookAlikeButton() {
        openVersion("v3-trap");
        LoginPage login = new LoginPage(healer);
        login.enterUsername("demo_user");
        login.enterPassword("secret123");

        HealingFailedException ex = Assert.expectThrows(HealingFailedException.class, login::clickLogin);

        Assert.assertEquals(ex.outcome(), HealingOutcome.LOW_CONFIDENCE);
        Assert.assertTrue(ex.event().primaryProblem().startsWith("found an element that does not match"),
                ex.event().primaryProblem());
        Object deleted = ((JavascriptExecutor) driver).executeScript("return window.accountDeleted;");
        Assert.assertEquals(deleted, Boolean.FALSE, "The Delete account button must not have been clicked");
    }
}
