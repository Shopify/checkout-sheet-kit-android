# Changelog

## 3.1.1 October 2, 2024

- Ensure that cached WebView instances don't have existing parents before trying to add them to their container.

## 3.1.0 September 6, 2024

- Implement and expose `onShowFileChooser()`, to support clients with checkouts that need to show a native file chooser. Example in the MobileBuyIntegration demo app.

## 3.0.4 August 7, 2024

- Update Web Pixel schema data classes.

## 3.0.3 August 6, 2024

- Tag instrumentation with `preloading` state.

## 3.0.2 Jul 26, 2024

- Implements `onPermissionRequest()` to call a new `eventProcessor.onPermissionRequest(permissionRequest: PermissionRequest)` callback allowing clients to grant or deny permission requests, or request permissions (e.g. camera, record audio). This is sometimes required for checkouts that use features that require verifying identity.

## 3.0.1 May 31, 2024

- Call `onPause()` on the WebView as it's created if preloading, and `onResume()` when it's presented, so the Page Visibility API reports correct values.
- Ensure `WebView.destroy()` is not called on visible views, if preload is called while the view is visible.

## 3.0.0 May 20, 2024

- `ShopifyCheckoutSheet.present()` now returns an interface allowing clients to dismiss the sheet.
- Error handling has been improved\*. The kit also attempts to load checkout in a recovery WebView when certain errors are encountered. See [Error Handling](https://github.com/Shopify/checkout-sheet-kit-android#error-handling) for more information.

\*Please note the exception class hierarchy has been updated to be more comprehensive. Each exception class now returns an `isRecoverable: Boolean`, an `errorCode` and an `errorDescription`.

## 2.0.1 March 19, 2024

- Update compileSDK to 34
- Upgrade gradle and plugins (android gradle plugin, kotlin serialization, android kotlin)
- Upgrade dependencies (robolectric)

## 2.0.0 March 14, 2024

### New Features

1. **Breaking Changes** The loading spinner has been replaced by a progress bar on the webview. This will result in a faster perceived load time for checkout because the SDK will no longer wait for a full page load to show the DOM content.

If you were previously setting the loading spinner color, the field has been renamed from `spinnerColor` to `progressIndicator e.g:

```diff
Colors(
 - spinnerColor = Color.ResourceId(R.color.a_color),
 + progressIndicator = Color.ResourceId(R.color.a_color),
)
```

2. **Breaking Changes** The `onCheckoutCompleted` callback now returns a completed event object, containing details about the order:

```kotlin
override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
 println(checkoutCompletedEvent.orderDetails.id)
}
```

3. **Breaking Changes** The `CheckoutEventProcessor` passed to `present()` must now be a subclass of `DefaultCheckoutEventProcessor`.

4. The webview cache is no longer cleared on closing the dialog if checkout has not yet completed. This allows quickly reopening the dialog, and matches the behaviour in the swift library. As in swift, if preloading is enabled, it's important to call preload each tim the cart changes to avoid stale checkouts.

5. Upgrade `org.jetbrains.kotlinx:kotlinx-serialization-json` dependency from 1.5.1 to 1.6.3

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
