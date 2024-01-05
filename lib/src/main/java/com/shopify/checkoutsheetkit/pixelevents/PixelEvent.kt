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
    @SerialName("dom") DOM("dom"),
    @SerialName("custom") CUSTOM("custom");

    public companion object {
        public fun fromTypeName(typeName: String?): EventType? {
            if (typeName == null) {
                return null
            }
            return EventType.values().firstOrNull {
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
     * The type of event, standard, custom or dom.
     * See https://shopify.dev/docs/api/web-pixels-api#customer-events-reference
     */
    public val type: EventType?
}

public enum class StandardPixelsEventType(public val eventName: String) {
    CART_VIEWED("cart_viewed"),
    CHECKOUT_ADDRESS_INFO_SUBMITTED("checkout_address_info_submitted"),
    CHECKOUT_COMPLETED("checkout_completed"),
    CHECKOUT_CONTACT_INFO_SUBMITTED("checkout_contact_info_submitted"),
    CHECKOUT_SHIPPING_INFO_SUBMITTED("checkout_shipping_info_submitted"),
    CHECKOUT_STARTED("checkout_started"),
    COLLECTION_VIEWED("collection_viewed"),
    PAGE_VIEWED("page_viewed"),
    PAYMENT_INFO_SUBMITTED("payment_info_submitted"),
    PRODUCT_ADDED_TO_CART("product_added_to_cart"),
    PRODUCT_REMOVED_FROM_CART("product_removed_from_cart"),
    PRODUCT_VIEWED("product_viewed"),
    SEARCH_SUBMITTED("search_submitted");

    public companion object {
        public fun fromEventName(eventName: String): StandardPixelsEventType? =
            StandardPixelsEventType.values().firstOrNull {
                it.eventName == eventName
            }
    }
}

public enum class DomPixelsEventType(public val eventName: String) {
    DOM_EVENT_CLICKED("clicked"),
    DOM_EVENT_FORM_SUBMITTED("form_submitted"),
    DOM_EVENT_INPUT_BLURRED("input_blurred"),
    DOM_EVENT_INPUT_CHANGED("input_changed"),
    DOM_EVENT_INPUT_FOCUSED("input_focused");
    public companion object {
        public fun fromEventName(eventName: String): DomPixelsEventType? =
            DomPixelsEventType.values().firstOrNull {
                it.eventName == eventName
            }
    }
}

/**
 * The `cart_viewed` event logs an instance where a customer visited the cart
 * page
 */
@Serializable
public data class CartViewedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CartViewedData? = null,
): PixelEvent

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

@Serializable
public data class CartViewedData(
    public val cart: Cart? = null
)

/**
 * A cart represents the merchandise that a customer intends to purchase, and
 * the estimated cost associated with the cart.
 */
@Serializable
public data class Cart(
    /**
     * The estimated costs that the customer will pay at checkout.
     */
    public val cost: CartCost? = null,

    /**
     * A globally unique identifier.
     */
    public val id: String? = null,

    public val lines: List<CartLine>? = null,

    /**
     * The total number of items in the cart.
     */
    public val totalQuantity: Double? = null
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
public data class CartCost(
    /**
     * The total amount for the customer to pay.
     */
    public val totalAmount: MoneyV2? = null
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
 * Information about the merchandise in the cart.
 */
@Serializable
public data class CartLine(
    /**
     * The cost of the merchandise that the customer will pay for at checkout. The
     * costs are subject to change and changes will be reflected at checkout.
     */
    public val cost: CartLineCost? = null,

    /**
     * The merchandise that the buyer intends to purchase.
     */
    public val merchandise: ProductVariant? = null,

    /**
     * The quantity of the merchandise that the customer intends to purchase.
     */
    public val quantity: Double? = null
)

/**
 * The cost of the merchandise that the customer will pay for at checkout. The
 * costs are subject to change and changes will be reflected at checkout.
 *
 * The cost of the merchandise line that the customer will pay at checkout.
 */
@Serializable
public data class CartLineCost(
    /**
     * The total cost of the merchandise line.
     */
    public val totalAmount: MoneyV2? = null
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
 * The `checkout_address_info_submitted` event logs an instance of a customer
 * submitting their mailing address. This event is only available in checkouts
 * where checkout extensibility for customizations is enabled
 */
@Serializable
public data class CheckoutAddressInfoSubmittedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutAddressInfoSubmittedData? = null,
): PixelEvent

@Serializable
public data class CheckoutAddressInfoSubmittedData(
    public val checkout: Checkout? = null
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
     * The three-letter code that represents the currency, for example, USD.
     * Supported codes include standard ISO 4217 codes, legacy codes, and non-
     * standard codes.
     */
    public val currencyCode: String? = null,

    /**
     * A list of discount applications.
     */
    public val discountApplications: List<DiscountApplication>? = null,

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
     * A globally unique identifier.
     */
    public val id: String? = null,

    /**
     * The quantity of the line item.
     */
    public val quantity: Double? = null,

    /**
     * The title of the line item. Defaults to the product's title.
     */
    public val title: String? = null,

    /**
     * Product variant of the line item.
     */
    public val variant: ProductVariant? = null
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
     * The ID of the order.
     */
    public val id: String? = null
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
    public val gateway: String? = null
)

/**
 * The `checkout_completed` event logs when a visitor completes a purchase. This
 * event is available on the order status and checkout pages
 *
 * The `checkout_completed` event logs when a visitor completes a purchase.
 * This event is available on the order status and checkout pages
 */
@Serializable
public data class CheckoutCompletedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutCompletedData? = null,
): PixelEvent

@Serializable
public data class CheckoutCompletedData(
    public val checkout: Checkout? = null
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
public data class CheckoutContactInfoSubmittedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutContactInfoSubmittedData? = null,
): PixelEvent

@Serializable
public data class CheckoutContactInfoSubmittedData(
    public val checkout: Checkout? = null
)

/**
 * The `checkout_shipping_info_submitted` event logs an instance where the
 * customer chooses a shipping rate. This event is only available in checkouts
 * where checkout extensibility for customizations is enabled
 */
@Serializable
public data class CheckoutShippingInfoSubmittedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutShippingInfoSubmittedData? = null,
): PixelEvent

@Serializable
public data class CheckoutShippingInfoSubmittedData(
    public val checkout: Checkout? = null
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
public data class CheckoutStartedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CheckoutStartedData? = null,
): PixelEvent

@Serializable
public data class CheckoutStartedData(
    public val checkout: Checkout? = null
)

/**
 * The `collection_viewed` event logs an instance where a customer visited a
 * product collection index page. This event is available on the online store
 * page
 */
@Serializable
public data class CollectionViewedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: CollectionViewedData? = null,
): PixelEvent

