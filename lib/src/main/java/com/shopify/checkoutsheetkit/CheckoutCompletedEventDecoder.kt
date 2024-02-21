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
package com.shopify.checkoutsheetkit

import com.shopify.checkoutsheetkit.pixelevents.MoneyV2
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class CheckoutCompletedEvent(
    public val orderDetails: OrderDetails? = null
)

@Serializable
public data class OrderDetails(
    public val id: String? = null,
    public val email: String? = null,
    public val phone: String? = null,
    public val cart: CartInfo? = null,
    public val paymentMethods: List<OrderPaymentMethod> = emptyList(),
    public val billingAddress: Address? = null,
    public val deliveries: List<DeliveryInfo> = emptyList(),
)

@Serializable
public data class CartInfo(
    public val token: String? = null,
    public val lines: List<CartLine> = emptyList(),
    public val price: PriceSet? = null,
)

@Serializable
public data class PriceSet(
    public val subtotal: MoneyV2? = null,
    public val total: MoneyV2? = null,
    public val taxes: MoneyV2? = null,
    public val discounts: List<Discount>? = null,
    public val shipping: MoneyV2? = null,
)

@Serializable
public data class Address(
    public val referenceId: String? = null,
    public val name: String? = null,
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val address1: String? = null,
    public val address2: String? = null,
    public val city: String? = null,
    public val countryCode: String? = null,
    public val zoneCode: String? = null,
    public val postalCode: String? = null,
    public val phone: String? = null,
)

@Serializable
public data class OrderPaymentMethod(
    public val type: String? = null,
    public val details: Map<String, String?>? = emptyMap(),
)

/**
 * Current possible methods:
 *  SHIPPING, PICK_UP, RETAIL, LOCAL, PICKUP_POINT, NONE
 */
@Serializable
public data class DeliveryInfo(
    public val method: String? = null,
    public val details: DeliveryDetails? = null,
)

@Serializable
public data class DeliveryDetails(
    public val name: String? = null,
    public val location: Address? = null,
    public val additionalInfo: String? = null,
)

@Serializable
public data class CartLine(
    public val title: String? = null,
    public val quantity: Int? = null,
    public val price: MoneyV2? = null,
    public val image: CartLineImage? = null,
    public val merchandiseId: String? = null,
    public val productId: String? = null,
    public val discounts: List<Discount>? = null,
)

@Serializable
public data class Discount(
    public val title: String? = null,
    public val amount: MoneyV2? = null,
    public val applicationType: String? = null,
    public val valueType: String? = null,
    public val value: Double? = null,
)

@Serializable
public data class CartLineImage(
    public val sm: String? = null,
    public val md: String? = null,
    public val lg: String? = null,
    public val altText: String? = null,
)

internal class CheckoutCompletedEventDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(decodedMsg: WebToSdkEvent): CheckoutCompletedEvent {
        return try {
            decoder.decodeFromString<CheckoutCompletedEvent>(decodedMsg.body)
        } catch (e: Exception) {
            log.e("CheckoutBridge", "Failed to decode CheckoutCompleted event", e)
            CheckoutCompletedEvent()
        }
    }
}
