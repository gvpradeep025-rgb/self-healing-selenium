# Design decisions

## 1. Never trust a guessed element
Self-healing is dangerous when it silently clicks "something close enough". Every match, including the primary one,
is validated against a fingerprint. A recovered element is accepted only when it scores at least 0.80 **and** it is the only
strong candidate for that strategy. Every recovery is reported, so a human can update the locator later.

## 2. Validate the primary locator too
A common real bug is an id being reused for a different element after a redesign. If only "not found" triggered healing,
the test would click the wrong element. `checkPrimary` scores the primary match and treats a low score as broken
(see `demo-app/v3-trap`).

## 3. Ordered fallbacks, most stable first
`name` and accessible name (`aria-label`) rarely change during a visual redesign, visible text changes more often,
and structural CSS like `form button[type='submit']` is the least specific. Fallbacks are tried in that order and the
first strategy with exactly one strong candidate wins.

## 4. Weighted similarity instead of exact matching
Exact matching would reject harmless changes such as "Log in" → "Log in now". A normalised Levenshtein similarity per
attribute, weighted by how much each attribute says about identity, tolerates small changes but still rejects
"Delete account" (score 0.495 in the trap test).

## 5. Refuse with a reason
Failures are classified so the report tells you what to do:
- `ELEMENT_MISSING`: the feature may really be gone. Check with the developer.
- `LOW_CONFIDENCE`: something is there, but it is not the expected element.
- `AMBIGUOUS`: the page needs a more specific locator.

## 6. A bundled demo app
Testing against a public site makes healing impossible to prove, because you can't control when its ids change.
Four local versions make each scenario repeatable, and CI never fails because an outside site is down.

## Limitations and next steps
- Fingerprints are written by hand in page objects. Next step: record the fingerprint automatically on every successful
  primary match and compare against the last known good version.
- The threshold is global. It could be set per element for critical actions such as payments.
- Shadow DOM and iframes are not searched.