@Serializable
public data class CollectionViewedData(
    public val collection: Collection? = null
)

/**
 * A collection is a group of products that a shop owner can create to organize
 * them or make their shops easier to browse.
 */
@Serializable
public data class Collection(
    /**
     * A globally unique identifier.
     */
    public val id: String? = null,

    public val productVariants: List<ProductVariant>? = null,

    /**
     * The collection’s name. Maximum length: 255 characters.
     */
    public val title: String? = null
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
public data class PageViewedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: PageViewedData? = null,
): PixelEvent

// The page viewed can be obtained via the context attribute
public typealias PageViewedData = Unit

/**
 * The `payment_info_submitted` event logs an instance of a customer submitting
 * their payment information. This event is available on the checkout page
 *
 * The `payment_info_submitted` event logs an instance of a customer
 * submitting their payment information. This event is available on the
 * checkout page
 */
@Serializable
public data class PaymentInfoSubmittedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: PaymentInfoSubmittedData? = null,
): PixelEvent

@Serializable
public data class PaymentInfoSubmittedData(
    public val checkout: Checkout? = null
)

/**
 * The `product_added_to_cart` event logs an instance where a customer adds a
 * product to their cart. This event is available on the online store page
 */
@Serializable
public data class ProductAddedToCartEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: ProductAddedToCartData? = null,
): PixelEvent

