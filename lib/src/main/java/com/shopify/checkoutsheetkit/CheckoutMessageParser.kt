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

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import com.shopify.checkoutsheetkit.rpc.CheckoutComplete
import com.shopify.checkoutsheetkit.rpc.CheckoutStart
import com.shopify.checkoutsheetkit.rpc.RPCRequest
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.rpc.events.AddressChangeRequested
import kotlinx.serialization.json.Json

internal class CheckoutMessageParser(
    internal val json: Json,
    private val log: LogWrapper = ShopifyCheckoutSheetKit.log,
) {

    /**
     * Parse a raw JSON-RPC message and return either an RPC request or a notification event.
     */
    fun parse(rawMessage: String): CheckoutMessage? {
        // RPCRequestRegistry will decode all supported messages
        return when (val decoded = RPCRequestRegistry.decode(rawMessage)) {
            is AddressChangeRequested -> CheckoutMessage.Request(decoded)
            is CheckoutStart -> CheckoutMessage.StartNotification(decoded.params)
            is CheckoutComplete -> CheckoutMessage.CompleteNotification(decoded.params)
            null -> {
                log.d(LOG_TAG, "Received unsupported or invalid message")
                null
            }
            else -> {
                // For any other RPCRequest types that might be added in the future
                CheckoutMessage.Request(decoded)
            }
        }
    }

    /**
     * Wrapper for different types of checkout messages
     */
    sealed class CheckoutMessage {
        data class Request(val rpcRequest: RPCRequest<*, *>) : CheckoutMessage()
        data class StartNotification(val event: CheckoutStartEvent) : CheckoutMessage()
        data class CompleteNotification(val event: CheckoutCompleteEvent) : CheckoutMessage()
    }

    companion object {
        private const val LOG_TAG = "CheckoutMessageParser"
    }
}
