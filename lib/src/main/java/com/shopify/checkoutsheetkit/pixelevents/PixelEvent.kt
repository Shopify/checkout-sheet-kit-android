/*
 * MIT License
 *
 * Copyright 2023-present, Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.shopify.checkoutsheetkit.pixelevents

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject

@Serializable
internal class PixelEventWrapper(
    internal val name: String,
    internal val event: JsonObject,
)

@Serializable
public enum class EventType(public val typeName: String) {
    @SerialName("standard") STANDARD("standard"),
    @SerialName("extended-standard") EXTENDED_STANDARD("extended-standard"),
    @SerialName("custom") CUSTOM("custom");

    public companion object {
        public fun fromTypeName(typeName: String?): EventType? {
            if (typeName == null) {
                return null
            }
            return entries.firstOrNull {
                it.typeName == typeName
            }
        }
    }
}

public sealed interface PixelEvent {
    /**
     * The ID of the customer event
     */
    public val id: String?

    /**
     * The name of the customer event
     */
    public val name: String?

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    public val timestamp: String?

    /**
     * The type of event, standard or custom.
     * See https://shopify.dev/docs/api/web-pixels-api#customer-events-reference
     */
    public val type: EventType?
}

@Serializable
public data class AlertDisplayedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: AlertDisplayedPixelEventData? = null,
): PixelEvent

@Serializable
public data class UIExtensionErroredPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: UIExtensionErroredPixelEventData? = null,
): PixelEvent

/**
 * An object that contains information about an error that was displayed to a buyer.
 */
@Serializable
public data class AlertDisplayedPixelEventData(
    public val alert: AlertDisplayedPixelEventAlert? = null
)

@Serializable
public data class AlertDisplayedPixelEventAlert(
    /**
     * The part of the page the error relates to.
     * Follows the [Shopify Functions target format], for example "cart.deliveryGroups[0].deliveryAddress.address1".
     */
    public val target: String? = null,

    /**
     * The value of the field at the time the error occurred or null if the error does not relate to an individual field.
     */
    public val value: String? = null,

    /**
     * The type of error that occurred, current possible values are:
     *  - `INPUT_REQUIRED` - A required field is empty.
     *  - `INPUT_INVALID` - The input provided is incorrect or improperly formatted.
     *  - `CONTACT_ERROR` - An alert related to a contact information issue was displayed.
     *  - `DELIVERY_ERROR` - An alert related to a delivery issue was displayed.
     *  - `PAYMENT_ERROR` - An alert related to a payment issue was displayed.
     *  - `DISCOUNT_ERROR` - An alert related to a discount code or gift card issue was displayed.
     *  - `INVENTORY_ERROR` - An alert related to an inventory issue was displayed.
     *  - `MERCHANDISE_ERROR` - An alert related to a merchandise issue was displayed.
     *  - `CHECKOUT_ERROR` - An alert related to a general checkout issue was displayed.
     */
    public val type: String? = null,

    /**
     * The alert message that was displayed to the user.
     */
    public val message: String? = null,
)

/**
 * An object that contains data about a UI Extension error that occurred.
 */
@Serializable
public data class UIExtensionErroredPixelEventData(
    public val error: UIExtensionErrorPixelEventError? = null
)

@Serializable
public data class UIExtensionErrorPixelEventError(
    /**
     * The type of error that occurred. Current possible values:
     *   - `EXTENSION_USAGE_ERROR` - An error caused by incorrect usage of extension APIs or UI components.
     */
    public val type: String? = null,

    /**
     * The unique identifier of the app that the extension belongs to.
     */
    public val appId: String? = null,

    /**
     * The name of the app that the extension belongs to.
     */
    public val appName: String? = null,

    /**
     * The API version used by the extension
     */
    public val apiVersion: String? = null,

    /**
     * The version of the app that encountered the error.
     */
    public val appVersion: String? = null,

    /**
     * The name of the extension that encountered the error.
     */
    public val extensionName: String? = null,

    /**
     * The [target](https://shopify.dev/docs/api/checkout-ui-extensions/latest/targets) of the extension, for example
     * "purchase.checkout.delivery-address.render-after".
     */
    public val extensionTarget: String? = null,

    /**
     * The [placement reference](https://shopify.dev/docs/apps/build/checkout/test-checkout-ui-extensions#dynamic-targets) of the extension,
     * only populated for dynamic targets.
     */
    public val placementReference: String? = null,

    /**
     * The message associated with the error that occurred.
     */
    public val message: String? = null,

    /**
     * The stack trace associated with the error that occurred.
     */
    public val trace: String? = null,
)

