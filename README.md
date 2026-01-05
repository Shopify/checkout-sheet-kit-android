# Shopify Checkout Kit - Android

[![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)](/LICENSE)
![Tests](https://github.com/Shopify/checkout-sheet-kit-android/actions/workflows/test.yml/badge.svg?branch=main)
[![GitHub Release](https://img.shields.io/github/release/shopify/checkout-sheet-kit-android.svg?style=flat)]()

<img width="3200" height="800" alt="gradients" src="https://github.com/user-attachments/assets/1f1d7351-1715-4165-874e-c1f2195bcb20" />

**Shopify's Checkout Kit for Android** is a library that enables Android apps to provide the world's highest converting, customizable, one-page checkout within an app. The presented experience is a fully-featured checkout that preserves all of the store customizations: Checkout UI extensions, Functions, and more. It also provides idiomatic defaults such as support for light and dark mode, and convenient developer APIs to embed, customize and follow the lifecycle of the checkout experience. Check out our developer blog to [learn how Checkout Kit is built](https://www.shopify.com/partners/blog/mobile-checkout-sdks-for-ios-and-android).

**Note**: We're in the process of renaming "Checkout Sheet Kit" to "Checkout Kit." The dev docs and README already use the new name, while the package itself will be updated in an upcoming version.

- [Shopify Checkout Kit - Android](#shopify-checkout-kit---android)
  - [Requirements](#requirements)
  - [Getting Started](#getting-started)
    - [Gradle](#gradle)
    - [Maven](#maven)
  - [Basic Usage](#basic-usage)
    - [Application Authentication](#application-authentication)
      - [Fetching Authentication Tokens](#fetching-authentication-tokens)
  - [Configuration](#configuration)
    - [Color Scheme](#color-scheme)
    - [Log Level](#log-level)
    - [Checkout Dialog Title](#checkout-dialog-title)
  - [Preloading](#preloading)
    - [Important considerations](#important-considerations)
    - [Flash Sales](#flash-sales)
    - [When to preload](#when-to-preload)
    - [Cache invalidation](#cache-invalidation)
    - [Lifecycle management for preloaded checkout](#lifecycle-management-for-preloaded-checkout)
      - [Additional considerations for preloaded checkout](#additional-considerations-for-preloaded-checkout)
  - [Monitoring the lifecycle of a checkout session](#monitoring-the-lifecycle-of-a-checkout-session)
    - [Error handling](#error-handling)
      - [`CheckoutException`](#checkoutexception)
      - [Exception Hierarchy](#exception-hierarchy)
  - [Integrating identity \& customer accounts](#integrating-identity--customer-accounts)
    - [Cart: buyer bag, identity, and preferences](#cart-buyer-bag-identity-and-preferences)
    - [Multipass](#multipass)
    - [Shop Pay](#shop-pay)
    - [Customer Account API](#customer-account-api)
  - [Contributing](#contributing)
  - [License](#license)

## Requirements

- JDK 17+
- Android SDK 23+

## Getting Started

The SDK is an [open source Android library](https://central.sonatype.com/artifact/com.shopify/checkout-sheet-kit). As a quick start, see
[sample projects](samples/README.md) or use one of the following ways to integrate the SDK into
your project:

### Gradle

```groovy
implementation "com.shopify:checkout-sheet-kit:3.5.3"
```

### Maven

```xml

<dependency>
   <groupId>com.shopify</groupId>
   <artifactId>checkout-sheet-kit</artifactId>
   <version>4.0.1-SNAPSHOT</version>
</dependency>
```

## Basic Usage

Once the SDK has been added as a dependency, you can import the library:

```kotlin
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
```

To present a checkout to the buyer, your application must first obtain a checkout URL.
The most common way is to use the [Storefront GraphQL API](https://shopify.dev/docs/api/storefront)
to assemble a cart (via `cartCreate` and related update mutations) and load the
[`checkoutUrl`](https://shopify.dev/docs/api/storefront/latest/objects/Cart#field-cart-checkouturl). Alternatively, a [cart permalink](https://help.shopify.com/en/manual/products/details/cart-permalink) can be provided.
You can use any GraphQL client to obtain a checkout URL and we recommend
Shopify's [Mobile Buy SDK for Android](https://github.com/Shopify/mobile-buy-sdk-android) to
simplify the development workflow:

```kotlin

val client = GraphClient.build(
    context = applicationContext,
    shopDomain = "yourshop.myshopify.com",
    accessToken = "<storefront access token>"
)

val cartQuery = Storefront.query { query ->
    query.cart(ID(id)) {
        it.checkoutUrl()
    }
}

client.queryGraph(cartQuery).enqueue {
    if (it is GraphCallResult.Success) {
        val checkoutUrl = it.response.data?.cart?.checkoutUrl
    }
}
```

The `checkoutUrl` object is a standard web checkout URL that can be opened in any browser.
To present a native checkout dialog in your Android application, provide
the `checkoutUrl` alongside optional runtime configuration settings to the `present(checkoutUrl)`
function provided by the SDK:

```kotlin
fun presentCheckout() {
    val checkoutUrl = cart.checkoutUrl
    ShopifyCheckoutSheetKit.present(checkoutUrl, context, checkoutEventProcessor)
}
```

> [!TIP]
> To help optimize and deliver the best experience the SDK also provides a
> [preloading API](#preloading) that can be used to initialize the checkout session ahead of time.

### Application Authentication

To allow customizing checkout with app specific branding, and/or to receive PII in checkout lifecycle events,
you will need to pass an app authentication token to checkout via `CheckoutOptions` when calling `preload` or `present`.

```kotlin
import com.shopify.checkoutsheetkit.Authentication
import com.shopify.checkoutsheetkit.CheckoutOptions

val checkoutOptions = CheckoutOptions(
    authentication = Authentication.Token(jwtToken)
)

ShopifyCheckoutSheetKit.preload(checkoutUrl, context, checkoutOptions)

ShopifyCheckoutSheetKit.present(
    checkoutUrl = checkoutUrl,
    context = context,
    checkoutEventProcessor = checkoutEventProcessor,
    options = checkoutOptions,
)
```

#### Fetching Authentication Tokens

Authentication tokens should be fetched from your authentication endpoint using OAuth client credentials flow:

```kotlin
suspend fun fetchAuthToken(): String {
    // POST to your auth endpoint with client credentials
    val response = httpClient.post(authEndpoint) {
        setBody(mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "grant_type" to "client_credentials"
        ))
    }

    // Parse and return the JWT access token
    return response.body<AuthTokenResponse>().accessToken
}
```

Fetch the token asynchronously before presenting checkout, with graceful fallback on error:

```kotlin
suspend fun presentCheckout(url: String, activity: ComponentActivity) {
    val token = try {
        fetchAuthToken()
    } catch (e: Exception) {
        null // Fallback to unauthenticated checkout
    }

    val options = token?.let { CheckoutOptions(authentication = Authentication.Token(it)) }
    ShopifyCheckoutSheetKit.present(url, activity, processor, options)
}
```

Note: Tokens are embedded in the checkout URL and should be treated as secrets. Avoid logging the URL or
persisting it beyond the lifetime of the session.

## Configuration

The SDK provides a way to customize the presented checkout experience via
the `ShopifyCheckoutSheetKit.configure` function.

### Color Scheme

By default, the SDK will match the user's device color appearance. This behavior can be customized
via the `colorScheme` property:

```kotlin
ShopifyCheckoutSheetKit.configure {
    // [Default] Automatically toggle idiomatic light and dark themes based on device preference.
    it.colorScheme = ColorScheme.Automatic()

    // Force idiomatic light color scheme
    it.colorScheme = ColorScheme.Light()

    // Force idiomatic dark color scheme
    it.colorScheme = ColorScheme.Dark()

    // Force web theme, as rendered by a mobile browser
    it.colorScheme = ColorScheme.Web()

    // Force web theme, passing colors for the modal header and background
    it.colorScheme = ColorScheme.Web(
        Colors(
            webViewBackground = Color.ResourceId(R.color.web_view_background),
            headerFont = Color.ResourceId(R.color.header_font),
            headerBackground = Color.ResourceId(R.color.header_background),
            progressIndicator = Color.ResourceId(R.color.progress_indicator),
        )
    )
}
```

> [!Tip]
> Colors can also be specified in sRGB format (e.g. `Color.SRGB(-0xff0001)`) and can also be overridden for Light/Dark/Automatic themes, (see example below)

```kotlin
val automatic = ColorScheme.Automatic(
    lightColors = Colors(
        headerBackground = Color.ResourceId(R.color.headerLight),
        headerFont = Color.ResourceId(R.color.headerFontLight),
        webViewBackground = Color.ResourceId(R.color.webViewBgLight),
        progressIndicator = Color.ResourceId(R.color.indicatorLight),
    ),
    darkColors = Colors(
        headerBackground = Color.ResourceId(R.color.headerDark),
        headerFont = Color.ResourceId(R.color.headerFontDark),
        webViewBackground = Color.ResourceId(R.color.webViewBgDark),
        progressIndicator = Color.ResourceId(R.color.indicatorDark),
    )
)
```

**Close Icon Customization**

The close icon in the checkout dialog header can be customized using the `customize` method for an ergonomic API:

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.colorScheme = ColorScheme.Light().customize {
        // Option 1: Just tint the default close icon
        closeIconTint = Color.ResourceId(R.color.my_custom_tint_color)

        // Option 2: Use a completely custom drawable
        closeIcon = DrawableResource(R.drawable.my_custom_close_icon)
    }
}
```

For automatic theme switching, you can provide different customizations for light and dark modes:

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.colorScheme = ColorScheme.Automatic().customize(
        light = {
            closeIconTint = Color.ResourceId(R.color.light_tint)
        },
        dark = {
            closeIconTint = Color.ResourceId(R.color.dark_tint)
        }
    )
}
```

> [!Note]
> If both `closeIcon` and `closeIconTint` are provided, the custom drawable (`closeIcon`) takes precedence and the tint is ignored.

The colors that can be modified are:

- headerBackground - Used to customize the background of the app bar on the dialog,
- headerFont - Used to customize the font color of the header text within in the app bar,
- webViewBackground - Used to customize the background color of the WebView,
- progressIndicator - Used to customize the color of the progress indicator shown when checkout is loading.
- closeIcon - Used to provide a completely custom close icon drawable
- closeIconTint - Used to tint the default close icon with a custom color

The current configuration can be obtained by calling `ShopifyCheckoutSheetKit.getConfiguration()`.

### Log Level

Enable additional debug logs via the `logLevel` configuration option.

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.logLevel = LogLevel.DEBUG
}
```

### Checkout Dialog Title

To customize the title of the Dialog that the checkout WebView is displayed within, or to provide different values for the various locales your app supports, override the `checkout_web_view_title` String resource in your application, e.g:

```xml
<string name="checkout_web_view_title">Buy Now!</string>
```

## Preloading

Initializing a checkout session requires communicating with Shopify servers, thus depending
on the network quality and bandwidth available to the buyer can result in undesirable waiting
time for the buyer. To help optimize and deliver the best experience, the SDK provides a
`preloading` "hint" that allows developers to signal that the checkout session should be
initialized in the background, ahead of time.

Preloading is an advanced feature that can be disabled via a runtime flag:

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.preloading = Preloading(enabled = false) // defaults to true
}
```

Once enabled, preloading a checkout is as simple as calling
`preload(checkoutUrl, activity)` with a valid `checkoutUrl`. To provide app authentication pass in `CheckoutOptions`, as with `present`.

```kotlin
ShopifyCheckoutSheetKit.preload(checkoutUrl, this, checkoutOptions)
```

Setting enabled to `false` will cause all calls to the `preload` function to be ignored. This allows the application to selectively toggle preloading behavior as a remote feature flag or dynamically in response to client conditions - e.g. when data saver functionality is enabled by the user.

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.preloading = Preloading(enabled = false)
}
ShopifyCheckoutSheetKit.preload(checkoutUrl, this) // no-op
```

### Important considerations

1. Initiating preload results in background network requests and additional
   CPU/memory utilization for the client, and should be used when there is a
   high likelihood that the buyer will soon request to checkout—e.g. when the
   buyer navigates to the cart overview or a similar app-specific experience.
2. A preloaded checkout session reflects the cart contents at the time when
   `preload` is called. If the cart is updated after `preload` is called, the
   application needs to call `preload` again to reflect the updated checkout
   session.
3. Calling `preload(checkoutUrl)` is a hint, **not a guarantee**: the library
   may debounce or ignore calls to this API depending on various conditions; the
   preload may not complete before `present(checkoutUrl)` is called, in which
   case the buyer may still see a spinner while the checkout session is
   finalized.

### Flash Sales

It is important to note that during Flash Sales or periods of high amounts of traffic, buyers may be entered into a queue system.

**Calls to preload which result in a buyer being enqueued will be rejected.** This means that a buyer will never enter the queue without their knowledge.

### When to preload

Calling `preload()` each time an item is added to a buyer's cart can put significant strain on Shopify systems, which in return can result in rejected requests. Rejected requests will not result in a visual error shown to users, but will degrade the experience since they will need to load checkout from scratch.

Instead, a better approach is to call `preload()` when you have a strong enough signal that the buyer intends to check out. In some cases this might mean a buyer has navigated to a "cart" screen.

### Cache invalidation

Should you wish to manually clear the preload cache, there is a `ShopifyCheckoutSheetKit.invalidate()` helper function to do so. This function will be a no-op if no checkout is preloaded.

You may wish to do this if the buyer changes shortly before entering checkout, e.g. by changing cart quantity on a cart view.

### Lifecycle management for preloaded checkout

Preloading renders a checkout in a background webview, which is brought to foreground when `ShopifyCheckoutSheetKit.present()` is called. The content of preloaded checkout reflects the state of the cart when `preload()` was initially called. If the cart is mutated after `preload()` is called, the application is responsible for invalidating the preloaded checkout to ensure that up-to-date checkout content is displayed to the buyer:

1. To update preloaded contents: call `preload()` once again
2. To disable preloaded content: toggle the preload configuration setting

The library will automatically invalidate/abort preload under the following conditions:

- Request results in network error or non 2XX server response code
- The checkout has successfully completed, as indicated by the server response
- When `ShopifyCheckoutSheet.configure` is called (e.g. with theming changes).

A preloaded checkout _is not_ automatically invalidated when checkout is closed. For example, if a buyer loads the checkout then exists, the preloaded checkout is retained and should be updated when cart contents change.

#### Additional considerations for preloaded checkout

1. Preloading is a hint, not a guarantee. The library may debounce or ignore
   calls depending on various conditions; the preload may not complete before
   `present(checkoutUrl)` is called, in which case the buyer may still see a progress/loading indicator while the checkout session is finalized.
2. Preloading results in background network requests and additional CPU/memory utilization
   for the client, and should be used responsibly. For example, conditionally based on the state of the client and when there is a high likelihood that the buyer will soon
   request to checkout.

## Monitoring the lifecycle of a checkout session

Extend the `DefaultCheckoutEventProcessor` abstract class to register callbacks for key lifecycle events during the checkout session:

```kotlin
val processor = object : DefaultCheckoutEventProcessor(activity) {
    override fun onStart(event: CheckoutStartEvent) {
        // Called when checkout starts.
        // Provides the initial cart state at the beginning of the checkout flow.
    }

    override fun onComplete(event: CheckoutCompleteEvent) {
        // Called when the checkout was completed successfully by the buyer.
        // Use this to update UI, reset cart state, etc.
    }

    override fun onCancel() {
        // Called when the checkout was canceled by the buyer.
        // Note: This will also be received after closing a completed checkout
    }

    override fun onFail(error: CheckoutException) {
        // Called when the checkout encountered an error and has been aborted.
        Log.e("Checkout", "Checkout failed: ${error.errorDescription}", error)

        when (error) {
            is CheckoutExpiredException -> {
                // Cart expired or completed - create a new cart
                cartViewModel.clearCart()
                showError("Your cart has expired. Please try again.")
            }
            is ConfigurationException -> {
                // Configuration issue - may need developer intervention
                showError("Checkout is currently unavailable.")
            }
            is ClientException -> {
                // Checkout unavailable - show error to buyer
                showError("Unable to complete checkout. Please try again later.")
            }
            else -> {
                // SDK internal errors - may be recoverable
                if (!error.isRecoverable) {
                    showError("An error occurred during checkout.")
                }
            }
        }
    }

    override fun onCheckoutLinkClicked(uri: Uri) {
        // Called when the buyer clicks a link within the checkout experience:
        // - email address (`mailto:`)
        // - telephone number (`tel:`)
        // - web (http:)
        // - deep link (e.g. myapp://checkout)
        // and is being directed outside the application.

        // Note: to support deep links on Android 11+ using the `DefaultCheckoutEventProcessor`,
        // the client app should add a queries element in its manifest declaring which apps it should interact with.
        // See the MobileBuyIntegration sample's manifest for an example.
        // Queries reference - https://developer.android.com/guide/topics/manifest/queries-element

        // If no app can be queried to deal with the link, the processor will log a warning:
        // `Unrecognized scheme for link clicked in checkout` along with the uri.
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams,
    ): Boolean {
        // Called to tell the client to show a file chooser. This is called to handle HTML forms with 'file' input type,
        // in response to the user pressing the "Select File" button.
        // To cancel the request, call filePathCallback.onReceiveValue(null) and return true.
    }

    override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        // Called to tell the client to show a geolocation permissions prompt as a geolocation permissions
        // request has been made.
        // Invoked for example if a customer uses `Use my location` for pickup points
    }

    override fun onGeolocationPermissionsHidePrompt() {
        // Called to tell the client to hide the geolocation permissions prompt, e.g. as the request has been cancelled
    }

    override fun onPermissionRequest(permissionRequest: PermissionRequest) {
        // Called when a permission has been requested, e.g. to access the camera
        // implement to grant/deny/request permissions.
    }

    override fun onAddressChangeStart(event: CheckoutAddressChangeStartEvent) {
        // Called when the buyer requests to change their shipping or billing address.
        // Note: Only emitted when the feature is enabled for an authenticated app.
        // Respond with updated address information using event.respondWith()
        // See the MobileBuyIntegration sample for a complete example.
    }

    override fun onSubmitStart(event: CheckoutSubmitStartEvent) {
        // Called when checkout submission starts.
        // Note: Only emitted when the feature is enabled for an authenticated app.
        // Use this to provide payment authorization if required.
        // Respond using event.respondWith() to proceed with submission.
    }

    override fun onPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStartEvent) {
        // Called when the buyer requests to change their payment method.
        // Note: Only emitted when the feature is enabled for an authenticated app.
        // Respond with updated payment information using event.respondWith()
    }
}
```

> [!Note]
> The `DefaultCheckoutEventProcessor` provides default implementations for current and future callback functions (such as `onLinkClicked()`), which can be overridden by clients wanting to change default behavior.

### Error handling

In the event of a checkout error occurring, the Checkout Kit _may_ attempt to retry to recover from the error. Recovery will happen in the background by discarding the failed WebView and creating a new "recovery" instance. Recovery will be attempted in the following scenarios:

- The WebView receives a 5XX status code
- An internal SDK error is emitted

There are some caveats to note when this scenario occurs:

1. The checkout experience may look different to buyers. Though the sheet kit will attempt to load any checkout customizations for the storefront, there is no guarantee they will show in recovery mode.
2. The `onComplete(checkoutCompleteEvent: CheckoutCompleteEvent)` will be emitted with partial data. Invocations will only receive the order ID via `checkoutCompleteEvent.orderConfirmation.order.id`.

Should you wish to opt-out of this fallback experience entirely, you can do so by overriding `shouldRecoverFromError`. Errors given to the `onFail(error: CheckoutException)` lifecycle method will contain an `isRecoverable` property by default indicating whether the request should be retried or not.

`preRecoveryActions()` can also be overridden to execute code before a fallback takes place, for example to add logging, or clear up any potentially problematic state such as in cookies. By default this function is a no-op.

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.errorRecovery = object: ErrorRecovery {
        override fun shouldRecoverFromError(checkoutException: CheckoutException): Boolean {
            // To disable recovery (default = checkoutException.isRecoverable)
            return false
        }

        override fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
            // Perform actions prior to recovery, e.g. logging, clearing up cookies:
            if (exception is HttpException) {
                CookiePurger.purge(checkoutUrl)
            }
        }
    }
}
```

#### `CheckoutException`

Checkout errors can originate from the checkout web experience itself (`checkout.error` events) or from the SDK. All errors include `errorDescription`, `errorCode`, and `isRecoverable` properties.

**Configuration Errors** - `ConfigurationException` (non-recoverable)

These indicate issues with storefront or checkout configuration. Checkout will be dismissed automatically.

| Error Code                       | Description                                  | Recommendation                                                                              |
| -------------------------------- | -------------------------------------------- | ------------------------------------------------------------------------------------------- |
| `STOREFRONT_PASSWORD_REQUIRED`   | Access to checkout is password protected.    | Checkout unavailable. Consider opening checkout URL in external browser.                    |
| `CUSTOMER_ACCOUNT_REQUIRED`      | Customer must be logged in.                  | Ensure buyer is authenticated before presenting checkout.                                   |
| `INVALID_PAYLOAD`                | The checkout payload is invalid.             | Verify the cart and checkout configuration.                                                 |
| `INVALID_SIGNATURE`              | The checkout signature is invalid.           | Check authentication token generation.                                                      |
| `NOT_AUTHORIZED`                 | The request is not authorized.               | Verify app authentication credentials.                                                      |
| `PAYLOAD_EXPIRED`                | The authentication token has expired.        | Fetch a new authentication token and create a new checkout session.                        |

**Expired Checkout Errors** - `CheckoutExpiredException` (non-recoverable)

These indicate the checkout session is no longer valid. Checkout will be dismissed automatically.

| Error Code       | Description                                               | Recommendation                             |
| ---------------- | --------------------------------------------------------- | ------------------------------------------ |
| `CART_COMPLETED` | The cart associated with the checkout has been completed. | Create a new cart and open a new checkout. |
| `INVALID_CART`   | The cart is invalid or empty.                             | Create a new cart and open a new checkout. |

**Client Errors** - `ClientException` (non-recoverable)

These indicate checkout is temporarily or permanently unavailable. Checkout will be dismissed automatically.

| Error Code               | Description                                         | Recommendation                                                      |
| ------------------------ | --------------------------------------------------- | ------------------------------------------------------------------- |
| `KILLSWITCH_ENABLED`     | Checkout preloading is temporarily disabled.        | Retry checkout without preloading, or show error to buyer.          |
| `UNRECOVERABLE_FAILURE`  | An unrecoverable error occurred in checkout.        | Show error to buyer. Consider opening checkout URL in external browser. |
| `POLICY_VIOLATION`       | A policy violation was detected.                    | Show error to buyer.                                                |
| `VAULTED_PAYMENT_ERROR`  | An error occurred with a vaulted payment method.    | Show error to buyer. Consider opening checkout URL in external browser. |

**SDK Internal Errors** - `CheckoutSheetKitException` / `HttpException` / `CheckoutUnavailableException`

These indicate issues within the SDK or network layer:

| Exception Class                | Error Code                  | Description                                                 | Recommendation                       |
| ------------------------------ | --------------------------- | ----------------------------------------------------------- | ------------------------------------ |
| `CheckoutSheetKitException`    | `ERROR_RECEIVING_MESSAGE`   | Failed to receive a message from checkout.                  | Show checkout in a fallback browser. |
| `CheckoutSheetKitException`    | `ERROR_SENDING_MESSAGE`     | Failed to send a message to checkout.                       | Show checkout in a fallback browser. |
| `CheckoutSheetKitException`    | `RENDER_PROCESS_GONE`       | The WebView render process is gone.                         | Show checkout in a fallback browser. |
| `HttpException`                | `HTTP_ERROR`                | An unexpected HTTP error occurred (includes status code).   | Show checkout in a fallback browser. |
| `CheckoutUnavailableException` | varies                      | Checkout is unavailable for another reason.                 | Show checkout in a fallback browser. |

#### Exception Hierarchy

```mermaid
---
title: Checkout Kit Exception Hierarchy
---
classDiagram
    CheckoutException <|-- ConfigurationException
    CheckoutException <|-- CheckoutExpiredException
    CheckoutException <|-- CheckoutSheetKitException
    CheckoutException <|-- CheckoutUnavailableException
    CheckoutUnavailableException <|-- HttpException
    CheckoutUnavailableException <|-- ClientException

    <<Abstract>> CheckoutException
    CheckoutException : +String errorDescription
    CheckoutException : +String errorCode
    CheckoutException : +bool isRecoverable

    class ConfigurationException{
        note: "Store or checkout configuration issues."
    }
    class CheckoutExpiredException{
        note: "Expired or invalid carts/checkouts."
    }
    class CheckoutUnavailableException{
        note: "Unexpected errors."
    }
    class HttpException{
        note: "Unexpected Http response"
        +int statusCode
    }
    class ClientException{
        note: "Unexpected client/web error"
    }
    class CheckoutSheetKitException{
        note: "Error in Checkout Kit code"
    }
```

## Integrating identity & customer accounts

Buyer-aware checkout experience reduces friction and increases conversion. Depending on the context
of the buyer (guest or signed-in), knowledge of buyer preferences, or account/identity system, the
application can use on of the following methods to initialize personalized and contextualized buyer
experience.

### Cart: buyer bag, identity, and preferences

In addition to specifying the line items, the Cart can include buyer identity (name, email, address,
etc.), and delivery and payment preferences:
see [guide](https://shopify.dev/docs/custom-storefronts/building-with-the-storefront-api/cart/manage).
Included information will be used to present pre-filled and pre-selected choices to the buyer within
checkout.

### Multipass

[Shopify Plus](https://help.shopify.com/en/manual/intro-to-shopify/pricing-plans/plans-features/shopify-plus-plan)
merchants
using [Classic Customer Accounts](https://help.shopify.com/en/manual/customers/customer-accounts/classic-customer-accounts)
can use [Multipass](https://shopify.dev/docs/api/multipass) to integrate an external identity system
and initialize a buyer-aware checkout session.

```json
{
  "email": "<Customer's email address>",
  "created_at": "<Current timestamp in ISO8601 encoding>",
  "remote_ip": "<Client IP address>",
  "return_to": "<Checkout URL obtained from Storefront API>",
  ...
}
```

1. Follow the [Multipass documentation](https://shopify.dev/docs/api/multipass) to create a
   multipass
   URL and set the `'return_to'` to be the obtained `checkoutUrl`
2. Provide the Multipass URL to `ShopifyCheckoutSheetKit.present()`.

> [!Important]
> the above JSON omits useful customer attributes that should be provided where possible and
> encryption and signing should be done server-side to ensure Multipass keys are kept secret.

> [!NOTE]
> Multipass errors are not "recoverable" (See [Error Handling](#error-handling)) due to their one-time nature. Failed requests containing multipass URLs
> will require re-generating new tokens.

### Shop Pay

To initialize accelerated Shop Pay checkout, the cart can set a
[walletPreference](https://shopify.dev/docs/api/storefront/latest/mutations/cartBuyerIdentityUpdate#field-cartbuyeridentityinput-walletpreferences)
to 'shop_pay'. The sign-in state of the buyer is app-local and the buyer will be prompted to sign in
to their Shop account on their first checkout, and their sign-in state will be remembered for future
checkout sessions.

### Customer Account API

The Customer Account API allows you to authenticate buyers and provide a personalized checkout experience.
For detailed implementation instructions, see our [Customer Account API Authentication Guide](https://shopify.dev/docs/storefronts/headless/mobile-apps/checkout-sheet-kit/authenticate-checkouts).

---

## Contributing

We welcome code contributions, feature requests, and reporting of issues. Please
see [guidelines and instructions](.github/CONTRIBUTING.md).

## License

Shopify's Checkout Kit is provided under an [MIT License](LICENSE).
