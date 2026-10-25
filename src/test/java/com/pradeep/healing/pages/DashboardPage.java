package com.pradeep.healing.pages;

import com.pradeep.healing.core.ElementDescriptor;
import com.pradeep.healing.core.ElementFingerprint;
import com.pradeep.healing.core.SelfHealingLocator;
import org.openqa.selenium.By;

import java.util.List;

/** Page object for the screen shown after a successful login. */
public class DashboardPage {

    public static final ElementDescriptor WELCOME = new ElementDescriptor("Welcome heading",
            By.id("welcome-message"),
            List.of(By.cssSelector("[data-role='welcome']"),
                    By.xpath("//h2[starts-with(normalize-space(),'Welcome')]")),
            ElementFingerprint.builder().tag("h2").text("Welcome, demo_user").build());

    private final SelfHealingLocator healer;

    public DashboardPage(SelfHealingLocator healer) {
        this.healer = healer;
    }

    public String welcomeText() {
        return healer.find(WELCOME).getText();
    }
}
