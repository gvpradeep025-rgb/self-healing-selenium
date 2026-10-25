package com.pradeep.healing.pages;

import com.pradeep.healing.core.ElementDescriptor;
import com.pradeep.healing.core.ElementFingerprint;
import com.pradeep.healing.core.SelfHealingLocator;
import org.openqa.selenium.By;

import java.util.List;

/**
 * Page object for the DemoBank login screen. Each element has a primary locator, ordered fallbacks
 * (accessible name and stable attributes first, structural CSS last) and a fingerprint.
 */
public class LoginPage {

    public static final ElementDescriptor USERNAME = new ElementDescriptor("Username field",
            By.id("username"),
            List.of(By.name("username"),
                    By.cssSelector("input[aria-label='Username']"),
                    By.cssSelector("input[placeholder='Username']")),
            ElementFingerprint.builder().tag("input").name("username").type("text")
                    .ariaLabel("Username").placeholder("Username").build());

    public static final ElementDescriptor PASSWORD = new ElementDescriptor("Password field",
            By.id("password"),
            List.of(By.name("password"),
                    By.cssSelector("input[aria-label='Password']"),
                    By.cssSelector("input[type='password']")),
            ElementFingerprint.builder().tag("input").name("password").type("password")
                    .ariaLabel("Password").placeholder("Password").build());

    public static final ElementDescriptor LOGIN_BUTTON = new ElementDescriptor("Log in button",
            By.id("login-button"),
            List.of(By.cssSelector("button[aria-label='Log in']"),
                    By.xpath("//button[normalize-space()='Log in']"),
                    By.cssSelector("form button[type='submit']")),
            ElementFingerprint.builder().tag("button").type("submit")
                    .ariaLabel("Log in").text("Log in").build());

    public static final ElementDescriptor ERROR_MESSAGE = new ElementDescriptor("Login error message",
            By.id("error-message"),
            List.of(By.cssSelector("[role='alert']")),
            ElementFingerprint.builder().tag("p").text("Invalid username or password").build());

    private final SelfHealingLocator healer;

    public LoginPage(SelfHealingLocator healer) {
        this.healer = healer;
    }

    public void enterUsername(String username) {
        healer.find(USERNAME).sendKeys(username);
    }

    public void enterPassword(String password) {
        healer.find(PASSWORD).sendKeys(password);
    }

    public void clickLogin() {
        healer.find(LOGIN_BUTTON).click();
    }

    public void loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public String errorText() {
        return healer.find(ERROR_MESSAGE).getText();
    }
}
