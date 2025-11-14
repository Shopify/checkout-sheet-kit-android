# Contributing

The following is a set of guidelines for contributing to the project. Please take a moment to read
through them before submitting your first PR.

## Code of Conduct

This project and everyone participating in it are governed by
the [Code of Conduct](./CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable
behavior to [opensource@shopify.com](mailto:opensource@shopify.com).

## Welcomed Contributions

- Reporting issues with existing features
- Bug fixes
- Performance improvements
- Documentation
- Usability Improvements

## Things we won't merge

- Additional dependencies that limit sdk use (e.g. android dependencies)
- Any changes that break existing tests
- Any changes without sufficient tests

## Proposing Features

When in doubt about whether we will be interested in including a new feature in this project, please
open an issue to propose the feature so we can confirm the feature should be in scope for the
project before it is implemented.

**NOTE**: Issues that have not been active for 30 days will be marked as stale, and subsequently closed after a further 7 days of inactivity.

## How To Contribute

1. Fork the repo and branch off of main
2. Create a feature branch in your fork
3. Make changes and add any relevant tests
4. Verify the changes locally (e.g. via the sample app)
5. Commit your changes and push
6. Ensure all checks (e.g. tests) are passing in GitHub
7. Create a new pull request with a detailed description of what is changing and why

## Releasing a new version

### Preparing for a release

Before creating a release, ensure the following version is updated:

1. Bump the [versionName](https://github.com/Shopify/checkout-sheet-kit-android/blob/main/lib/build.gradle#L18) in `lib/build.gradle`

**Important**: The version string in `lib/build.gradle` must match the git tag exactly, including any pre-release suffixes (e.g., `-beta.1`, `-rc.1`). The CI validation workflow will enforce this.

### Version format

- **Production releases**: `X.Y.Z` (e.g., `3.5.0`)
- **Pre-releases**: `X.Y.Z-{alpha|beta|rc}.N` (e.g., `3.5.0-beta.1`, `3.5.0-rc.2`)

Pre-release suffixes ensure:
- Maven/Gradle users must explicitly specify the version to install pre-releases
- Gradle doesn't treat them as the default "latest" version
- Maven Central correctly identifies them as pre-release artifacts

**Note on version naming**: Unlike development builds which use `-SNAPSHOT` (e.g., `3.5.0-SNAPSHOT`), pre-releases use `-alpha.N`, `-beta.N`, or `-rc.N` suffixes. SNAPSHOT versions are for in-development builds that change frequently. Pre-releases are stable builds being tested before final release. We only publish pre-releases (alpha/beta/rc), never SNAPSHOT versions.

### Creating a release

Navigate to <https://github.com/Shopify/checkout-sheet-kit-android/releases> and click "Draft a new release", then complete the following steps:

#### For production releases (from `main` branch):

1. Ensure you're on the `main` branch
2. Create a tag for the new version (e.g., `3.5.0`)
3. Use the same tag as the release title
4. Document the full list of changes since the previous release, tagging merged pull requests where applicable
5. ✅ Check "Set as the latest release" to ensure Maven/Gradle identifies this as the latest release
6. Click "Publish release"

#### For pre-releases (from non-`main` branch):

1. Ensure you're on a feature/release branch (NOT `main`)
2. Create a tag with a pre-release suffix (e.g., `3.5.0-beta.1`, `3.5.0-rc.2`)
3. Use the same tag as the release title
4. Document the changes being tested in this pre-release
5. ✅ Check "Set as a pre-release" (NOT "Set as the latest release")
6. Click "Publish release"

### What happens after publishing

When you publish a release (production or pre-release), the [publish workflow](https://github.com/Shopify/checkout-sheet-kit-android/actions/workflows/publish.yml) will automatically:

1. **Validate versions**: Ensures the `lib/build.gradle` version matches the git tag and validates the version format
2. **Deploy to Maven Central**: Publishes the version to Maven Central (OSSRH)

**Note**: A manual approval by a maintainer is required before the release is published to Maven Central.

### Using pre-releases

For users to install a pre-release version, they must specify the exact version in their dependency configuration:

**Gradle** - Specify the exact version in `build.gradle`:

```groovy
implementation "com.shopify:checkout-sheet-kit:3.5.0-beta.1"
```

**Maven** - Specify the exact version in `pom.xml`:

```xml
<dependency>
   <groupId>com.shopify</groupId>
   <artifactId>checkout-sheet-kit</artifactId>
   <version>3.5.0-beta.1</version>
</dependency>
```

**Important**: Pre-release versions will not be automatically resolved by Gradle's version resolution. Users must explicitly specify the pre-release version to use it.
