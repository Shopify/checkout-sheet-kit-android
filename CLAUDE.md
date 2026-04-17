# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project overview

Shopify Checkout Kit for Android is a published AAR library (`com.shopify:checkout-sheet-kit`) that presents Shopify checkouts as a native, dialog-hosted WebView in consumer apps. It is consumed by third-party Android apps via Maven Central, so changes to the library's public surface have real consumer impact and real reversal cost once released.

Two modules matter:

- **`lib/`** — the library itself. Everything here ships to consumers.
- **`samples/MobileBuyIntegration/`** — a demo app that consumes `lib/` as a source dependency. Changes here never reach consumers; this module is for internal testing and developer onboarding.

The sample is a separate Gradle composite (`samples/MobileBuyIntegration/settings.gradle`) that includes `:lib` from `../../lib`. The sample's `gradle.properties` and Gradle wrapper are independent of the root's.

## Where to make changes

- Library source: `lib/src/main/java/com/shopify/checkoutsheetkit/`. Flat package at the top level with a few subpackages (`errorevents/`, `lifecycleevents/`, `pixelevents/`).
- Library tests: `lib/src/test/java/com/shopify/checkoutsheetkit/`. "No test, no merge" is a listed reject criterion in `.github/CONTRIBUTING.md`.
- Java interop is a first-class concern — the library is commonly consumed from Java code. `lib/src/test/java/com/shopify/checkoutsheetkit/InteropTest.java` exercises the public API from Java specifically; treat breakage there as a consumer-facing issue.

## Key components

- **`ShopifyCheckoutSheetKit.kt`** — the public singleton. Entry point for all consumer interactions (configure, preload, present).
- **`CheckoutDialog.kt`** — the dialog that hosts the WebView, including the progress indicator and error-recovery coordination.
- **`CheckoutWebView.kt`** — primary WebView. Holds the URL-keyed cache with a **5-minute preload TTL**; instruments page loads; routes bridge messages.
- **`BaseWebView.kt`** — abstract base class. Any new WebView variant must extend this so shared configuration (JS interface name, user agent suffix, client handling) is consistent.
- **`FallbackWebView.kt`** — minimal WebView swapped in during error recovery when the primary path fails.
- **`CheckoutBridge.kt`** — the JS ↔ native bridge. `SCHEMA_VERSION` is a cross-boundary contract with the web checkout team; bumping it requires coordination with them.
- **`Configuration.kt`** — runtime config container (color scheme, preload enable, log level, error recovery policy). Any config change clears the WebView cache.
- **`CheckoutEventProcessor.kt`** + **`DefaultCheckoutEventProcessor`** — consumer-implemented lifecycle interface (completion, failure, pixel events, permission prompts, link clicks). Changes here are consumer API changes.

## Testing patterns

- Tests use **Robolectric** (`@RunWith(RobolectricTestRunner::class)`) to exercise Android framework code without a device.
- Main-thread tasks are drained with `shadowOf(Looper.getMainLooper()).runToEndOfTasks()`. If a test involves posted work and seems flaky, check whether this is being called.
- Assertion library is **AssertJ**; mocking is **Mockito** + **Mockito-Kotlin**. Don't introduce new assertion/mocking libraries without discussion.
- Mockito is pinned at **4.x** intentionally — Mockito 5.x requires JVM target 11, and the library is on 1.8. Noted in `lib/build.gradle` with `// noinspection NewerVersionAvailable` comments.
- Tests live in the same package as the class under test (file name: `ClassNameTest.kt`).

## Conventions

- **`-Xexplicit-api=strict`** is on (`lib/build.gradle`). Every public class, method, field, and property must have an explicit visibility modifier. "Accidentally public" is not a thing here. This is a consumer-protection rule — if you see a public-by-default declaration, it was deliberate.
- **Max line length: 140** (detekt-enforced). Detekt config: `lib/detekt.config.yml`.
- **MIT license header required on every new source file.** Format: copy the top comment of any existing `.kt` or `.java` file in `lib/src/main` or `lib/src/test`. Enforced in CI via `scripts/check_license_headers.rb`.
- **Library JVM target: 1.8.** Intentional for consumer compatibility; don't raise without a major-version discussion.
- **Library Kotlin version is pinned.** The `lib/build.gradle` plugin version and any `apiVersion` / `languageVersion` settings exist to keep consumer compatibility stable. A Kotlin major-version migration is a planned major-version event, not a casual dep bump.

## Public API surface

The library's public API is captured in `lib/api/lib.api` (managed by the [binary-compatibility-validator](https://github.com/Kotlin/binary-compatibility-validator) Gradle plugin). Every PR is gated by `./gradlew :lib:apiCheck` in CI — the build fails if the compiled public API diverges from the committed baseline.

If a change intentionally modifies public API (adding, removing, or changing any public class, method, field, or property):

1. Run `./gradlew :lib:apiDump` (or `dev api dump`) to regenerate the baseline.
2. Review the diff in `lib/api/lib.api` — it's the single best indicator of consumer impact, and reviewers will focus on it.
3. Commit the updated `.api` file in the same PR as the code change.

If `apiCheck` fails and you did *not* intend to change public API, the diff tells you what inadvertently leaked out. Fix the leak rather than updating the baseline — you've accidentally shifted the consumer contract.

## Common commands

- Tests: `./gradlew :lib:test` (or `dev test`)
- API surface: `./gradlew :lib:apiCheck` / `./gradlew :lib:apiDump` (or `dev api check` / `dev api dump`)
- Lint: `./gradlew detekt lintRelease` (or `dev style`)
- Auto-fix lint: `./gradlew detekt --auto-correct` (or `dev fix`)
- Full local verification: `./gradlew :lib:clean :lib:test :lib:detekt :lib:lintRelease :lib:assembleRelease`
- Sample app build (from `samples/MobileBuyIntegration/`): `./gradlew assembleDebug`

## Consumer requirements

Raising any of these is a consumer-facing breaking change and needs visible release notes:

- `minSdk` (library's minimum supported Android API level at runtime)
- `compileSdk` floor for consumers (enforced via `aarMetadata.minCompileSdk` on the library, or implicitly raised by any transitive `androidx` dependency whose own metadata demands a newer `compileSdk`)
- Kotlin compiler / `apiVersion` / `languageVersion`
- JVM target

**Transitive `androidx` bumps can silently raise the `compileSdk` floor** — review dependabot PRs with this in mind, and run `./gradlew :lib:apiCheck` and check `aarMetadata` output when bumping any `androidx.*` dependency.

## Release process

Versions are bumped via:

1. The fallback value in `lib/build.gradle` for the `CHECKOUT_SHEET_KIT_VERSION` env var.
2. The install snippets in `README.md` (Gradle and Maven).

Publishing goes through GitHub Releases → `.github/workflows/publish.yml` → manual approval gate before Maven Central deploy. Full procedure: `.github/CONTRIBUTING.md` "Releasing a new version".

## Things not to touch without discussion

- **Library Kotlin version pin.** Consumer compatibility floor; any migration is a deliberate major-version decision.
- **`minSdk` / JVM target.** Same story.
- **`CheckoutBridge.SCHEMA_VERSION`.** Cross-team contract with the web checkout — changing it without coordination breaks the bridge.
- **Preload TTL (5 minutes).** Performance-sensitive; has been tuned. Don't tweak without a reason.
- **`-Xexplicit-api=strict`.** Removing this would let implicit public declarations ship; keeping it is a consumer-protection invariant.
