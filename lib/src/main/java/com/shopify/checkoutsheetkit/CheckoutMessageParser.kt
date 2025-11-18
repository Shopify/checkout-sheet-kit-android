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

import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_COMPLETE
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_START
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutStartEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

internal class CheckoutMessageParser(
    internal val json: Json,
    private val log: LogWrapper = ShopifyCheckoutSheetKit.log,
) {

    /**
     * Parse a raw JSON-RPC message and return either an RPC request or a notification event.
     */
    fun parse(rawMessage: String): CheckoutMessage? {
        // First try to decode via the registry for RPC requests
        val rpcRequest = RPCRequestRegistry.decode(rawMessage)
        if (rpcRequest != null) {
            return CheckoutMessage.Request(rpcRequest)
        }

        // Fall back to manual parsing for notifications and other messages
        val envelope = json.runCatching { decodeFromString<JsonRpcEnvelope>(rawMessage) }.getOrNull()
            ?: return null

        return when (envelope.method) {
            METHOD_START -> envelope.params
                .decodeOrNull<CheckoutStartEvent> {
                    log.d(LOG_TAG, "Failed to decode checkout start params: ${it.message}")
                }
                ?.let { CheckoutMessage.StartNotification(it) }

            METHOD_COMPLETE -> envelope.params
                .decodeOrNull<CheckoutCompleteEvent> {
                    log.d(LOG_TAG, "Failed to decode checkout completed params: ${it.message}")
                }
                ?.let { CheckoutMessage.CompleteNotification(it) }

            else -> {
                log.d(LOG_TAG, "Received unsupported message method: ${envelope.method}")
                null
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

    @Serializable
    private data class JsonRpcEnvelope(
        @SerialName(CheckoutMessageContract.VERSION_FIELD)
        val version: String? = null,
        val method: String? = null,
        val params: JsonElement? = null,
        val id: JsonElement? = null,
    )

    companion object {
        private const val LOG_TAG = "CheckoutMessageParser"
    }

    private inline fun <reified T> JsonElement?.decodeOrNull(onFailure: (Throwable) -> Unit): T? {
        return this?.let {
            runCatching { json.decodeFromJsonElement<T>(it) }
                .onFailure(onFailure)
                .getOrNull()
        }
    }
}
