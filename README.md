# Self-Healing Selenium Framework

![Self-Healing Tests](https://github.com/gvpradeep025-rgb/self-healing-selenium/actions/workflows/healing-tests.yml/badge.svg)

A Selenium + Java + TestNG framework that **recovers when a website's locators change**, and, just as importantly,
**refuses to continue when it cannot be sure it found the right element**.

UI tests usually break when a developer renames an id. This framework tries ordered fallback locators,
scores every candidate against a fingerprint of the expected element, and only accepts a match that is
both strong and unique. Every recovery and every refusal is written to an HTML report with screenshots.

## Why it matters

| Situation | Plain Selenium | This framework |
|---|---|---|
| A button's id is renamed | Test fails | Heals via accessible name, logs it, test passes |
| The old id is reused on a *different* button | Clicks the wrong button | Detects the fingerprint mismatch and refuses |
| Two elements look identical | Clicks the first one | Stops with `AMBIGUOUS` |
| The element is really gone | Test fails | Fails with a clear `ELEMENT_MISSING` / `LOW_CONFIDENCE` reason |

## How it works

```
find(descriptor)
 ├─ 1. Primary locator finds exactly one element AND it matches the fingerprint? → use it
 ├─ 2. Otherwise take a "before" screenshot and try fallbacks in order
 │     (name → aria-label → visible text → structural CSS)
 │     • score each visible candidate 0.0–1.0 against the fingerprint
 │     • exactly one candidate ≥ 0.80 → HEALED (red-outlined "after" screenshot, report entry)
 │     • two or more ≥ 0.80          → AMBIGUOUS, stop
 └─ 3. Nothing strong enough        → LOW_CONFIDENCE or ELEMENT_MISSING, stop
```

**Similarity scoring.** Each element has a fingerprint (tag, visible text, aria-label, name, type, placeholder).
Each attribute is compared with a normalised Levenshtein similarity and weighted by how much it says about identity
(text 0.25, aria-label 0.20, name 0.15, placeholder 0.15, tag 0.15, type 0.10).
Only the attributes in the fingerprint count.

**Failure classification.** `PRIMARY_OK`, `HEALED`, `ELEMENT_MISSING`, `LOW_CONFIDENCE`, `AMBIGUOUS`.

## The demo app (bundled, no internet needed)

| Version | What changed | Expected result |
|---|---|---|
| `demo-app/v1` | Original DemoBank login | All primary locators work, 0 healings |
| `demo-app/v2` | Redesign: every id renamed | All 4 elements healed, login passes |
| `demo-app/v3-trap` | "Delete account" button reuses the old `login-button` id | Mismatch detected, `LOW_CONFIDENCE`, account **not** deleted |
| `demo-app/v4-ambiguous` | Two identical "Log in" buttons | `AMBIGUOUS`, nothing clicked |

Because the app ships inside the repo, the tests are deterministic and CI never depends on an outside website.

## Tests (13)

- **Unit, `SimilarityScorerTest` (6):** identical match, case/spacing tolerance, small wording change, the Delete account trap is rejected, missing attributes, Levenshtein basics.
- **`BaselineV1Test` (2):** valid and invalid login with zero healing events.
- **`HealingV2Test` (3):** login survives the redesign; typed text really lands in the renamed fields; every recovery has before and after screenshots on disk.
- **`SafetyTrapTest` (1):** the reused id is caught and the dangerous button is never clicked.
- **`AmbiguityTest` (1):** twin buttons are reported, not guessed.

## Healing report

After a run, open `target/healing-report/index.html` (on GitHub: **Actions → a run → Artifacts → healing-report**).
It shows totals (primary OK / healed / refused) and one row per event: test, element, broken locator, why it broke,
outcome, the locator that healed it, score, candidates checked, the full fallback trail, and screenshot links.
`healing-events.json` holds the same data for other tools.

## Project structure

```
demo-app/                 v1, v2, v3-trap, v4-ambiguous
src/main/java/com/pradeep/healing/
  core/     ElementDescriptor, ElementFingerprint, SimilarityScorer, SelfHealingLocator,
            HealingOutcome, HealingEvent, HealingFailedException, HealingContext
  report/   HealingReport, HealingReportListener, ScreenshotService
  driver/   DriverFactory
src/test/java/com/pradeep/healing/
  pages/    BaseUiTest, LoginPage, DashboardPage   (Page Object Model)
  tests/    BaselineV1Test, HealingV2Test, SafetyTrapTest, AmbiguityTest
  unit/     SimilarityScorerTest
docs/design-decisions.md
```

## Run it

Needs Java 17, Maven and Chrome.

```bash
mvn clean test                    # opens Chrome
mvn clean test -Dheadless=true    # no window, as on CI
```

## Tech

Java 17 · Selenium 4 · TestNG · Page Object Model · GitHub Actions
