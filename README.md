# Shopify Checkout Sheet Kit - Android

[![GitHub license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)](/LICENSE)
![Tests](https://github.com/Shopify/checkout-sheet-kit-android/actions/workflows/test.yml/badge.svg?branch=main)

![image](https://github.com/Shopify/checkout-sheet-kit-android/assets/2034704/c6c726dc-a211-406b-b848-53ade91a164d)

**Checkout Sheet Kit for Android** is a library that enables Android apps to provide the world's highest converting, customizable, one-page checkout within the mobile app. The presented experience is a fully-featured checkout that preserves all of the store customizations: Checkout UI extensions, Scripts, Functions, Web Pixels, and more. It also provides idiomatic defaults such as support for light and dark mode, and convenient developer APIs to embed, customize and follow the lifecycle of the checkout experience. Check out our developer blog to [learn how Checkout Sheet Kit is built](https://www.shopify.com/partners/blog/mobile-checkout-sdks-for-ios-and-android).

### Requirements

- JDK 11+
- Android SDK 23+
- The SDK is not compatible with checkout.liquid. The Shopify Store must be migrated for extensibility

### Getting Started

The SDK is an open source Android library. As a quick start, see
[sample projects](samples/README.md) or use one of the following ways to integrate the SDK into
your project:

#### Gradle

```groovy
implementation "com.shopify:checkout-sheet-kit:$checkoutSdkVersion"
```

#### Maven

```xml

<dependency>
   <groupId>com.shopify</groupId>
   <artifactId>checkout-sheet-kit</artifactId>
   <version>${checkoutSdkVersion}</version>
</dependency>
```

### Basic Usage

Once the SDK has been added as a dependency, you can import the library:

```kotlin
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
```

To present a checkout to the buyer, your application must first obtain a checkout URL.
The most common way is to use the [Storefront GraphQL API](https://shopify.dev/docs/api/storefront)
to assemble a cart (via `cartCreate` and related update mutations) and query the
[checkoutUrl](https://shopify.dev/docs/api/storefront/2023-10/objects/Cart#field-cart-checkouturl).
You can use any GraphQL client to accomplish this and we recommend
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
To present a native checkout modal in your Android application, provide
the `checkoutUrl` alongside optional runtime configuration settings to the `present(checkoutUrl)`
function provided by the SDK:

```kotlin
fun presentCheckout() {
    val checkoutUrl = cart.checkoutUrl
    ShopifyCheckoutSheetKit.present(checkoutUrl, context, checkoutEventProcessor)
}
```

To help optimize and deliver the best experience the SDK also provides a
[preloading API](#preloading) that can be used to initialize the checkout session in the background
and ahead of time.

### Configuration

The SDK provides a way to customize the presented checkout experience via
the `ShopifyCheckoutSheetKit.configure` function.

#### `colorScheme`

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
            spinnerColor = Color.ResourceId(R.color.loading_spinner),
        )
    )
}
```

- _Note: use preloading to optimize and deliver an instant buyer experience._
- _Note: Colors can also be specified in sRGB format (e.g. `Color.SRGB(-0xff0001)`)_
- _Note: Colors can also be overridden for Light/Dark/Automatic themes, e.g:_

```kotlin
val automatic = ColorScheme.Automatic(
    lightColors = Colors(
        headerBackground = Color.ResourceId(R.color.headerLight),
        headerFont = Color.ResourceId(R.color.headerFontLight),
        webViewBackground = Color.ResourceId(R.color.webViewBgLight),
        spinnerColor = Color.ResourceId(R.color.spinnerLight),
    ),
    darkColors = Colors(
        headerBackground = Color.ResourceId(R.color.headerDark),
        headerFont = Color.ResourceId(R.color.headerFontDark,
        webViewBackground = Color.ResourceId(R.color.webViewBgDark),
        spinnerColor = Color.ResourceId(R.color.spinnerDark),
    )
)
```

The current configuration can be obtained by calling `ShopifyCheckoutSheetKit.getConfiguration()`.

### Preloading

Initializing a checkout session requires communicating with Shopify servers and, depending
on the network weather and the quality of the buyer's connection, can result in undesirable
waiting time for the buyer. To help optimize and deliver the best experience, the SDK provides
a preloading hint that allows app developers to signal and initialize the checkout session in
the background and ahead of time.

Preloading is an advanced feature that can be disabled via a runtime flag:

```kotlin
ShopifyCheckoutSheetKit.configure {
    it.preloading = Preloading(enabled = false)
}
```

Once enabled, preloading a checkout is as simple as:

```kotlin
ShopifyCheckoutSheetKit.preload(checkoutUrl)
```

**Important considerations:**

1. Initializing preload results in background network requests and additional CPU/memory utilization
   for the client, and should be used when there is a high likelihood that the buyer will soon
   request to checkout-e.g. when the buyer navigates to the cart overview or similar app-specific
   experience.
2. A preloaded checkout session reflects the cart contents at the time when `preload` is called. If
   the cart is updated after `preload` is called, the application needs to call `preload` again to
   reflect the updated checkout session.
3. Calling `preload(checkoutUrl)` is a hint, not a guarantee. The library may debounce or ignore
   calls to this API depending on various conditions; the preload may not complete before
   `present(checkoutUrl)` is called, in which case the buyer may still see a spinner while the
   checkout session is finalized.

### Monitoring the lifecycle of a checkout session

You can use the `WebEventProcessor` interface to register callbacks for key lifecycle events
during the checkout session:

```kotlin
val processor = object : WebEventProcessor {
    override fun onCheckoutCompleted() {
        // Called when the checkout was completed successfully by the buyer.
        // Use this to update UI, reset cart state, etc.
    }

    override fun onCheckoutCanceled() {
        // Called when the checkout was canceled by the buyer.
    }

    override fun onCheckoutFailed(error: CheckoutException) {
        /**
         * Called when the checkout encountered an error and has been aborted.
         * CheckoutException will be one of the following:
         */

        /**
         * Issued when an internal error within Shopify Checkout SDK.
         * In event of an sdkError you could use the stacktrace to inform you of how to proceed,
         * if the issue persists, it is recommended to open a bug report in https://github.com/Shopify/checkout-sheet-kit-android
         */
        class CheckoutSdkError(errorMsg: String) : CheckoutException(errorMsg)

        /**
         * Issued when checkout has encountered a unrecoverable error (for example server side error).
         * if the issue persists, it is recommended to open a bug report in https://github.com/Shopify/checkout-sheet-kit-android
         */
        class CheckoutUnavailableException : CheckoutException("Checkout is currently unavailable due to an internal error.")

        /**
         * Issued when checkout is no longer available and will no longer be available with the checkout URL supplied.
         * This may happen when the user has paused on checkout for a long period (hours) and
         * then attempted to proceed again with the same checkout URL.
         * In event of checkoutExpired, a new checkout URL will need to be generated.
         */
        class CheckoutExpiredException :
            CheckoutException("Checkout is no longer available with the token provided. Please generate a new checkout URL.")

        /**
         * Issued when the provided checkout URL results in an error related to shop being on checkout.liquid.
         * The SDK only supports stores migrated for extensibility.
         */
        class CheckoutLiquidNotMigratedException :
            CheckoutException("The checkout URL provided has resulted in an error because the store is still using checkout.liquid. Checkout Sheet Kit only supports checkout with extensibility.")


    }

    override fun onCheckoutLinkClicked(uri: Uri) {
        // Called when the buyer clicks a link within the checkout experience:
        // - email address (`mailto:`)
        // - telephone number (`tel:`)
        // - web (http:)
        // and is being directed outside the application.
    }

    override fun onWebPixelEvent(event: PixelEvent) {
        // Called when a web pixel event is emitted in checkout.
        // Use this to submit events to your analytics system, see below.
    }
}

```

#### Integrating with Web Pixels, monitoring behavioral data

App developers can use [lifecycle events](#monitoring-the-lifecycle-of-a-checkout-session) to
monitor and log the status of a checkout session.

To safeguard user privacy, [Web Pixel](https://shopify.dev/docs/apps/marketing/pixels) events will not be dispatched from within the checkout webview. Instead, the events will be relayed back to your application through the `onWebPixelEvent` checkout event processor function.

Implement this function to process the events you're interested in, augment them with customer and session identity, transform them into an appropriate schema and submit them to your preferred analytics system. For example:

```kotlin
fun onWebPixelEvent(event: PixelEvent) {
    if (!hasPermissionToCaptureEvents()) {
        return
    }

    when (event) {
        is StandardPixelEvent -> processStandardEvent(event)
        is CustomPixelEvent -> processCustomEvent(event)
    }
}

fun processStandardEvent(event: StandardPixelEvent) {
    const endpoint = "https://example.com/pixel?id=${accountID}&uid=${userId}";

    val payload = AnalyticsPayload(
        eventTime: event.timestamp,
        action: event.name,
        details: event.data.checkout
    )

    // Send events to third-party servers
    httpClient.post(endpoint, payload)
}

// ... other functions, incl. processCustomEvent(event)
```

_Note: You will likely need to augment these events with customer/session information derived from app state._

_Note: The `customData` attribute of CustomPixelEvent can take on any shape. As such, this attribute will be returned as a String. Client applications should define a custom data type and deserialize the `customData` string into that type._

### Integrating identity & customer accounts

Buyer-aware checkout experience reduces friction and increases conversion. Depending on the context
of the buyer (guest or signed-in), knowledge of buyer preferences, or account/identity system, the
application can use on of the following methods to initialize personalized and contextualized buyer
experience.

#### Cart: buyer bag, identity, and preferences

In addition to specifying the line items, the Cart can include buyer identity (name, email, address,
etc.), and delivery and payment preferences:
see [guide](https://shopify.dev/docs/custom-storefronts/building-with-the-storefront-api/cart/manage).
Included information will be used to present pre-filled and pre-selected choices to the buyer within
checkout.

#### Multipass

[Shopify Plus](https://help.shopify.com/en/manual/intro-to-shopify/pricing-plans/plans-features/shopify-plus-plan)
merchants
using [Classic Customer Accounts](https://help.shopify.com/en/manual/customers/customer-accounts/classic-customer-accounts)
can use [Multipass](https://shopify.dev/docs/api/multipass) to integrate an external identity system
and initialize a buyer-aware checkout session.

#### Multipass

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

_Note: the above JSON omits useful customer attributes that should be provided where possible and
encryption and signing should be done server-side to ensure Multipass keys are kept secret._

#### Shop Pay

To initialize accelerated Shop Pay checkout, the cart can set a
[walletPreference](https://shopify.dev/docs/api/storefront/latest/mutations/cartBuyerIdentityUpdate#field-cartbuyeridentityinput-walletpreferences)
to 'shop_pay'. The sign-in state of the buyer is app-local and the buyer will be prompted to sign in
to their Shop account on their first checkout, and their sign-in state will be remembered for future
checkout sessions.

#### Customer Account API

We are working on a library to provide buyer sign-in and authentication powered by the
[new Customer Account API](https://www.shopify.com/partners/blog/introducing-customer-account-api-for-headless-stores)
—stay tuned.

---

### Contributing

We welcome code contributions, feature requests, and reporting of issues. Please
see [guidelines and instructions](.github/CONTRIBUTING.md).

### License

Shopify's Checkout Sheet Kit is provided under an [MIT License](LICENSE).
