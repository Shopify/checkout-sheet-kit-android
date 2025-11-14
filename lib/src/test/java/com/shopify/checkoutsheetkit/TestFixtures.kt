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

import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.lifecycleevents.CartBuyerIdentity
import com.shopify.checkoutsheetkit.lifecycleevents.CartCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.lifecycleevents.OrderConfirmation

/**
 * Test fixtures for creating test data with sensible defaults.
 * Customize only the fields you need for your test.
 */

/**
 * Creates a test Cart instance with sensible defaults.
 *
 * Example:
 * ```
 * val cart = createTestCart(
 *     id = "custom-cart-id",
 *     totalAmount = "99.99"
 * )
 * ```
 */
internal fun createTestCart(
    id: String = "cart-id-123",
    subtotalAmount: String = "10.00",
    totalAmount: String = "10.00",
    currencyCode: String = "USD",
): Cart = Cart(
    id = id,
    lines = emptyList(),
    cost = CartCost(
        subtotalAmount = Money(amount = subtotalAmount, currencyCode = currencyCode),
        totalAmount = Money(amount = totalAmount, currencyCode = currencyCode)
    ),
    buyerIdentity = CartBuyerIdentity(),
    deliveryGroups = emptyList(),
    discountCodes = emptyList(),
    appliedGiftCards = emptyList(),
    discountAllocations = emptyList(),
    delivery = CartDelivery(addresses = emptyList())
)

/**
 * Creates a test OrderConfirmation instance with sensible defaults.
 *
 * Example:
 * ```
 * val confirmation = createTestOrderConfirmation(orderId = "order-123")
 * ```
 */
internal fun createTestOrderConfirmation(
    orderId: String = "order-id-123",
    orderNumber: String? = null,
    isFirstOrder: Boolean = false,
    url: String? = null
): OrderConfirmation = OrderConfirmation(
    url = url,
    order = OrderConfirmation.Order(id = orderId),
    number = orderNumber,
    isFirstOrder = isFirstOrder
)

// EVENTS

/**
 * Creates a test CheckoutStartEvent instance with sensible defaults.
 *
 * Example:
 * ```
 * val event = createTestCheckoutStartEvent(
 *     cart = createTestCart(totalAmount = "50.00")
 * )
 * ```
 */
internal fun createTestCheckoutStartEvent(
    cart: Cart = createTestCart()
): CheckoutStartEvent = CheckoutStartEvent(cart = cart)

/**
 * Creates a test CheckoutCompleteEvent instance with sensible defaults.
 *
 * Example:
 * ```
 * val event = createTestCheckoutCompleteEvent(
 *     orderConfirmation = createTestOrderConfirmation(orderId = "order-456"),
 *     cart = createTestCart(totalAmount = "100.00")
 * )
 * ```
 */
internal fun createTestCheckoutCompleteEvent(
    cart: Cart = createTestCart(),
    orderConfirmation: OrderConfirmation = createTestOrderConfirmation(),
): CheckoutCompleteEvent = CheckoutCompleteEvent(
    cart = cart,
    orderConfirmation = orderConfirmation,
)