@Serializable
public data class PageViewedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
): PixelEvent

@Serializable
public data class CheckoutStartedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class CheckoutCompletedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class CheckoutAddressInfoSubmittedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class CheckoutContactInfoSubmittedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class CheckoutShippingInfoSubmittedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class PaymentInfoSubmittedPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutPixelEventData? = null,
): PixelEvent

@Serializable
public data class CheckoutPixelEventData(
    public val checkout: Checkout? = null
)

/**
 * A snapshot of various read-only properties of the browser at the time of
 * event
 */
@Serializable
public data class Context(
    /**
     * Snapshot of a subset of properties of the `document` object in the top
     * frame of the browser
     */
    public val document: Document? = null,

    /**
     * Snapshot of a subset of properties of the `navigator` object in the top
     * frame of the browser
     */
    public val navigator: Navigator? = null,

    /**
     * Snapshot of a subset of properties of the `window` object in the top frame
     * of the browser
     */
    public val window: Window? = null
)

/**
 * Snapshot of a subset of properties of the `document` object in the top
 * frame of the browser
 *
 * A snapshot of a subset of properties of the `document` object in the top
 * frame of the browser
 */
@Serializable
public data class Document(
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the character set being used by the document
     */
    public val characterSet: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the URI of the current document
     */
    public val location: Location? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the URI of the page that linked to this page
     */
    public val referrer: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the title of the current document
     */
    public val title: String? = null
)

/**
 * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
 * returns the URI of the current document
 *
 * A snapshot of a subset of properties of the `location` object in the top
 * frame of the browser
 *
 * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
 * location, or current URL, of the window object
 */
@Serializable
public data class Location(
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing a `'#'` followed by the fragment identifier of the URL
     */
    public val hash: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the host, that is the hostname, a `':'`, and the port of
     * the URL
     */
    public val host: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the domain of the URL
     */
    public val hostname: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the entire URL
     */
    public val href: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the canonical form of the origin of the specific location
     */
    public val origin: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing an initial `'/'` followed by the path of the URL, not
     * including the query string or fragment
     */
    public val pathname: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the port number of the URL
     */
    public val port: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the protocol scheme of the URL, including the final `':'`
     */
    public val protocol: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing a `'?'` followed by the parameters or "querystring" of
     * the URL
     */
    public val search: String? = null
)

/**
 * Snapshot of a subset of properties of the `navigator` object in the top
 * frame of the browser
 *
 * A snapshot of a subset of properties of the `navigator` object in the top
 * frame of the browser
 */
@Serializable
public data class Navigator(
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns `false` if setting a cookie will be ignored and true otherwise
     */
    public val cookieEnabled: Boolean? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns a string representing the preferred language of the user, usually
     * the language of the browser UI. The `null` value is returned when this
     * is unknown
     */
    public val language: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns an array of strings representing the languages known to the user,
     * by order of preference
     */
    public val languages: List<String>? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns the user agent string for the current browser
     */
    public val userAgent: String? = null
)

/**
 * Snapshot of a subset of properties of the `window` object in the top frame
 * of the browser
 *
 * A snapshot of a subset of properties of the `window` object in the top frame
 * of the browser
 */
