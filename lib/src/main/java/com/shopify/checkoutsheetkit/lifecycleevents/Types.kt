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
package com.shopify.checkoutsheetkit.lifecycleevents

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.jsonObject

/**
 * Common data types shared between lifecycle events
 */

@Serializable
public data class OrderConfirmation(
    public val url: String? = null,
    public val order: Order,
    public val number: String? = null,
    public val isFirstOrder: Boolean
) {
    @Serializable
    public data class Order(
        public val id: String
    )
}

@Serializable
public data class Cart(
    public val id: String,
    public val lines: List<CartLine> = emptyList(),
    public val cost: CartCost,
    public val buyerIdentity: CartBuyerIdentity,
    public val deliveryGroups: List<CartDeliveryGroup> = emptyList(),
    public val discountCodes: List<CartDiscountCode> = emptyList(),
    public val appliedGiftCards: List<AppliedGiftCard> = emptyList(),
    public val discountAllocations: List<CartDiscountAllocation> = emptyList(),
    public val delivery: CartDelivery,
    public val payment: CartPayment? = null
)

@Serializable
public data class CartLine(
    public val id: String,
    public val quantity: Int,
    public val merchandise: CartLineMerchandise,
    public val cost: CartLineCost,
    public val discountAllocations: List<CartDiscountAllocation> = emptyList()
)

@Serializable
public data class CartLineCost(
    public val amountPerQuantity: Money,
    public val subtotalAmount: Money,
    public val totalAmount: Money
)

@Serializable
public data class CartLineMerchandise(
    public val id: String,
    public val title: String,
    public val product: Product,
    public val image: MerchandiseImage? = null,
    public val selectedOptions: List<SelectedOption> = emptyList()
) {
    @Serializable
    public data class Product(
        public val id: String,
        public val title: String
    )
}

@Serializable
public data class MerchandiseImage(
    public val url: String,
    public val altText: String? = null
)

@Serializable
public data class SelectedOption(
    public val name: String,
    public val value: String
)

@Serializable
public data class CartDiscountAllocation(
    public val discountedAmount: Money,
    public val discountApplication: DiscountApplication,
    public val targetType: DiscountApplicationTargetType
)

@Serializable
public data class DiscountApplication(
    public val allocationMethod: AllocationMethod,
    public val targetSelection: TargetSelection,
    public val targetType: DiscountApplicationTargetType,
    public val value: DiscountValue
) {
    @Serializable
    public enum class AllocationMethod {
        @SerialName("ACROSS")
        ACROSS,

        @SerialName("EACH")
        EACH
    }

    @Serializable
    public enum class TargetSelection {
        @SerialName("ALL")
        ALL,

        @SerialName("ENTITLED")
        ENTITLED,

        @SerialName("EXPLICIT")
        EXPLICIT
    }
}

@Serializable
public enum class DiscountApplicationTargetType {
    @SerialName("LINE_ITEM")
    LINE_ITEM,

    @SerialName("SHIPPING_LINE")
    SHIPPING_LINE
}

@Serializable
public data class CartCost(
    public val subtotalAmount: Money,
    public val totalAmount: Money
)

@Serializable
public data class CartBuyerIdentity(
    public val email: String? = null,
    public val phone: String? = null,
    public val customer: Customer? = null,
    public val countryCode: String? = null
)

@Serializable
public data class Customer(
    public val id: String,
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val email: String? = null,
    public val phone: String? = null
)

@Serializable
public data class CartDeliveryGroup(
    public val deliveryAddress: MailingAddress,
    public val deliveryOptions: List<CartDeliveryOption> = emptyList(),
    public val selectedDeliveryOption: CartDeliveryOption? = null,
    public val groupType: CartDeliveryGroupType
)

@Serializable
public data class MailingAddress(
    public val address1: String? = null,
    public val address2: String? = null,
    public val city: String? = null,
    public val province: String? = null,
    public val country: String? = null,
    public val countryCodeV2: String? = null,
    public val zip: String? = null,
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val phone: String? = null,
    public val company: String? = null
)

@Serializable
public data class CartDeliveryOption(
    public val code: String? = null,
    public val title: String? = null,
    public val description: String? = null,
    public val handle: String,
    public val estimatedCost: Money,
    public val deliveryMethodType: CartDeliveryMethodType
)

@Serializable
public enum class CartDeliveryMethodType {
    @SerialName("SHIPPING")
    SHIPPING,

    @SerialName("PICKUP")
    PICKUP,

    @SerialName("PICKUP_POINT")
    PICKUP_POINT,

    @SerialName("LOCAL")
    LOCAL,

    @SerialName("NONE")
    NONE
}

@Serializable
public enum class CartDeliveryGroupType {
    @SerialName("SUBSCRIPTION")
    SUBSCRIPTION,

    @SerialName("ONE_TIME_PURCHASE")
    ONE_TIME_PURCHASE
}

