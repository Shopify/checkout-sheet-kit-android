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

import kotlinx.serialization.Serializable

/**
 * Error codes emitted by checkout.error events.
 * These errors originate from the web checkout experience and indicate various failure scenarios.
 */
@Serializable
public enum class CheckoutErrorCode {
    /**
     * The authentication payload passed could not be decoded.
     */
    INVALID_PAYLOAD,

    /**
     * The JWT signature or encrypted token signature was invalid.
     */
    INVALID_SIGNATURE,

    /**
     * The access token was not valid for the shop.
     */
    NOT_AUTHORIZED,

    /**
     * The provided authentication payload has expired.
     */
    PAYLOAD_EXPIRED,

    /**
     * The buyer must be logged in to a customer account to proceed with checkout.
     */
    CUSTOMER_ACCOUNT_REQUIRED,

    /**
     * The storefront requires a password to access checkout.
     */
    STOREFRONT_PASSWORD_REQUIRED,

    /**
     * The cart associated with the checkout has already been completed.
     */
    CART_COMPLETED,

    /**
     * The cart is invalid or no longer exists.
     */
    INVALID_CART,

    /**
     * Checkout preloading has been temporarily disabled via killswitch.
     */
    KILLSWITCH_ENABLED,

    /**
     * An unrecoverable error occurred during checkout.
     */
    UNRECOVERABLE_FAILURE,

    /**
     * A policy violation was detected during checkout.
     */
    POLICY_VIOLATION,

    /**
     * An error occurred processing a vaulted payment method.
     */
    VAULTED_PAYMENT_ERROR
}
