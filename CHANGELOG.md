# Changelog

## 0.3.0 - November 24, 2023

- **Breaking:** Allow setting sRGB colors as well as color resource IDs in ColorSchemes.

  ```kotlin
    // Previously
    webViewBackground = Color(R.color.checkoutDarkBg)

    // Now
    webViewBackground = Color.ResourceId(R.color.checkoutDarkBg)
    // Or
    webViewBackground = Color.SRGB(-0xff0001)
  ```

- Implemented onRenderProcessGone to prevent app crashes when the render process is killed to reclaim memory
- JavaDoc improvements
- Add function to retrieve the currently applied ShopifyCheckoutSheet configuration

Note: breaking changes may be made during Dev Preview with minor version increments, but will receive a major version increments
after release for General Availability.

## 0.3.1 - November 24, 2023

- Fix for Java interoperability related to default arguments on `DefaultCheckoutEventProcessor`.

## 0.3.2 - November 27, 2023

- Adds annotations (`@ColorInt` and `@ColorRes`) for more robust color value enforcement.
- Exposes the `lightColors` and `darkColors` properties of the `Automatic` ColorScheme class to allow overrides.

## 0.3.3 - November 27, 2023

- Exposes the `errorDescription` internal variable on the `CheckoutException` class.

## 0.4.0 January 04, 2024

- Inform checkout when the sheet has been presented to help distinguish between preloads and presents. Groundwork for analytics.
- Emit instrumentation payloads to improve observability.
- **Breaking:** Renames library from checkout kit to checkout sheet kit. Apologies for the inconvenience, steps to upgrade:

1. Update gradle/maven import from `implementation 'com.shopify:checkout-kit:0.3.3'` to `implementation 'com.shopify:checkout-sheet-kit:0.4.0'`
2. Change imports from `com.shopify.checkoutkit.*` to `com.shopify.checkoutsheetkit.*`
3. Call `ShopifyCheckoutSheet.present|preload|configure()` instead of `ShopifyCheckoutKit.present|preload|configure()`
