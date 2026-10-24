# ai-generated/

Drafts land here. Nothing in this directory is compiled by Maven, so a
generated test cannot reach the suite by accident.

Flow:

1. Generate a draft: `python3 scripts/generate_tests.py`
2. Read every generated test. Run it if you want a signal, but reading comes first.
3. Promote the tests you trust: move them into `src/test/java/com/bharath/qa/pricing/` by hand, fixing names and assertions as needed.
4. Run the suite: `mvn test`

Generated tests are drafts, not verdicts. The human review in step 2 is the point of this repo, not a chore around it.
