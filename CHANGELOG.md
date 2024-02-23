# Changelog

## 1.0.0 January 31, 2024

- Checkout Sheet Kit is now generally available

## 0.5.0 - January 26, 2024

- **Breaking Changes** A new `onWebPixelEvent(PixelEvent)` function has been added to the `CheckoutEventProcessor` interface. This allows listening for [Web Pixel](https://shopify.dev/docs/apps/marketing/pixels) events that take place in checkout, so they can be emitted to your preferred analytics system. See `README.md` for more details.

_Note_: If your processor extends `DefaultCheckoutEventProcessor`, a no-op implementation has been added, so no changes are required unless you'd like to respond to pixel events. If your processor does not extend `DefaultCheckoutEventProcessor`, you will need to implement this function.

- Fix: Prevent loading checkout twice during preloads.
- Fix: Match `CheckoutDialog`'s header padding with checkout's padding.
- Fix: Ensure the WebView cache is cleared on error responses for preloaded requests.

## 0.4.0 - January 10, 2024

- **Breaking Changes:** The library has been rebranded from Shopify Checkout Kit to Shopify Checkout Sheet Kit. Apologies for any inconvenience caused. Here are the steps to upgrade:

1. Update your gradle/maven import:

```diff
- implementation 'com.shopify:checkout-kit:0.3.3'
+ implementation 'com.shopify:checkout-sheet-kit:0.4.0'
```

2. Update the imports throughout your codebase:
```diff
- com.shopify.checkoutkit.*
+ com.shopify.checkoutsheetkit.*
```

3. Update the `present|preload|configure()` calls throughout your codebase:
```diff
- ShopifyCheckoutKit.present|preload|configure()
+ ShopifyCheckoutSheetKit.present|preload|configure()
```

Also included:

- Inform checkout when the sheet has been presented to help distinguish between preloads and presents. Groundwork for analytics.
- Emit instrumentation payloads to improve observability.

## 0.3.3 - November 27, 2023

- Exposes the `errorDescription` internal variable on the `CheckoutException` class.

## 0.3.2 - November 27, 2023

- Adds annotations (`@ColorInt` and `@ColorRes`) for more robust color value enforcement.
- Exposes the `lightColors` and `darkColors` properties of the `Automatic` ColorScheme class to allow overrides.

## 0.3.1 - November 24, 2023

- Fix for Java interoperability related to default arguments on `DefaultCheckoutEventProcessor`.

## 0.3.0 - November 24, 2023

- **Breaking Changes:** Allows setting sRGB colors as well as color resource IDs in ColorSchemes.

  ```kotlin
    // Previously
    webViewBackground = Color(R.color.checkoutDarkBg)

    // Now
    webViewBackground = Color.ResourceId(R.color.checkoutDarkBg)
    // Or
    webViewBackground = Color.SRGB(-0xff0001)
  ```

- Implemented `onRenderProcessGone` to prevent app crashes when the render process is killed to reclaim memory
- JavaDoc improvements
- Added function to retrieve the currently applied `ShopifyCheckoutKit` configuration
