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

import com.shopify.checkoutsheetkit.pixelevents.MoneyV2
import kotlinx.serialization.Serializable

@Serializable
public data class OrderDetails(
    public val id: String,
    public val email: String? = null,
    public val phone: String? = null,
    public val cart: CartInfo,
    public val billingAddress: Address? = null,
    public val paymentMethods: List<PaymentMethod> = emptyList(),
    public val deliveries: List<DeliveryInfo> = emptyList(),
)

@Serializable
public data class CartInfo(
    public val token: String,
    public val lines: List<CartLine>,
    public val price: Price,
)

@Serializable
public data class Price(
    public val total: MoneyV2? = null,
    public val subtotal: MoneyV2? = null,
    public val taxes: MoneyV2? = null,
    public val shipping: MoneyV2? = null,
    public val discounts: List<Discount>? = emptyList(),
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
public data class PaymentMethod(
    public val type: String,
    public val details: Map<String, String>? = emptyMap(),
)

/**
 * Current possible methods:
 *  SHIPPING, PICK_UP, RETAIL, LOCAL, PICKUP_POINT, NONE
 */
@Serializable
public data class DeliveryInfo(
    public val method: String,
    public val details: DeliveryDetails,
)

@Serializable
public data class DeliveryDetails(
    public val name: String? = null,
    public val location: Address? = null,
    public val additionalInfo: String? = null,
)

@Serializable
public data class CartLine(
    public val merchandiseId: String? = null,
    public val productId: String? = null,
    public val image: CartLineImage? = null,
    public val quantity: Int,
    public val title: String,
    public val price: MoneyV2,
    public val discounts: List<Discount>? = emptyList(),
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
    public val sm: String,
    public val md: String,
    public val lg: String,
    public val altText: String? = null,
)
