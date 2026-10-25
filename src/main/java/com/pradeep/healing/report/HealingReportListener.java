package com.pradeep.healing.report;

import com.pradeep.healing.core.HealingContext;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Tags healing events with the running test and writes the report when the suite ends. */
public class HealingReportListener implements ITestListener, ISuiteListener {

    @Override
    public void onTestStart(ITestResult result) {
        HealingContext.setCurrentTest(testName(result));
    }

    @Override
    public void onFinish(ISuite suite) {
        try {
            HealingReport.write();
        } catch (Exception e) {
            System.out.println("[HEALING] Could not write report: " + e.getMessage());
        }
    }

    public static String testName(ITestResult result) {
        return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
    }
}
