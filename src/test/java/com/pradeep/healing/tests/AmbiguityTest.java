package com.pradeep.healing.tests;

import com.pradeep.healing.core.HealingFailedException;
import com.pradeep.healing.core.HealingOutcome;
import com.pradeep.healing.pages.BaseUiTest;
import com.pradeep.healing.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/** v4 has two identical "Log in" buttons. Choosing one would be a guess, so the framework must stop. */
public class AmbiguityTest extends BaseUiTest {

    @Test(description = "Two equally good matches are reported as AMBIGUOUS instead of picking one")
    public void refusesToGuessBetweenTwinButtons() {
        openVersion("v4-ambiguous");
        LoginPage login = new LoginPage(healer);

        HealingFailedException ex = Assert.expectThrows(HealingFailedException.class, login::clickLogin);

        Assert.assertEquals(ex.outcome(), HealingOutcome.AMBIGUOUS);
        Assert.assertTrue(ex.getMessage().contains("2 elements match equally"), ex.getMessage());
    }
}