@Serializable
public data class Window(
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window),
     * gets the height of the content area of the browser window including, if
     * rendered, the horizontal scrollbar
     */
    public val innerHeight: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the width of the content area of the browser window including, if rendered,
     * the vertical scrollbar
     */
    public val innerWidth: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * location, or current URL, of the window object
     */
    public val location: Location? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * global object's origin, serialized as a string
     */
    public val origin: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the height of the outside of the browser window
     */
    public val outerHeight: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the width of the outside of the browser window
     */
    public val outerWidth: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), an
     * alias for window.scrollX
     */
    public val pageXOffset: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), an
     * alias for window.scrollY
     */
    public val pageYOffset: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen), the
     * interface representing a screen, usually the one on which the current
     * window is being rendered
     */
    public val screen: Screen? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * horizontal distance from the left border of the user's browser viewport to
     * the left side of the screen
     */
    public val screenX: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * vertical distance from the top border of the user's browser viewport to the
     * top side of the screen
     */
    public val screenY: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * number of pixels that the document has already been scrolled horizontally
     */
    public val scrollX: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * number of pixels that the document has already been scrolled vertically
     */
    public val scrollY: Double? = null
)

/**
 * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen), the
 * interface representing a screen, usually the one on which the current
 * window is being rendered
 *
 * The interface representing a screen, usually the one on which the current
 * window is being rendered
 */
@Serializable
public data class Screen(
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen/height),
     * the height of the screen
     */
    public val height: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen/width),
     * the width of the screen
     */
    public val width: Double? = null
)

/**
 * A monetary value with currency.
 */
@Serializable
public data class MoneyV2(
    /**
     * The decimal money amount.
     */
    public val amount: Double? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    public val currencyCode: String? = null
)

/**
 * The merchandise that the buyer intends to purchase.
 *
 * A product variant represents a different version of a product, such as
 * differing sizes or differing colors.
 */
@Serializable
public data class ProductVariant(
    /**
     * A globally unique identifier.
     */
    public val id: String? = null,

    /**
     * Image associated with the product variant. This field falls back to the
     * product image if no image is available.
     */
    public val image: Image? = null,

    /**
     * The product variant’s price.
     */
    public val price: MoneyV2? = null,

    /**
     * The product object that the product variant belongs to.
     */
    public val product: Product? = null,

    /**
     * The SKU (stock keeping unit) associated with the variant.
     */
    public val sku: String? = null,

    /**
     * The product variant’s title.
     */
    public val title: String? = null,

    /**
     * The product variant’s untranslated title.
     */
    public val untranslatedTitle: String? = null
)

/**
 * An image resource.
 */
@Serializable
public data class Image(
    /**
     * The location of the image as a URL.
     */
    public val src: String? = null
)

/**
 * The product object that the product variant belongs to.
 *
 * A product is an individual item for sale in a Shopify store.
 */
@Serializable
public data class Product(
    /**
     * The ID of the product.
     */
    public val id: String? = null,

    /**
     * The product’s title.
     */
    public val title: String? = null,

    /**
     * The [product
     * type](https://help.shopify.com/en/manual/products/details/product-type)
     * specified by the merchant.
     */
    public val type: String? = null,

    /**
     * The product’s untranslated title.
     */
    public val untranslatedTitle: String? = null,

    /**
     * The relative URL of the product.
     */
    public val url: String? = null,

    /**
     * The product’s vendor name.
     */
    public val vendor: String? = null
)

/**
 * A container for all the information required to add items to checkout and
 * pay.
 */
