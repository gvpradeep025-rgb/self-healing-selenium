package com.pradeep.healing.report;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/** Saves the screenshots attached to the healing report. Returns paths relative to the report folder. */
public class ScreenshotService {

    private static final AtomicInteger SEQ = new AtomicInteger();
    private final WebDriver driver;

    public ScreenshotService(WebDriver driver) {
        this.driver = driver;
    }

    public String capture(String test, String element, String stage) {
        try {
            String file = String.format("screenshots/%03d-%s-%s-%s.png",
                    SEQ.incrementAndGet(), safe(test), safe(element), stage);
            Path target = HealingReport.REPORT_DIR.resolve(file);
            Files.createDirectories(target.getParent());
            Files.write(target, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            return file;
        } catch (Exception e) {
            System.out.println("[HEALING] Screenshot failed: " + e.getMessage());
            return null;
        }
    }

    /** Outlines the recovered element in red, captures the page, then removes the outline. */
    public String captureHighlighted(WebElement el, String test, String element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object old = js.executeScript("var s = arguments[0].style.outline; arguments[0].style.outline = '3px solid red'; return s;", el);
        try {
            return capture(test, element, "after");
        } finally {
            js.executeScript("arguments[0].style.outline = arguments[1];", el, old == null ? "" : old.toString());
        }
    }

    private static String safe(String s) {
        return s.replaceAll("[^A-Za-z0-9]+", "_");
    }
}