@Serializable
public data class ProductAddedToCartData(
    public val cartLine: CartLine? = null
)

/**
 * The `product_removed_from_cart` event logs an instance where a customer
 * removes a product from their cart. This event is available on the online
 * store page
 */
@Serializable
public data class ProductRemovedFromCartEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: ProductRemovedFromCartData? = null,
): PixelEvent

@Serializable
public data class ProductRemovedFromCartData(
    public val cartLine: CartLine? = null
)

/**
 * The `product_variant_viewed` event logs an instance where a customer
 * interacts with the product page and views a different variant than the
 * initial `product_viewed` impression. This event is available on the Product
 * page
 */
@Serializable
public data class ProductVariantViewed(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: ProductVariantViewedData? = null,
): PixelEvent

@Serializable
public data class ProductVariantViewedData(
    public val productVariant: ProductVariant? = null
)

/**
 * The `product_viewed` event logs an instance where a customer visited a
 * product details page. This event is available on the product page
 */
@Serializable
public data class ProductViewedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: ProductViewedData? = null,
): PixelEvent

@Serializable
public data class ProductViewedData(
    public val productVariant: ProductVariant? = null
)

/**
 * The `search_submitted` event logs an instance where a customer performed a
 * search on the storefront. This event is available on the online store page
 */
@Serializable
public data class SearchSubmittedEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val context: Context? = null,
    public val data: SearchSubmittedData? = null,
): PixelEvent

@Serializable
public data class SearchSubmittedData(
    public val searchResult: SearchResult? = null
)

/**
 * An object that contains the metadata of when a search has been performed.
 */
@Serializable
public data class SearchResult(
    public val productVariants: List<ProductVariant>? = null,

    /**
     * The search query that was executed
     */
    public val query: String? = null
)

/**
 * This event represents any custom events emitted by partners or merchants via
 * the `publish` method
 */
@Serializable
public data class CustomEvent(
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

@Serializable
public data class ClickedDomEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val data: DomEventsClickedData? = null
): PixelEvent

@Serializable
public data class DomEventsClickedData(
    public val element: GenericElement? = null,
)

@Serializable
public data class FormSubmittedDomEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val data: DomEventsFormSubmittedData? = null
): PixelEvent

@Serializable
public data class DomEventsFormSubmittedData(
    public val element: FormElement? = null,
)

@Serializable
public data class InputBlurredDomEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val data: DomEventsInputBlurredData? = null
): PixelEvent

@Serializable
public data class DomEventsInputBlurredData(
    public val element: InputElement? = null
)

public data class InputChangedDomEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val data: DomEventsInputChangedData? = null
): PixelEvent

public data class DomEventsInputChangedData(
    public val element: InputElement? = null,
)

public data class InputFocusedDomEvent(
    public override val id: String? = null,
    public override val name: String? = null,
    public override val timestamp: String? = null,
    public override val type: EventType? = null,
    public val data: DomEventsInputFocusedData? = null
): PixelEvent

public data class DomEventsInputFocusedData(
    public val element: InputElement? = null,
)

@Serializable
public data class GenericElement(
    public val href: String? = null,
    public val id: String? = null,
    public val name: String? = null,
    public val tagName: String? = null,
    public val type: String? = null,
    public val value: String? = null
)

@Serializable
public data class FormElement(
    public val action: String? = null,
    public val elements: List<InputElement>? = null,
    public val id: String? = null,
)

@Serializable
public data class InputElement(
    public val id: String? = null,
    public val name: String? = null,
    public val tagName: String? = null,
    public val type: String? = null,
    public val value: String? = null,
)
