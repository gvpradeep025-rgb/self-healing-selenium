package com.pradeep.healing.driver;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/** Creates Chrome. Run with -Dheadless=true on CI; Selenium Manager downloads the driver automatically. */
public final class DriverFactory {

    private DriverFactory() { }

    public static WebDriver create() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900");
        String binary = System.getProperty("chrome.binary");   // optional, e.g. -Dchrome.binary=/usr/bin/chromium
        if (binary != null && !binary.isBlank()) {
            options.setBinary(binary);
        }
        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        return new ChromeDriver(options);
    }
}
