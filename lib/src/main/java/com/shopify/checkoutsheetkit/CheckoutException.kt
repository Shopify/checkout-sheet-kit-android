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

import kotlinx.serialization.Serializable

/**
 * Superclass for the Shopify Checkout Sheet Kit exceptions
 */
@Serializable
public abstract class CheckoutException(
    public val errorDescription: String,
    public val errorCode: String,
    public val isRecoverable: Boolean
) : Exception(errorDescription)

/**
 * Issued when an internal error occurs within Shopify Checkout Sheet Kit. If the issue persists, it is recommended to open a bug report
 * in https://github.com/Shopify/checkout-sheet-kit-android
 */
public class CheckoutSheetKitException(
    errorDescription: String,
    errorCode: String = UNKNOWN,
    isRecoverable: Boolean,
) : CheckoutException(errorDescription, errorCode, isRecoverable) {
        public companion object {
            public const val ERROR_SENDING_MESSAGE_TO_CHECKOUT: String = "error_sending_message"
            public const val ERROR_RECEIVING_MESSAGE_FROM_CHECKOUT: String = "error_receiving_message"
            public const val RENDER_PROCESS_GONE: String = "render_process_gone"
            public const val UNKNOWN: String = "unknown"
        }
}

/**
 * Issued when checkout has encountered a unrecoverable error (for example server side error).
 * if the issue persists, it is recommended to open a bug report in https://github.com/Shopify/checkout-sheet-kit-android
 */
public open class CheckoutUnavailableException @JvmOverloads constructor(
    errorDescription: String? = null,
    errorCode: String = UNKNOWN,
    isRecoverable: Boolean,
) : CheckoutException(errorDescription ?: "Checkout is currently unavailable due to an internal error", errorCode, isRecoverable) {
        public companion object {
            public const val CLIENT_ERROR: String = "client_error"
            public const val HTTP_ERROR: String = "http_error"
            public const val UNKNOWN: String = "unknown"
        }
}

/**
 * Subclass of CheckoutUnavailableException, issued when Checkout is unavailable because a HTTP call resulted in an unexpected status code,
 * (incl. both client or server HTTP errors).
 */
public class HttpException @JvmOverloads constructor(
    errorDescription: String? = null,
    public val statusCode: Int, isRecoverable: Boolean
) : CheckoutUnavailableException(errorDescription, HTTP_ERROR, isRecoverable)

/**
 * Subclass of CheckoutUnavailableException, issued when Checkout is unavailable for reasons unrelated to HTTP calls.
 */
public class ClientException @JvmOverloads constructor(
    errorDescription: String? = null,
    isRecoverable: Boolean
) : CheckoutUnavailableException(errorDescription, CLIENT_ERROR, isRecoverable)

/**
 * Issued when checkout is no longer available and will no longer be available with the checkout URL supplied.
 * This may happen when the user has paused on checkout for a long period (hours) and
 * then attempted to proceed again with the same checkout URL.
 * In event of checkoutExpired, a new checkout URL will need to be generated.
 */
public class CheckoutExpiredException @JvmOverloads constructor(
    errorDescription: String? = null,
    errorCode: String = UNKNOWN,
    isRecoverable: Boolean,
) : CheckoutException(
    errorDescription ?: "Checkout is no longer available with the provided token. Please generate a new checkout URL",
    errorCode,
    isRecoverable,
) {
        public companion object {
            public const val CART_EXPIRED: String = "cart_expired"
            public const val CART_COMPLETED: String = "cart_completed"
            public const val INVALID_CART: String = "invalid_cart"
            public const val UNKNOWN: String = "unknown"
        }
}

/**
 * Issued when the provided checkout URL results in an error related to a configuration issue, e.g. the shop being on checkout.liquid.
 * The SDK only supports stores migrated for extensibility.
 */
public class ConfigurationException @JvmOverloads constructor(
    errorDescription: String? = null,
    errorCode: String = UNKNOWN,
    isRecoverable: Boolean,
) : CheckoutException(errorDescription ?: "Checkout is unavailable due to a configuration issue.", errorCode, isRecoverable) {
        public companion object {
            public const val CHECKOUT_LIQUID_NOT_MIGRATED: String = "checkout_liquid_not_migrated"
            public const val STOREFRONT_PASSWORD_REQUIRED: String = "storefront_password_required"
            public const val UNKNOWN: String = "unknown"
        }
}
