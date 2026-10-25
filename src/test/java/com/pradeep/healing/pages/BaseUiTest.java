package com.pradeep.healing.pages;

import com.pradeep.healing.core.HealingContext;
import com.pradeep.healing.core.SelfHealingLocator;
import com.pradeep.healing.driver.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.nio.file.Paths;

/** Opens a fresh Chrome for every test and points the healer at it. */
public abstract class BaseUiTest {

    protected WebDriver driver;
    protected SelfHealingLocator healer;

    @BeforeMethod(alwaysRun = true)
    public void startBrowser(Method method) {
        HealingContext.setCurrentTest(getClass().getSimpleName() + "." + method.getName());
        driver = DriverFactory.create();
        healer = new SelfHealingLocator(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Opens one version of the bundled demo app, e.g. "v1" or "v2". */
    protected void openVersion(String version) {
        driver.get(Paths.get("demo-app", version, "index.html").toAbsolutePath().toUri().toString());
    }

    protected String currentTest() {
        return HealingContext.currentTest();
    }
}
