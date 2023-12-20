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
package com.shopify.checkoutkit.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject


/**
 * The `cart_viewed` event logs an instance where a customer visited the cart
 * page
 */
@Serializable
public data class CartViewed (
    val context: Context? = null,
    val data: CartViewedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

/**
 * A snapshot of various read-only properties of the browser at the time of
 * event
 */
@Serializable
public data class Context (
    /**
     * Snapshot of a subset of properties of the `document` object in the top
     * frame of the browser
     */
    val document: WebPixelsDocument? = null,

    /**
     * Snapshot of a subset of properties of the `navigator` object in the top
     * frame of the browser
     */
    val navigator: WebPixelsNavigator? = null,

    /**
     * Snapshot of a subset of properties of the `window` object in the top frame
     * of the browser
     */
    val window: WebPixelsWindow? = null
)

/**
 * Snapshot of a subset of properties of the `document` object in the top
 * frame of the browser
 *
 * A snapshot of a subset of properties of the `document` object in the top
 * frame of the browser
 */
@Serializable
public data class WebPixelsDocument (
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the character set being used by the document
     */
    val characterSet: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the URI of the current document
     */
    val location: Location? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the URI of the page that linked to this page
     */
    val referrer: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Document),
     * returns the title of the current document
     */
    val title: String? = null
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
public data class Location (
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing a `'#'` followed by the fragment identifier of the URL
     */
    val hash: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the host, that is the hostname, a `':'`, and the port of
     * the URL
     */
    val host: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the domain of the URL
     */
    val hostname: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the entire URL
     */
    val href: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the canonical form of the origin of the specific location
     */
    val origin: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing an initial `'/'` followed by the path of the URL, not
     * including the query string or fragment
     */
    val pathname: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the port number of the URL
     */
    val port: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing the protocol scheme of the URL, including the final `':'`
     */
    val protocol: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Location), a
     * string containing a `'?'` followed by the parameters or "querystring" of
     * the URL
     */
    val search: String? = null
)

/**
 * Snapshot of a subset of properties of the `navigator` object in the top
 * frame of the browser
 *
 * A snapshot of a subset of properties of the `navigator` object in the top
 * frame of the browser
 */
@Serializable
public data class WebPixelsNavigator (
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns `false` if setting a cookie will be ignored and true otherwise
     */
    val cookieEnabled: Boolean? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns a string representing the preferred language of the user, usually
     * the language of the browser UI. The `null` value is returned when this
     * is unknown
     */
    val language: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns an array of strings representing the languages known to the user,
     * by order of preference
     */
    val languages: List<String>? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Navigator),
     * returns the user agent string for the current browser
     */
    val userAgent: String? = null
)

/**
 * Snapshot of a subset of properties of the `window` object in the top frame
 * of the browser
 *
 * A snapshot of a subset of properties of the `window` object in the top frame
 * of the browser
 */
@Serializable
public data class WebPixelsWindow (
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window),
     * gets the height of the content area of the browser window including, if
     * rendered, the horizontal scrollbar
     */
    val innerHeight: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the width of the content area of the browser window including, if rendered,
     * the vertical scrollbar
     */
    val innerWidth: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * location, or current URL, of the window object
     */
    val location: Location? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * global object's origin, serialized as a string
     */
    val origin: String? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the height of the outside of the browser window
     */
    val outerHeight: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), gets
     * the width of the outside of the browser window
     */
    val outerWidth: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), an
     * alias for window.scrollX
     */
    val pageXOffset: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), an
     * alias for window.scrollY
     */
    val pageYOffset: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen), the
     * interface representing a screen, usually the one on which the current
     * window is being rendered
     */
    val screen: Screen? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * horizontal distance from the left border of the user's browser viewport to
     * the left side of the screen
     */
    val screenX: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * vertical distance from the top border of the user's browser viewport to the
     * top side of the screen
     */
    val screenY: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * number of pixels that the document has already been scrolled horizontally
     */
    val scrollX: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window), the
     * number of pixels that the document has already been scrolled vertically
     */
    val scrollY: Double? = null
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
public data class Screen (
    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen/height),
     * the height of the screen
     */
    val height: Double? = null,

    /**
     * Per [MDN](https://developer.mozilla.org/en-US/docs/Web/API/Screen/width),
     * the width of the screen
     */
    val width: Double? = null
)

