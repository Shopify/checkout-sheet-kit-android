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

import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit.log
import com.shopify.checkoutsheetkit.WebToSdkEvent
import com.shopify.checkoutsheetkit.lifecycleevents.DeliveryAddressChangePayload
import java.util.concurrent.CompletableFuture
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
public data class CheckoutAddressChangeRequestedEventData(
    public val addressType: String,
    public val selectedAddress: CheckoutAddressChangeRequestedSelectedAddress? = null,
)

@Serializable
public data class CheckoutAddressChangeRequestedSelectedAddress(
    public val firstName: String? = null,
    public val lastName: String? = null,
    public val company: String? = null,
    public val address1: String? = null,
    public val address2: String? = null,
    public val city: String? = null,
    public val countryCode: String? = null,
    public val phone: String? = null,
    public val provinceCode: String? = null,
    public val zip: String? = null,
)

public class CheckoutAddressChangeRequestedEvent internal constructor(
    public val addressType: String,
    public val selectedAddress: CheckoutAddressChangeRequestedSelectedAddress?,
    private val onResponse: (DeliveryAddressChangePayload) -> Unit,
    private val onCancel: () -> Unit
) {

    private val lock = Any()
    private var hasCompleted = false
    private var pendingFuture: CompletableFuture<DeliveryAddressChangePayload>? = null

    public fun respondWith(result: DeliveryAddressChangePayload) {
        val shouldRespond = synchronized(lock) {
            if (hasCompleted) {
                false
            } else {
                hasCompleted = true
                pendingFuture?.cancel(true)
                pendingFuture = null
                true
            }
        }
        if (shouldRespond) {
            onResponse(result)
        }
    }

    public fun respondWith(future: CompletableFuture<DeliveryAddressChangePayload>) {
        val attachFuture = synchronized(lock) {
            if (hasCompleted) {
                false
            } else {
                pendingFuture?.cancel(true)
                pendingFuture = future
                true
            }
        }

        if (!attachFuture) {
            return
        }

        future.whenComplete { payload, throwable ->
            val action = synchronized(lock) {
                if (hasCompleted || pendingFuture !== future) {
                    null
                } else {
                    hasCompleted = true
                    pendingFuture = null
                    if (throwable == null && payload != null) {
                        Action.Respond(payload)
                    } else {
                        Action.Cancel
                    }
                }
            }

            when (action) {
                is Action.Respond -> onResponse(action.payload)
                Action.Cancel -> onCancel()
                null -> Unit
            }
        }
    }

    public fun cancel() {
        val futureToCancel = synchronized(lock) {
            if (hasCompleted) {
                null
            } else {
                hasCompleted = true
                val future = pendingFuture
                pendingFuture = null
                future
            }
        }
        futureToCancel?.cancel(true)
        onCancel()
    }

    private sealed class Action {
        data class Respond(val payload: DeliveryAddressChangePayload) : Action()
        object Cancel : Action()
    }
}

/**
 * Decoder for checkout address change requested messages using kotlinx serialization.
 */
internal class CheckoutAddressChangeRequestedDecoder constructor(
    private val decoder: Json = Json { ignoreUnknownKeys = true }
) {
    private companion object {
        private const val LOG_TAG = "CheckoutAddressChangeRequestedDecoder"
    }

    fun decode(
        decodedMsg: WebToSdkEvent,
        onResponse: (DeliveryAddressChangePayload) -> Unit,
        onCancel: () -> Unit
    ): CheckoutAddressChangeRequestedEvent? {
        return try {
            val eventData = decoder.decodeFromString<CheckoutAddressChangeRequestedEventData>(decodedMsg.body)
            CheckoutAddressChangeRequestedEvent(
                addressType = eventData.addressType,
                selectedAddress = eventData.selectedAddress,
                onResponse = onResponse,
                onCancel = onCancel
            )
        } catch (e: Exception) {
            log.e(LOG_TAG, "Failed to decode AddressChangeRequested event", e)
            null
        }
    }
}
