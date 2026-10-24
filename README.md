# ollama-junit-demo

A small demo with one idea: **a local Ollama model writes JUnit 5 unit tests
for a small Java app, and a human reviews them before they become the suite.**

No cloud model, no API key, no test run leaves your machine. The model runs
on `localhost:11434` and the tests run with Maven.

## The app

`src/main/java/com/bharath/qa/pricing/` holds a tiny, pure pricing module —
no files, no network, no database, which is what makes it easy to unit test:

- `LineItem` — one cart line: sku, unit price, quantity.
- `CustomerTier` — BRONZE, SILVER, GOLD.
- `PricingService` — subtotal, tier discount, and total with tax.

Discount rules: GOLD gets 15% when the subtotal is 200 or more, otherwise
10%; SILVER gets 5%; BRONZE gets none. Total = (subtotal − discount) ×
(1 + taxRate). Invalid input (negative price, non-positive quantity, null
arguments) throws.

## Prerequisites

- JDK 17 or newer and Maven (`mvn -v` should work).
- Ollama running locally, with a code model pulled, for example:
  `ollama pull qwen2.5-coder:7b` (the default model; override with
  `OLLAMA_MODEL` or `--model`).

## Run the hand-written tests

These are the suite's baseline. They pass without Ollama and show the
naming and assertion style generated tests should follow.

```sh
mvn test
```

## Generate tests with Ollama

```sh
python3 scripts/generate_tests.py
```

This sends `PricingService.java` plus `scripts/prompt_template.txt` to your
local Ollama model and writes the draft to
`ai-generated/PricingServiceGeneratedTest.java`. To see the prompt without
calling the model:

```sh
python3 scripts/generate_tests.py --dry-run
```

Other options: `--model` (for example `--model qwen3:8b`), `--host`,
`--source`, `--out`. The script uses only the Python standard library.

## Review, then promote

Generated tests are drafts, not verdicts. This is the deliberate part of
the demo:

1. Read every generated test in `ai-generated/`. Fix vague names and
   weak assertions.
2. Move the tests you trust into `src/test/java/com/bharath/qa/pricing/`
   by hand.
3. Run `mvn test` again.

Nothing in `ai-generated/` is compiled by Maven, so a draft cannot reach
the suite until you move it.

## Reports

Every `mvn test` run produces three reports. No extra command is needed
to generate them; the commands below are only for viewing.

**Execution — Surefire (plain text, always available).**
One `.txt` file per test class, plus XML for tooling:

```sh
ls target/surefire-reports/
```

**Execution — Allure (HTML, the readable one).**
The run writes raw results to `target/allure-results/`. Render them with
the Allure command line (install once: `brew install allure` on macOS,
or download it from the Allure releases page):

```sh
allure serve target/allure-results
```

This opens a report with every test case, its steps, and pass/fail status.

**Coverage — JaCoCo (HTML).** Open this file in a browser:

```sh
open target/site/jacoco/index.html
```

It shows, per class and per method, which lines and branches the tests
executed, plus machine-readable `jacoco.xml` and `jacoco.csv` beside it.
There is deliberately no coverage gate in the build: the number is
information, not a pass/fail verdict.

**A useful before/after exercise:** run `mvn test` and note the coverage,
then generate tests, promote the ones you trust, run `mvn test` again,
and compare the JaCoCo report. That comparison is the honest measure of
what the model's tests added.

## How it is laid out

- `src/main/java/.../pricing/` — the app under test.
- `src/test/java/.../pricing/PricingServiceTest.java` — hand-written baseline tests.
- `scripts/generate_tests.py` — the Ollama client (standard library only).
- `scripts/prompt_template.txt` — the prompt sent to the model; edit it to
  change what gets generated.
- `ai-generated/` — staging area for drafts, never compiled.

## Continuous integration

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs `mvn test`
on every push and pull request and uploads the three reports as
artifacts (Surefire output, JaCoCo HTML, Allure raw results). CI runs
only the hand-written suite: the generator needs a local Ollama model,
so it stays a local step.

## Notes

- The model sometimes writes plausible-looking tests with wrong numbers
  (especially around the GOLD 200 threshold). That is the demo working as
  intended: the review step exists because generated tests must be checked,
  not because the model is perfect.
- Test names state the behavior and the expected outcome in plain words.
  If a generated name is vague, rename it during review.
