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

@Serializable
public data class CheckoutCompleteEvent(
    public val orderConfirmation: OrderConfirmation,
    public val cart: Cart
) {
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
        public val delivery: CartDelivery
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
        public val id: String? = null,
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
    public data class CartSelectableAddress(
        public val address: CartDeliveryAddress
    )

    @Serializable
    public data class CartDeliveryAddress(
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
    )

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
    )

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

            return runCatching {
                jsonDecoder.json.decodeFromJsonElement(Money.serializer(), element)
            }.map {
                DiscountValue.MoneyValue(it)
            }.getOrElse {
                runCatching {
                    jsonDecoder.json.decodeFromJsonElement(PricingPercentageValue.serializer(), element)
                }.map {
                    DiscountValue.PercentageValue(it)
                }.getOrElse {
                    throw SerializationException("Unable to decode DiscountValue", it)
                }
            }
        }

        override fun serialize(encoder: Encoder, value: DiscountValue) {
            val jsonEncoder = encoder as? JsonEncoder
                ?: error("DiscountValueSerializer only supports JSON encoding")

            when (value) {
                is DiscountValue.MoneyValue -> jsonEncoder.encodeSerializableValue(Money.serializer(), value.money)
                is DiscountValue.PercentageValue -> jsonEncoder.encodeSerializableValue(PricingPercentageValue.serializer(), value.percentage)
            }
        }
    }
}

internal fun emptyCompletedEvent(id: String? = null): CheckoutCompleteEvent {
    return CheckoutCompleteEvent(
        orderConfirmation = CheckoutCompleteEvent.OrderConfirmation(
            url = null,
            order = CheckoutCompleteEvent.OrderConfirmation.Order(id = id ?: ""),
            number = null,
            isFirstOrder = false
        ),
        cart = CheckoutCompleteEvent.Cart(
            id = "",
            lines = emptyList(),
            cost = CheckoutCompleteEvent.CartCost(
                subtotalAmount = CheckoutCompleteEvent.Money(amount = "", currencyCode = ""),
                totalAmount = CheckoutCompleteEvent.Money(amount = "", currencyCode = "")
            ),
            buyerIdentity = CheckoutCompleteEvent.CartBuyerIdentity(
                email = null,
                phone = null,
                customer = null,
                countryCode = null
            ),
            deliveryGroups = emptyList(),
            discountCodes = emptyList(),
            appliedGiftCards = emptyList(),
            discountAllocations = emptyList(),
            delivery = CheckoutCompleteEvent.CartDelivery(addresses = emptyList())
        )
    )
}