@Serializable
public data class CartViewedData (
    val cart: Cart? = null
)

/**
 * A cart represents the merchandise that a customer intends to purchase, and
 * the estimated cost associated with the cart.
 */
@Serializable
public data class Cart (
    /**
     * The estimated costs that the customer will pay at checkout.
     */
    val cost: CartCost? = null,

    /**
     * A globally unique identifier.
     */
    val id: String? = null,

    val lines: List<CartLine>? = null,

    /**
     * The total number of items in the cart.
     */
    val totalQuantity: Double? = null
)

/**
 * The estimated costs that the customer will pay at checkout.
 *
 * The costs that the customer will pay at checkout. It uses
 * [`CartBuyerIdentity`](https://shopify.dev/api/storefront/reference/cart/cartb
 * uyeridentity) to determine [international pricing](https://shopify.dev/custom-
 * storefronts/internationalization/international-pricing#create-a-cart).
 */
@Serializable
public data class CartCost (
    /**
     * The total amount for the customer to pay.
     */
    val totalAmount: MoneyV2? = null
)

/**
 * The total amount for the customer to pay.
 *
 * A monetary value with currency.
 *
 * The total cost of the merchandise line.
 *
 * The product variant’s price.
 *
 * The monetary value with currency allocated to the discount.
 *
 * Price of this shipping rate.
 *
 * The price at checkout before duties, shipping, and taxes.
 *
 * The sum of all the prices of all the items in the checkout, including
 * duties, taxes, and discounts.
 *
 * The sum of all the taxes applied to the line items and shipping lines in
 * the checkout.
 *
 * The monetary value with currency allocated to the transaction method.
 */
@Serializable
public data class MoneyV2 (
    /**
     * The decimal money amount.
     */
    val amount: Double? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    val currencyCode: String? = null
)

/**
 * Information about the merchandise in the cart.
 */
@Serializable
public data class CartLine (
    /**
     * The cost of the merchandise that the customer will pay for at checkout. The
     * costs are subject to change and changes will be reflected at checkout.
     */
    val cost: CartLineCost? = null,

    /**
     * The merchandise that the buyer intends to purchase.
     */
    val merchandise: ProductVariant? = null,

    /**
     * The quantity of the merchandise that the customer intends to purchase.
     */
    val quantity: Double? = null
)

/**
 * The cost of the merchandise that the customer will pay for at checkout. The
 * costs are subject to change and changes will be reflected at checkout.
 *
 * The cost of the merchandise line that the customer will pay at checkout.
 */
@Serializable
public data class CartLineCost (
    /**
     * The total cost of the merchandise line.
     */
    val totalAmount: MoneyV2? = null
)

/**
 * The merchandise that the buyer intends to purchase.
 *
 * A product variant represents a different version of a product, such as
 * differing sizes or differing colors.
 */
@Serializable
public data class ProductVariant (
    /**
     * A globally unique identifier.
     */
    val id: String? = null,

    /**
     * Image associated with the product variant. This field falls back to the
     * product image if no image is available.
     */
    val image: Image? = null,

    /**
     * The product variant’s price.
     */
    val price: MoneyV2? = null,

    /**
     * The product object that the product variant belongs to.
     */
    val product: Product? = null,

    /**
     * The SKU (stock keeping unit) associated with the variant.
     */
    val sku: String? = null,

    /**
     * The product variant’s title.
     */
    val title: String? = null,

    /**
     * The product variant’s untranslated title.
     */
    val untranslatedTitle: String? = null
)

/**
 * An image resource.
 */
@Serializable
public data class Image (
    /**
     * The location of the image as a URL.
     */
    val src: String? = null
)

/**
 * The product object that the product variant belongs to.
 *
 * A product is an individual item for sale in a Shopify store.
 */
