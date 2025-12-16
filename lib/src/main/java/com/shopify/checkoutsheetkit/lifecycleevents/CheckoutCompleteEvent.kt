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

import com.shopify.checkoutsheetkit.CheckoutNotification
import com.shopify.checkoutsheetkit.rpc.RPCNotificationDecoder
import kotlinx.serialization.Serializable

/**
 * Event triggered when checkout is successfully completed.
 * Contains order confirmation details and final cart state.
 */
@Serializable
public data class CheckoutCompleteEvent(
    /**
     * Order confirmation details including order ID, number, and status.
     */
    public val orderConfirmation: OrderConfirmation,
    /**
     * The final cart state at the time of checkout completion.
     */
    public val cart: Cart
) : CheckoutNotification {
    override val method: String = "checkout.complete"

    internal companion object {
        internal val decoder = RPCNotificationDecoder.create<CheckoutCompleteEvent>("checkout.complete")
    }
}

internal fun emptyCompleteEvent(id: String? = null): CheckoutCompleteEvent {
    return CheckoutCompleteEvent(
        orderConfirmation = OrderConfirmation(
            url = null,
            order = OrderConfirmation.Order(id = id ?: ""),
            number = null,
            isFirstOrder = false
        ),
        cart = Cart(
            id = "",
            lines = emptyList(),
            cost = CartCost(
                subtotalAmount = Money(amount = "0.00", currencyCode = "USD"),
                totalAmount = Money(amount = "0.00", currencyCode = "USD")
            ),
            buyerIdentity = CartBuyerIdentity(
                email = null,
                phone = null,
                customer = null,
                countryCode = null
            ),
            deliveryGroups = emptyList(),
            discountCodes = emptyList(),
            appliedGiftCards = emptyList(),
            discountAllocations = emptyList(),
            delivery = CartDelivery(addresses = emptyList()),
            payment = CartPayment(methods = emptyList())
        )
    )
}