@Serializable
public data class Checkout(
    /**
     * A list of attributes accumulated throughout the checkout process.
     */
    public val attributes: List<Attribute>? = null,

    /**
     * The billing address to where the order will be charged.
     */
    public val billingAddress: MailingAddress? = null,

    /**
     * Indicates whether the customer has consented to be sent marketing material via email.
     */
    public val buyerAcceptsEmailMarketing: Boolean? = null,

    /**
     * Indicates whether the customer has consented to be sent marketing material via SMS.
     */
    public val buyerAcceptsSmsMarketing: Boolean? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    public val currencyCode: String? = null,

    /**
     * Represents the selected delivery options for a checkout.
     */
    public val delivery: Delivery? = null,

    /**
     * A list of discount applications.
     */
    public val discountApplications: List<DiscountApplication>? = null,

    /**
     * The total amount of the discounts applied to the price of the checkout.
     */
    public val discountsAmount: MoneyV2? = null,

    /**
     * The email attached to this checkout.
     */
    public val email: String? = null,

    /**
     * A list of line item objects, each one containing information about an item
     * in the checkout.
     */
    public val lineItems: List<CheckoutLineItem>? = null,

    /**
     * Information about the active localized experience.
     */
    public val localization: Localization? = null,

    /**
     * The resulting order from a paid checkout.
     */
    public val order: Order? = null,

    /**
     * A unique phone number for the customer. Formatted using E.164 standard. For
     * example, *+16135551111*.
     */
    public val phone: String? = null,

    /**
     * The shipping address to where the line items will be shipped.
     */
    public val shippingAddress: MailingAddress? = null,

    /**
     * Once a shipping rate is selected by the customer it is transitioned to a
     * `shipping_line` object.
     */
    public val shippingLine: ShippingRate? = null,

    /**
     * The phone number provided by the buyer after opting in to SMS marketing.
     */
    public val smsMarketingPhone: String? = null,

    /**
     * The price at checkout before duties, shipping, and taxes.
     */
    public val subtotalPrice: MoneyV2? = null,

    /**
     * A unique identifier for a particular checkout.
     */
    public val token: String? = null,

    /**
     * The sum of all the prices of all the items in the checkout, including
     * duties, taxes, and discounts.
     */
    public val totalPrice: MoneyV2? = null,

    /**
     * The sum of all the taxes applied to the line items and shipping lines in
     * the checkout.
     */
    public val totalTax: MoneyV2? = null,

    /**
     * A list of transactions associated with a checkout or order.
     */
    public val transactions: List<Transaction>? = null
)

/**
 * Custom attributes left by the customer to the merchant, either in their cart
 * or during checkout.
 */
@Serializable
public data class Attribute(
    /**
     * The key for the attribute.
     */
    public val key: String? = null,

    /**
     * The value for the attribute.
     */
    public val value: String? = null
)

@Serializable
public data class Country(
    /**
     * The ISO-3166-1 code for this country, for example, "US".
     */
    public val isoCode: String? = null,
)

@Serializable
public data class Language(
    /**
     * The BCP-47 language tag. It may contain a dash followed by an ISO 3166-1 alpha-2 region code, for example, "en-US".
     */
    public val isoCode: String? = null,
)

@Serializable
public data class Market(
    /**
     * A human-readable, shop-scoped identifier.
     */
    public val handle: String? = null,

    /**
     * A globally unique identifier.
     */
    public val id: String? = null,
)

@Serializable
public data class Localization(
    /**
     * The country of the active localized experience.
     */
    public val country: Country? = null,

    /**
     * The language of the active localized experience.
     */
    public val language: Language? = null,

    /**
     * The market including the country of the active localized experience.
     */
    public val market: Market? = null,
)

/**
 * A mailing address for customers and shipping.
 */
@Serializable
public data class MailingAddress(
    /**
     * The first line of the address. This is usually the street address or a P.O.
     * Box number.
     */
    public val address1: String? = null,

    /**
     * The second line of the address. This is usually an apartment, suite, or
     * unit number.
     */
    public val address2: String? = null,

    /**
     * The name of the city, district, village, or town.
     */
    public val city: String? = null,

    /**
     * The name of the country.
     */
    public val country: String? = null,

    /**
     * The two-letter code that represents the country, for example, US.
     * The country codes generally follows ISO 3166-1 alpha-2 guidelines.
     */
    public val countryCode: String? = null,

    /**
     * The customer’s first name.
     */
    public val firstName: String? = null,

    /**
     * The customer’s last name.
     */
    public val lastName: String? = null,

    /**
     * The phone number for this mailing address as entered by the customer.
     */
    public val phone: String? = null,

    /**
     * The region of the address, such as the province, state, or district.
     */
    public val province: String? = null,

    /**
     * The two-letter code for the region.
     * For example, ON.
     */
    public val provinceCode: String? = null,

    /**
     * The ZIP or postal code of the address.
     */
    public val zip: String? = null
)

/**
 * The information about the intent of the discount.
 */