@Serializable
public data class Product (
    /**
     * The ID of the product.
     */
    val id: String? = null,

    /**
     * The product’s title.
     */
    val title: String? = null,

    /**
     * The [product
     * type](https://help.shopify.com/en/manual/products/details/product-type)
     * specified by the merchant.
     */
    val type: String? = null,

    /**
     * The product’s untranslated title.
     */
    val untranslatedTitle: String? = null,

    /**
     * The relative URL of the product.
     */
    val url: String? = null,

    /**
     * The product’s vendor name.
     */
    val vendor: String? = null
)

/**
 * The `checkout_address_info_submitted` event logs an instance of a customer
 * submitting their mailing address. This event is only available in checkouts
 * where checkout extensibility for customizations is enabled
 */
@Serializable
public data class CheckoutAddressInfoSubmitted (
    val context: Context? = null,
    val data: CheckoutAddressInfoSubmittedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CheckoutAddressInfoSubmittedData (
    val checkout: Checkout? = null
)

/**
 * A container for all the information required to add items to checkout and
 * pay.
 */
@Serializable
public data class Checkout (
    /**
     * A list of attributes accumulated throughout the checkout process.
     */
    val attributes: List<Attribute>? = null,

    /**
     * The billing address to where the order will be charged.
     */
    val billingAddress: MailingAddress? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    val currencyCode: String? = null,

    /**
     * A list of discount applications.
     */
    val discountApplications: List<DiscountApplication>? = null,

    /**
     * The email attached to this checkout.
     */
    val email: String? = null,

    /**
     * A list of line item objects, each one containing information about an item
     * in the checkout.
     */
    val lineItems: List<CheckoutLineItem>? = null,

    /**
     * The resulting order from a paid checkout.
     */
    val order: Order? = null,

    /**
     * A unique phone number for the customer. Formatted using E.164 standard. For
     * example, *+16135551111*.
     */
    val phone: String? = null,

    /**
     * The shipping address to where the line items will be shipped.
     */
    val shippingAddress: MailingAddress? = null,

    /**
     * Once a shipping rate is selected by the customer it is transitioned to a
     * `shipping_line` object.
     */
    val shippingLine: ShippingRate? = null,

    /**
     * The price at checkout before duties, shipping, and taxes.
     */
    val subtotalPrice: MoneyV2? = null,

    /**
     * A unique identifier for a particular checkout.
     */
    val token: String? = null,

    /**
     * The sum of all the prices of all the items in the checkout, including
     * duties, taxes, and discounts.
     */
    val totalPrice: MoneyV2? = null,

    /**
     * The sum of all the taxes applied to the line items and shipping lines in
     * the checkout.
     */
    val totalTax: MoneyV2? = null,

    /**
     * A list of transactions associated with a checkout or order.
     */
    val transactions: List<Transaction>? = null
)

/**
 * Custom attributes left by the customer to the merchant, either in their cart
 * or during checkout.
 */
@Serializable
public data class Attribute (
    /**
     * The key for the attribute.
     */
    val key: String? = null,

    /**
     * The value for the attribute.
     */
    val value: String? = null
)

/**
 * A mailing address for customers and shipping.
 */
@Serializable
public data class MailingAddress (
    /**
     * The first line of the address. This is usually the street address or a P.O.
     * Box number.
     */
    val address1: String? = null,

    /**
     * The second line of the address. This is usually an apartment, suite, or
     * unit number.
     */
    val address2: String? = null,

    /**
     * The name of the city, district, village, or town.
     */
    val city: String? = null,

    /**
     * The name of the country.
     */
    val country: String? = null,

    /**
     * The two-letter code that represents the country, for example, US.
     * The country codes generally follows ISO 3166-1 alpha-2 guidelines.
     */
    val countryCode: String? = null,

    /**
     * The customer’s first name.
     */
    val firstName: String? = null,

    /**
     * The customer’s last name.
     */
    val lastName: String? = null,

    /**
     * The phone number for this mailing address as entered by the customer.
     */
    val phone: String? = null,

    /**
     * The region of the address, such as the province, state, or district.
     */
    val province: String? = null,

    /**
     * The two-letter code for the region.
     * For example, ON.
     */
    val provinceCode: String? = null,

    /**
     * The ZIP or postal code of the address.
     */
    val zip: String? = null
)

/**
 * The information about the intent of the discount.
 */
@Serializable
public data class DiscountApplication (
    /**
     * The method by which the discount's value is applied to its entitled items.
     *
     * - `ACROSS`: The value is spread across all entitled lines.
     * - `EACH`: The value is applied onto every entitled line.
     */
    val allocationMethod: String? = null,

    /**
     * How the discount amount is distributed on the discounted lines.
     *
     * - `ALL`: The discount is allocated onto all the lines.
     * - `ENTITLED`: The discount is allocated onto only the lines that it's
     * entitled for.
     * - `EXPLICIT`: The discount is allocated onto explicitly chosen lines.
     */
    val targetSelection: String? = null,

    /**
     * The type of line (i.e. line item or shipping line) on an order that the
     * discount is applicable towards.
     *
     * - `LINE_ITEM`: The discount applies onto line items.
     * - `SHIPPING_LINE`: The discount applies onto shipping lines.
     */
    val targetType: String? = null,

    /**
     * The customer-facing name of the discount. If the type of discount is
     * a `DISCOUNT_CODE`, this `title` attribute represents the code of the
     * discount.
     */
    val title: String? = null,

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
    val type: String? = null,

    /**
     * The value of the discount. Fixed discounts return a `Money` Object, while
     * Percentage discounts return a `PricingPercentageValue` object.
     */
    val value: Value? = null
)

/**
 * The value of the discount. Fixed discounts return a `Money` Object, while
 * Percentage discounts return a `PricingPercentageValue` object.
 *
 * The total amount for the customer to pay.
 *
 * A monetary value with currency.
 *
 * The total cost of the merchandise line.
 *
 * The product variant’s price.
 *
 * The monetary value with currency allocated to the discount.
 *
 * Price of this shipping rate.
 *
 * The price at checkout before duties, shipping, and taxes.
 *
 * The sum of all the prices of all the items in the checkout, including
 * duties, taxes, and discounts.
 *
 * The sum of all the taxes applied to the line items and shipping lines in
 * the checkout.
 *
 * The monetary value with currency allocated to the transaction method.
 *
 * A value given to a customer when a discount is applied to an order. The
 * application of a discount with this value gives the customer the specified
 * percentage off a specified item.
 */
@Serializable
public data class Value (
    /**
     * The decimal money amount.
     */
    val amount: Double? = null,

    /**
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    val currencyCode: String? = null,

    /**
     * The percentage value of the object.
     */
    val percentage: Double? = null
)

/**
 * A single line item in the checkout, grouped by variant and attributes.
 */
@Serializable
public data class CheckoutLineItem (
    /**
     * The discounts that have been applied to the checkout line item by a
     * discount application.
     */
    val discountAllocations: List<DiscountAllocation>? = null,

    /**
     * A globally unique identifier.
     */
    val id: String? = null,

    /**
     * The quantity of the line item.
     */
    val quantity: Double? = null,

    /**
     * The title of the line item. Defaults to the product's title.
     */
    val title: String? = null,

    /**
     * Product variant of the line item.
     */
    val variant: ProductVariant? = null
)

/**
 * The discount that has been applied to the checkout line item.
 */
@Serializable
public data class DiscountAllocation (
    /**
     * The monetary value with currency allocated to the discount.
     */
    val amount: MoneyV2? = null,

    /**
     * The information about the intent of the discount.
     */
    val discountApplication: DiscountApplication? = null
)

/**
 * An order is a customer’s completed request to purchase one or more products
 * from a shop. An order is created when a customer completes the checkout
 * process.
 */
@Serializable
public data class Order (
    /**
     * The ID of the order.
     */
    val id: String? = null
)

/**
 * A shipping rate to be applied to a checkout.
 */
@Serializable
public data class ShippingRate (
    /**
     * Price of this shipping rate.
     */
    val price: MoneyV2? = null
)

/**
 * A transaction associated with a checkout or order.
 */
@Serializable
public data class Transaction (
    /**
     * The monetary value with currency allocated to the transaction method.
     */
    val amount: MoneyV2? = null,

    /**
     * The name of the payment provider used for the transaction.
     */
    val gateway: String? = null
)

/**
 * The `checkout_completed` event logs when a visitor completes a purchase. This
 * event is available on the order status and checkout pages
 *
 * The `checkout_completed` event logs when a visitor completes a purchase.
 * This event is available on the order status and checkout pages
 */
@Serializable
public data class CheckoutCompleted (
    val context: Context? = null,
    val data: CheckoutCompletedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CheckoutCompletedData (
    val checkout: Checkout? = null
)

/**
 * The `checkout_contact_info_submitted` event logs an instance where a customer
 * submits a checkout form. This event is only available in checkouts where
 * checkout extensibility for customizations is enabled
 *
 * The `checkout_contact_info_submitted` event logs an instance where a
 * customer submits a checkout form. This event is only available in checkouts
 * where checkout extensibility for customizations is enabled
 */
@Serializable
public data class CheckoutContactInfoSubmitted (
    val context: Context? = null,
    val data: CheckoutContactInfoSubmittedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CheckoutContactInfoSubmittedData (
    val checkout: Checkout? = null
)

/**
 * The `checkout_shipping_info_submitted` event logs an instance where the
 * customer chooses a shipping rate. This event is only available in checkouts
 * where checkout extensibility for customizations is enabled
 */
@Serializable
public data class CheckoutShippingInfoSubmitted (
    val context: Context? = null,
    val data: CheckoutShippingInfoSubmittedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CheckoutShippingInfoSubmittedData (
    val checkout: Checkout? = null
)

/**
 * The `checkout_started` event logs an instance of a customer starting the
 * checkout process. This event is available on the checkout page. For checkout
 * extensibility, this event is triggered every time a customer enters checkout.
 * For non-checkout extensible shops, this event is only triggered the first
 * time a customer enters checkout.
 *
 * The `checkout_started` event logs an instance of a customer starting
 * the checkout process. This event is available on the checkout page. For
 * checkout extensibility, this event is triggered every time a customer
 * enters checkout. For non-checkout extensible shops, this event is only
 * triggered the first time a customer enters checkout.
 */
@Serializable
public data class CheckoutStarted (
    val context: Context? = null,
    val data: CheckoutStartedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CheckoutStartedData (
    val checkout: Checkout? = null
)

/**
 * The `collection_viewed` event logs an instance where a customer visited a
 * product collection index page. This event is available on the online store
 * page
 */
@Serializable
public data class CollectionViewed (
    val context: Context? = null,
    val data: CollectionViewedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class CollectionViewedData (
    val collection: Collection? = null
)

/**
 * A collection is a group of products that a shop owner can create to organize
 * them or make their shops easier to browse.
 */
@Serializable
public data class Collection (
    /**
     * A globally unique identifier.
     */
    val id: String? = null,

    val productVariants: List<ProductVariant>? = null,

    /**
     * The collection’s name. Maximum length: 255 characters.
     */
    val title: String? = null
)

/**
 * The `page_viewed` event logs an instance where a customer visited a page.
 * This event is available on the online store, checkout, and order status pages
 *
 * The `page_viewed` event logs an instance where a customer visited a page.
 * This event is available on the online store, checkout, and order status
 * pages
 */
@Serializable
public data class PageViewed (
    val context: Context? = null,
    val data: PageViewedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

public typealias PageViewedData = JsonObject

/**
 * The `payment_info_submitted` event logs an instance of a customer submitting
 * their payment information. This event is available on the checkout page
 *
 * The `payment_info_submitted` event logs an instance of a customer
 * submitting their payment information. This event is available on the
 * checkout page
 */
@Serializable
public data class PaymentInfoSubmitted (
    val context: Context? = null,
    val data: PaymentInfoSubmittedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class PaymentInfoSubmittedData (
    val checkout: Checkout? = null
)

/**
 * The `product_added_to_cart` event logs an instance where a customer adds a
 * product to their cart. This event is available on the online store page
 */
@Serializable
public data class ProductAddedToCart (
    val context: Context? = null,
    val data: ProductAddedToCartData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class ProductAddedToCartData (
    val cartLine: CartLine? = null
)

/**
 * The `product_removed_from_cart` event logs an instance where a customer
 * removes a product from their cart. This event is available on the online
 * store page
 */
@Serializable
public data class ProductRemovedFromCart (
    val context: Context? = null,
    val data: ProductRemovedFromCartData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class ProductRemovedFromCartData (
    val cartLine: CartLine? = null
)

/**
 * The `product_variant_viewed` event logs an instance where a customer
 * interacts with the product page and views a different variant than the
 * initial `product_viewed` impression. This event is available on the Product
 * page
 */
@Serializable
public data class ProductVariantViewed (
    val context: Context? = null,
    val data: ProductVariantViewedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class ProductVariantViewedData (
    val productVariant: ProductVariant? = null
)

/**
 * The `product_viewed` event logs an instance where a customer visited a
 * product details page. This event is available on the product page
 */
@Serializable
public data class ProductViewed (
    val context: Context? = null,
    val data: ProductViewedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class ProductViewedData (
    val productVariant: ProductVariant? = null
)

/**
 * The `search_submitted` event logs an instance where a customer performed a
 * search on the storefront. This event is available on the online store page
 */
@Serializable
public data class SearchSubmitted (
    val context: Context? = null,
    val data: SearchSubmittedData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * The name of the customer event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

@Serializable
public data class SearchSubmittedData (
    val searchResult: SearchResult? = null
)

/**
 * An object that contains the metadata of when a search has been performed.
 */
@Serializable
public data class SearchResult (
    val productVariants: List<ProductVariant>? = null,

    /**
     * The search query that was executed
     */
    val query: String? = null
)

/**
 * This event represents any custom events emitted by partners or merchants via
 * the `publish` method
 */
@Serializable
public data class CustomEvent (
    val context: Context? = null,
    val customData: CustomData? = null,

    /**
     * The ID of the customer event
     */
    val id: String? = null,

    /**
     * Arbitrary name of the custom event
     */
    val name: String? = null,

    /**
     * The timestamp of when the customer event occurred, in [ISO
     * 8601](https://en.wikipedia.org/wiki/ISO_8601) format
     */
    val timestamp: String? = null
): AnalyticsEvent

/**
 * A free-form object representing data specific to a custom event provided by
 * the custom event publisher
 */
public typealias CustomData = JsonObject

/**
 * A free-form object representing data specific to this event provided by
 * Shopify. Refer to [standard events](#standard-events) for details on the
 * payload available to each event
 */
public typealias Data = JsonObject

@Serializable
public data class InitData (
    val cart: Cart? = null,
    val checkout: Checkout? = null,
    val customer: Customer? = null,
    val productVariants: List<ProductVariant>? = null
)

/**
 * A customer represents a customer account with the shop. Customer accounts
 * store contact information for the customer, saving logged-in customers the
 * trouble of having to provide it at every checkout.
 */
@Serializable
public data class Customer (
    /**
     * The customer’s email address.
     */
    val email: String? = null,

    /**
     * The customer’s first name.
     */
    val firstName: String? = null,

    /**
     * The ID of the customer.
     */
    val id: String? = null,

    /**
     * The customer’s last name.
     */
    val lastName: String? = null,

    /**
     * The total number of orders that the customer has placed.
     */
    val ordersCount: Double? = null,

    /**
     * The customer’s phone number.
     */
    val phone: String? = null
)

/**
 * A value given to a customer when a discount is applied to an order. The
 * application of a discount with this value gives the customer the specified
 * percentage off a specified item.
 */
@Serializable
public data class PricingPercentageValue (
    /**
     * The percentage value of the object.
     */
    val percentage: Double? = null
)