@Serializable
public data class CartDelivery(
    public val addresses: List<CartSelectableAddress> = emptyList()
)

@Serializable
public data class CartPayment(
    public val methods: List<CartPaymentMethod> = emptyList()
)

@Serializable
public data class CartPaymentMethod(
    public val instruments: List<CartPaymentInstrument> = emptyList()
)

/**
 * Represents a delivery address union type from the Storefront API
 * https://shopify.dev/docs/api/storefront/latest/unions/CartAddress
 */
@Serializable(with = CartAddressSerializer::class)
public sealed interface CartAddress {
    @Serializable
    public data class DeliveryAddress(
        public val address1: String? = null,
        public val address2: String? = null,
        public val city: String? = null,
        public val company: String? = null,
        public val countryCode: String? = null,
        public val firstName: String? = null,
        public val lastName: String? = null,
        public val phone: String? = null,
        public val provinceCode: String? = null,
        public val zip: String? = null
    ) : CartAddress
}

private object CartAddressSerializer : KSerializer<CartAddress> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CartAddress")

    override fun deserialize(decoder: Decoder): CartAddress {
        // Protocol is versioned - for current version, always DeliveryAddress
        return CartAddress.DeliveryAddress.serializer().deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: CartAddress) {
        when (value) {
            is CartAddress.DeliveryAddress -> {
                CartAddress.DeliveryAddress.serializer().serialize(encoder, value)
            }
        }
    }
}

@Serializable
public data class CartSelectableAddress(
    public val address: CartAddress
)

/**
 * Type alias for CartAddress.DeliveryAddress for convenience.
 * Use this when you need to construct or work with delivery addresses directly.
 * Can be removed when we introduce CartInput
 */
public typealias CartDeliveryAddress = CartAddress.DeliveryAddress

@Serializable
public data class CartDiscountCode(
    public val code: String,
    public val applicable: Boolean
)

@Serializable
public data class AppliedGiftCard(
    public val amountUsed: Money,
    public val balance: Money,
    public val lastCharacters: String,
    public val presentmentAmountUsed: Money
)

@Serializable
public data class Money(
    public val amount: String,
    public val currencyCode: String
) {
    init {
        require(amount.toBigDecimalOrNull() != null) {
            "Invalid money amount: '$amount' (must be a valid decimal number)"
        }
        require(currencyCode.isNotBlank()) {
            "Currency code cannot be blank"
        }
    }
}

@Serializable
public data class PricingPercentageValue(
    public val percentage: Double
)

@Serializable(with = DiscountValueSerializer::class)
public sealed interface DiscountValue {
    public data class MoneyValue(val money: Money) : DiscountValue
    public data class PercentageValue(val percentage: PricingPercentageValue) : DiscountValue
}

private object DiscountValueSerializer : KSerializer<DiscountValue> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DiscountValue")

    override fun deserialize(decoder: Decoder): DiscountValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("DiscountValueSerializer only supports JSON decoding")
        val element = jsonDecoder.decodeJsonElement()

        val jsonObject = runCatching { element.jsonObject }.getOrElse {
            throw SerializationException("DiscountValue must be a JSON object, but was: ${element::class.simpleName}")
        }

        return when {
            jsonObject.containsKey("amount") && jsonObject.containsKey("currencyCode") -> {
                DiscountValue.MoneyValue(
                    jsonDecoder.json.decodeFromJsonElement(Money.serializer(), element)
                )
            }
            jsonObject.containsKey("percentage") -> {
                DiscountValue.PercentageValue(
                    jsonDecoder.json.decodeFromJsonElement(PricingPercentageValue.serializer(), element)
                )
            }
            else -> throw SerializationException(
                "Unable to decode DiscountValue: missing 'amount'/'currencyCode' or 'percentage' field"
            )
        }
    }

    override fun serialize(encoder: Encoder, value: DiscountValue) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("DiscountValueSerializer only supports JSON encoding")

        when (value) {
            is DiscountValue.MoneyValue -> jsonEncoder.encodeSerializableValue(
                Money.serializer(),
                value.money
            )
            is DiscountValue.PercentageValue -> jsonEncoder.encodeSerializableValue(
                PricingPercentageValue.serializer(),
                value.percentage
            )
        }
    }
}

/**
 * Response payload for address change start events.
 * Contains either cart input data or error information.
 */
@Serializable
public data class CheckoutAddressChangeStartResponsePayload(
    val cart: Cart? = null,
    val errors: List<ResponseError>? = null,
)

/**
 * Response payload for submit start events.
 * Contains cart updates or error information.
 */
@Serializable
public data class CheckoutSubmitStartResponsePayload(
    val cart: Cart? = null,
    val errors: List<ResponseError>? = null,
)

/**
 * Application-level error in cart response payload
 */
@Serializable
public data class ResponseError(
    val code: String,
    val message: String,
    val fieldTarget: String? = null,
)