@Serializable
public data class DiscountApplication(
    /**
     * The method by which the discount's value is applied to its entitled items.
     *
     * - `ACROSS`: The value is spread across all entitled lines.
     * - `EACH`: The value is applied onto every entitled line.
     */
    public val allocationMethod: String? = null,

    /**
     * How the discount amount is distributed on the discounted lines.
     *
     * - `ALL`: The discount is allocated onto all the lines.
     * - `ENTITLED`: The discount is allocated onto only the lines that it's
     * entitled for.
     * - `EXPLICIT`: The discount is allocated onto explicitly chosen lines.
     */
    public val targetSelection: String? = null,

    /**
     * The type of line (i.e. line item or shipping line) on an order that the
     * discount is applicable towards.
     *
     * - `LINE_ITEM`: The discount applies onto line items.
     * - `SHIPPING_LINE`: The discount applies onto shipping lines.
     */
    public val targetType: String? = null,

    /**
     * The customer-facing name of the discount. If the type of discount is
     * a `DISCOUNT_CODE`, this `title` attribute represents the code of the
     * discount.
     */
    public val title: String? = null,

    /**
     * The type of the discount.
     *
     * - `AUTOMATIC`: A discount automatically at checkout or in the cart without
     * the need for a code.
     * - `DISCOUNT_CODE`: A discount applied onto checkouts through the use of
     * a code.
     * - `MANUAL`: A discount that is applied to an order by a merchant or store
     * owner manually, rather than being automatically applied by the system or
     * through a script.
     * - `SCRIPT`: A discount applied to a customer's order using a script
     */
    public val type: String? = null,

    /**
     * The value of the discount. Fixed discounts return a `Money` Object, while
     * Percentage discounts return a `PricingPercentageValue` object.
     */
    public val value: Value? = null
)

/**
 * The value of the discount.
 */
@Serializable
public data class Value(
    /**
     * The decimal money amount.
     */
    public val amount: Double? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    public val currencyCode: String? = null,

    /**
     * The percentage value of the object.
     */
    public val percentage: Double? = null
)

/**
 * A single line item in the checkout, grouped by variant and attributes.
 */
@Serializable
public data class CheckoutLineItem(
    /**
     * The discounts that have been applied to the checkout line item by a
     * discount application.
     */
    public val discountAllocations: List<DiscountAllocation>? = null,

    /**
     * The combined price of all of the items in the line item after line-level discounts have been applied.
     */
    public val finalLinePrice: MoneyV2? = null,

    /**
     * A globally unique identifier.
     */
    public val id: String? = null,

    /**
     * The properties of the line item. A shop may add, or enable customers to add custom information to a line item. Line item properties
     * consist of a key and value pair.
     */
    public val properties: List<Property>? = null,

    /**
     * The quantity of the line item.
     */
    public val quantity: Double? = null,

    /**
     * The selling plan associated with the line item and the effect that each selling plan has on variants when they're purchased.
     */
    public val sellingPlanAllocation: SellingPlanAllocation? = null,

    /**
     * The title of the line item. Defaults to the product's title.
     */
    public val title: String? = null,

    /**
     * Product variant of the line item.
     */
    public val variant: ProductVariant? = null
)

@Serializable
public data class Delivery(
    /**
     * The selected delivery options for the event.
     */
    public val selectedDeliveryOptions: List<DeliveryOption>? = null,
)

@Serializable
public data class DeliveryOption(
    /**
     * The cost of the delivery option.
     */
    public val cost: MoneyV2? = null,

    /**
     * The cost of the delivery option after discounts have been applied.
     */
    public val costAfterDiscounts: MoneyV2? = null,

    /**
     * The description of the delivery option.
     */
    public val description: String? = null,

    /**
     * The unique identifier of the delivery option.
     */
    public val handle: String? = null,

    /**
     * The title of the delivery option.
     */
    public val title: String? = null,

    /**
     * The type of delivery option, e.g. pickup, pickupPoint, shipping, local.
     */
    public val type: String? = null,
)

/**
 * The discount that has been applied to the checkout line item.
 */
@Serializable
public data class DiscountAllocation(
    /**
     * The monetary value with currency allocated to the discount.
     */
    public val amount: MoneyV2? = null,

    /**
     * The information about the intent of the discount.
     */
    public val discountApplication: DiscountApplication? = null
)

