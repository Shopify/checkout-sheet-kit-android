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
package com.shopify.checkoutsheetkit.errorevents

import com.shopify.checkoutsheetkit.AuthenticationException
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.CheckoutExpiredException
import com.shopify.checkoutsheetkit.ClientException
import com.shopify.checkoutsheetkit.ConfigurationException
import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.WebToSdkEvent
import kotlinx.serialization.json.Json

internal class CheckoutErrorDecoder @JvmOverloads constructor(
    private val decoder: Json,
    private val log: LogWrapper = LogWrapper()
) {
    fun decode(message: WebToSdkEvent): CheckoutException? = try {
        decodeMessage(message).mapToCheckoutException()
    } catch (e: Exception) {
        log.e("CheckoutBridge", "Failed to decode CheckoutErrorPayload", e)
        throw e
    }

    internal fun decodeMessage(message: WebToSdkEvent): CheckoutErrorPayload {
        val errors = decoder.decodeFromString<List<CheckoutErrorPayload>>(message.body)
        return errors.first()
    }

    private fun CheckoutErrorPayload.mapToCheckoutException(): CheckoutException? {
        return when {
            this.group == CheckoutErrorGroup.CONFIGURATION && this.code == CUSTOMER_ACCOUNT_REQUIRED -> {
                AuthenticationException(
                    errorDescription = this.reason ?: "Customer account required.",
                    errorCode = AuthenticationException.CUSTOMER_ACCOUNT_REQUIRED,
                    isRecoverable = false,
                )
            }
            this.group == CheckoutErrorGroup.CONFIGURATION -> {
                ConfigurationException(
                    errorDescription = this.reason ?: "Storefront configuration error.",
                    errorCode = if (this.code == STOREFRONT_PASSWORD_REQUIRED) {
                        ConfigurationException.STOREFRONT_PASSWORD_REQUIRED }
                    else  {
                        ConfigurationException.UNKNOWN
                    },
                    isRecoverable = false,
                )
            }
            this.group == CheckoutErrorGroup.UNRECOVERABLE ->
                ClientException(
                    errorDescription = this.reason,
                    isRecoverable = true,
                )
            this.group == CheckoutErrorGroup.EXPIRED ->
                CheckoutExpiredException(
                    errorDescription = this.reason,
                    errorCode = this.expiredErrorCode(),
                    isRecoverable = false,
                )
            else -> {
                // The remaining error groups are unsupported and will be ignored
                null
            }
        }
    }

    private fun CheckoutErrorPayload.expiredErrorCode(): String {
        return when (this.code) {
            INVALID_CART -> CheckoutExpiredException.INVALID_CART
            CART_COMPLETED -> CheckoutExpiredException.CART_COMPLETED
            else -> CheckoutExpiredException.CART_EXPIRED
        }
    }

    companion object {
        private const val CUSTOMER_ACCOUNT_REQUIRED = "customer_account_required"
        private const val STOREFRONT_PASSWORD_REQUIRED = "storefront_password_required"
        private const val INVALID_CART = "invalid_cart"
        private const val CART_COMPLETED = "cart_completed"
    }
}