/**
 * Cart input types for updating cart state from embedder responses.
 *
 * Mirrors the [Storefront API CartInput](https://shopify.dev/docs/api/storefront/latest/input-objects/CartInput).
 *
 * @deprecated Use Cart instead for response payloads.
 */
@Deprecated("Use Cart instead for response payloads")
@Serializable
public data class CartInput(
    /** The delivery-related fields for the cart. */
    val delivery: CartDeliveryInput? = null,

    /** The customer associated with the cart. */
    val buyerIdentity: CartBuyerIdentityInput? = null,

    /** The case-insensitive discount codes that the customer added at checkout. */
    val discountCodes: List<String>? = null,

    /** Payment instruments for the cart. */
    val paymentInstruments: List<CartPaymentInstrumentInput>? = null,
)

/**
 * Delivery-related fields for the cart.
 */
@Serializable
public data class CartDeliveryInput(
    /** Selectable addresses presented to the buyer. */
    val addresses: List<CartSelectableAddressInput>? = null,
)

/**
 * A selectable delivery address with optional selection and reuse settings.
 */
@Serializable
public data class CartSelectableAddressInput(
    /** Exactly one kind of delivery address. */
    val address: CartDeliveryAddressInput,

    /** Whether this address is selected as the active delivery address. */
    val selected: Boolean? = null,
)

/**
 * A delivery address for a cart.
 *
 * Based on [Storefront API MailingAddressInput](https://shopify.dev/docs/api/storefront/latest/input-objects/MailingAddressInput).
 */
@Serializable
public data class CartDeliveryAddressInput(
    /** The first line of the address. Typically the street address or PO Box number. */
    val address1: String? = null,

    /** The second line of the address. Typically the number of the apartment, suite, or unit. */
    val address2: String? = null,

    /** The name of the city, district, village, or town. */
    val city: String? = null,

    /** The name of the customer's company or organization. */
    val company: String? = null,

    /** The two-letter country code (ISO 3166-1 alpha-2 format, e.g., "US", "CA"). */
    val countryCode: String? = null,

    /** The first name of the customer. */
    val firstName: String? = null,

    /** The last name of the customer. */
    val lastName: String? = null,

    /** The phone number for the address. Formatted using E.164 standard (e.g., +16135551111). */
    val phone: String? = null,

    /** The code for the region of the address, such as the province or state (e.g., "ON" for Ontario, or "CA" for California). */
    val provinceCode: String? = null,

    /** The zip or postal code of the address. */
    val zip: String? = null,
)

/**
 * The customer associated with the cart.
 *
 * Based on [Storefront API CartBuyerIdentityInput](https://shopify.dev/docs/api/storefront/latest/input-objects/CartBuyerIdentityInput).
 */
@Serializable
public data class CartBuyerIdentityInput(
    /** The email address of the buyer that is interacting with the cart / checkout. */
    val email: String? = null,

    /** The phone number of the buyer that is interacting with the cart / checkout. */
    val phone: String? = null,

    /** The country where the buyer is located. Two-letter country code (ISO 3166-1 alpha-2, e.g. US, GB, CA). */
    val countryCode: String? = null,
)

/**
 * Payment token input structure for checkout submission.
 */
@Serializable
public data class PaymentTokenInput(
    val token: String,
    val tokenType: String,
    val tokenProvider: String,
)

@Serializable
public enum class CardBrand {
    @SerialName("VISA")
    VISA,

    @SerialName("MASTERCARD")
    MASTERCARD,

    @SerialName("AMERICAN_EXPRESS")
    AMERICAN_EXPRESS,

    @SerialName("DISCOVER")
    DISCOVER,

    @SerialName("DINERS_CLUB")
    DINERS_CLUB,

    @SerialName("JCB")
    JCB,

    @SerialName("MAESTRO")
    MAESTRO,

    @SerialName("UNKNOWN")
    UNKNOWN
}

@Serializable
public data class CartPaymentInstrument(
    public val externalReferenceId: String,
    public val credentials: List<CartCredential>? = null
)

@Serializable
public data class CartCredential(
    val remoteTokenPaymentCredential: RemoteTokenPaymentCredential? = null
)

@Serializable
public data class RemoteTokenPaymentCredential(
    val token: String,
    val tokenType: String,
    val tokenHandler: String
)


/**
 * Type alias for CartDeliveryAddressInput used in payment instrument billing addresses.
 * This doesn't follow the Storefront API design so we are aliasing to an existing conforming shape.
 */
public typealias CartMailingAddressInput = CartDeliveryAddressInput

@Serializable
public data class CartPaymentInstrumentInput(
    public val externalReferenceId: String,
    public val lastDigits: String? = null,
    public val brand: CardBrand? = null,
    public val cardHolderName: String? = null,
    public val month: Int? = null,
    public val year: Int? = null,
    public val billingAddress: CartMailingAddressInput? = null,
)
