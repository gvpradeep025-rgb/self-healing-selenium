package com.pradeep.healing.report;

import com.pradeep.healing.core.HealingEvent;
import com.pradeep.healing.core.HealingOutcome;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Collects every healing event during the run and writes target/healing-report/index.html and healing-events.json. */
public final class HealingReport {

    public static final Path REPORT_DIR = Paths.get("target", "healing-report");

    private static final List<HealingEvent> EVENTS = Collections.synchronizedList(new ArrayList<>());
    private static final AtomicInteger PRIMARY_HITS = new AtomicInteger();

    private HealingReport() { }

    public static void record(HealingEvent event) { EVENTS.add(event); }
    public static void recordPrimaryHit() { PRIMARY_HITS.incrementAndGet(); }

    public static List<HealingEvent> events() {
        synchronized (EVENTS) {
            return new ArrayList<>(EVENTS);
        }
    }

    public static List<HealingEvent> eventsFor(String testName) {
        return events().stream().filter(e -> e.testName().equals(testName)).collect(Collectors.toList());
    }

    public static void write() throws IOException {
        Files.createDirectories(REPORT_DIR);
        List<HealingEvent> all = events();
        Files.writeString(REPORT_DIR.resolve("healing-events.json"), toJson(all), StandardCharsets.UTF_8);
        Files.writeString(REPORT_DIR.resolve("index.html"), toHtml(all), StandardCharsets.UTF_8);
        System.out.println("[HEALING] Report written to " + REPORT_DIR.resolve("index.html").toAbsolutePath());
    }

    private static String toHtml(List<HealingEvent> all) {
        long healed = all.stream().filter(e -> e.outcome() == HealingOutcome.HEALED).count();
        long refused = all.size() - healed;
        StringBuilder rows = new StringBuilder();
        for (HealingEvent e : all) {
            String css = e.outcome() == HealingOutcome.HEALED ? "healed" : "refused";
            rows.append("<tr class='").append(css).append("'>")
                .append(td(e.testName())).append(td(e.elementName())).append(td(e.primaryLocator()))
                .append(td(e.primaryProblem())).append("<td><b>").append(e.outcome()).append("</b></td>")
                .append(td(e.healedBy())).append(td(String.valueOf(e.score())))
                .append(td(String.valueOf(e.candidatesChecked()))).append(td(e.details()))
                .append("<td>").append(link(e.beforeScreenshot(), "before")).append(' ')
                .append(link(e.afterScreenshot(), "after")).append("</td></tr>\n");
        }
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Self-Healing Report</title><style>"
                + "body{font-family:Arial,sans-serif;margin:24px}table{border-collapse:collapse;width:100%;font-size:13px}"
                + "td,th{border:1px solid #ddd;padding:6px;vertical-align:top}th{background:#1f2937;color:#fff}"
                + "tr.healed{background:#ecfdf3}tr.refused{background:#fef3f2}.cards span{display:inline-block;margin-right:16px;"
                + "padding:10px 16px;border-radius:6px;background:#f2f4f7}</style></head><body>"
                + "<h1>Self-Healing Locator Report</h1><div class='cards'>"
                + "<span>Primary locators OK: <b>" + PRIMARY_HITS.get() + "</b></span>"
                + "<span>Healed: <b>" + healed + "</b></span>"
                + "<span>Refused: <b>" + refused + "</b></span></div><p></p>"
                + "<table><tr><th>Test</th><th>Element</th><th>Primary locator</th><th>Primary problem</th><th>Outcome</th>"
                + "<th>Healed by</th><th>Score</th><th>Candidates</th><th>Details</th><th>Screenshots</th></tr>"
                + rows + "</table></body></html>";
    }

    private static String toJson(List<HealingEvent> all) {
        return all.stream().map(e -> "  {"
                + q("test") + ":" + q(e.testName()) + ", "
                + q("element") + ":" + q(e.elementName()) + ", "
                + q("primaryLocator") + ":" + q(e.primaryLocator()) + ", "
                + q("primaryProblem") + ":" + q(e.primaryProblem()) + ", "
                + q("outcome") + ":" + q(e.outcome().name()) + ", "
                + q("healedBy") + ":" + q(e.healedBy()) + ", "
                + q("score") + ":" + e.score() + ", "
                + q("candidatesChecked") + ":" + e.candidatesChecked() + ", "
                + q("before") + ":" + q(e.beforeScreenshot()) + ", "
                + q("after") + ":" + q(e.afterScreenshot()) + ", "
                + q("time") + ":" + q(e.time().toString()) + "}")
                .collect(Collectors.joining(",\n", "[\n", "\n]\n"));
    }

    private static String td(String s) { return "<td>" + esc(s) + "</td>"; }

    private static String link(String path, String label) {
        return path == null ? "" : "<a href='" + esc(path) + "'>" + label + "</a>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }

    private static String q(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ") + "\"";
    }
}