/**
 * An order is a customer’s completed request to purchase one or more products
 * from a shop. An order is created when a customer completes the checkout
 * process.
 */
@Serializable
public data class Order(

    /**
     * The customer that placed the order.
     */
    public val customer: OrderCustomer? = null,

    /**
     * The ID of the order.
     */
    public val id: String? = null
)

@Serializable
public data class OrderCustomer(
    /**
     * The ID of the customer.
     */
    public val id: String? = null,
)

@Serializable
public data class Property(
    /**
     * The key for the property.
     */
    public val key: String? = null,
    /**
     * The value for the property.
     */
    public val value: String? = null,
)

@Serializable
public data class SellingPlanAllocation(
    /**
     * A representation of how products and variants can be sold and purchased. For example, an individual selling plan could be
     * '6 weeks of prepaid granola, delivered weekly'.
     */
    public val sellingPlan: SellingPlan,
)

@Serializable
public data class SellingPlan(
    /**
     * A globally unique identifier.
     */
    public val id: String? = null,
    /**
     * The name of the selling plan. For example, '6 weeks of prepaid granola, delivered weekly'.
     */
    public val name: String? = null,
)

/**
 * A shipping rate to be applied to a checkout.
 */
@Serializable
public data class ShippingRate(
    /**
     * Price of this shipping rate.
     */
    public val price: MoneyV2? = null
)

/**
 * A transaction associated with a checkout or order.
 */
@Serializable
public data class Transaction(
    /**
     * The monetary value with currency allocated to the transaction method.
     */
    public val amount: MoneyV2? = null,

    /**
     * The name of the payment provider used for the transaction.
     */
    public val gateway: String? = null,

    /**
     * The payment method used for the transaction.
     */
    public val paymentMethod: TransactionPaymentMethod? = null,
)

@Serializable
public data class TransactionPaymentMethod(
    /**
     * The name of the payment method used for the transaction. This may further specify the payment method used.
     */
    public val name: String? = null,

    /**
     * The type of payment method used for the transaction.
     *
     * - creditCard: A vaulted or manually entered credit card.
     * - redeemable: A redeemable payment method, such as a gift card or store credit.
     * - deferred: A deferred payment, such as invoicing the buyer and collecting payment later.
     * - local: A local payment method specific to the current region or market.
     * - manualPayment: A manual payment method, such as an in-person retail transaction.
     * - paymentOnDelivery: A payment that will be collected on delivery.
     * - wallet: An integrated wallet, such as PayPal, Google Pay, Apple Pay, etc.
     * - offsite: A payment processed outside of Shopify's checkout, excluding integrated wallets.
     * - customOnSite: A custom payment method that is processed through a checkout extension with a payments app.
     * - other: Another type of payment not defined here.
     */
    public val type: String? = null,
)

/**
 * This event represents any custom events emitted by partners or merchants via
 * the `publish` method
 */
@Serializable
public data class CustomPixelEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    // Clients are expected to define their own type for each custom event, and deserialize from String
    @Serializable(with = JsonObjectAsStringSerializer::class)
    public val customData: String? = null,
): PixelEvent

public object JsonObjectAsStringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("WithCustomDefault", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeSerializableValue(JsonObject.serializer()).toString()
    }
}

/**
 * A customer represents a customer account with the shop. Customer accounts
 * store contact information for the customer, saving logged-in customers the
 * trouble of having to provide it at every checkout.
 */
@Serializable
public data class Customer(
    /**
     * The customer’s email address.
     */
    public val email: String? = null,

    /**
     * The customer’s first name.
     */
    public val firstName: String? = null,

    /**
     * The ID of the customer.
     */
    public val id: String? = null,

    /**
     * The customer’s last name.
     */
    public val lastName: String? = null,

    /**
     * The total number of orders that the customer has placed.
     */
    public val ordersCount: Double? = null,

    /**
     * The customer’s phone number.
     */
    public val phone: String? = null
)

/**
 * A value given to a customer when a discount is applied to an order. The
 * application of a discount with this value gives the customer the specified
 * percentage off a specified item.
 */
@Serializable
public data class PricingPercentageValue(
    /**
     * The percentage value of the object.
     */
    public val percentage: Double? = null
)
