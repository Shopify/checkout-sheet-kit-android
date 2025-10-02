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

import com.shopify.checkoutsheetkit.CheckoutMessageContract.BODY_KEY
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_ADDRESS_CHANGE_REQUESTED
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_COMPLETED
import com.shopify.checkoutsheetkit.CheckoutMessageContract.METHOD_FIELD
import com.shopify.checkoutsheetkit.CheckoutMessageContract.PARAMS_FIELD
import com.shopify.checkoutsheetkit.CheckoutMessageContract.VERSION
import com.shopify.checkoutsheetkit.CheckoutMessageContract.VERSION_FIELD
import com.shopify.checkoutsheetkit.LogWrapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class CheckoutMessageParser(
    private val json: Json,
    private val log: LogWrapper = ShopifyCheckoutSheetKit.log,
) {

    fun parse(rawMessage: String): CheckoutMessage? {
        val envelope = runCatching { json.parseToJsonElement(rawMessage) }.getOrNull() as? JsonObject
            ?: return null

        val messageObject = envelope[BODY_KEY]?.let { extractMessageObject(it) } ?: return null
        val version = messageObject[VERSION_FIELD]?.jsonPrimitive?.contentOrNull
        if (version != VERSION) {
            log.d(LOG_TAG, "Unsupported message version: $version")
            return null
        }

        val method = messageObject[METHOD_FIELD]?.jsonPrimitive?.contentOrNull ?: return null
        val params = messageObject[PARAMS_FIELD]?.jsonObject

        return when (method) {
            METHOD_ADDRESS_CHANGE_REQUESTED -> params?.let { CheckoutMessage.AddressChangeRequested(it) }
            METHOD_COMPLETED -> params?.let { CheckoutMessage.Completed(it) }
            else -> {
                log.d(LOG_TAG, "Received unsupported message method: $method")
                null
            }
        }
    }

    private fun extractMessageObject(bodyElement: kotlinx.serialization.json.JsonElement): JsonObject? {
        return when (bodyElement) {
            is JsonObject -> bodyElement
            is JsonPrimitive -> {
                val content = bodyElement.contentOrNull ?: return null
                runCatching { json.parseToJsonElement(content) }.getOrNull() as? JsonObject
            }

            else -> null
        }
    }

    sealed class CheckoutMessage {
        data class AddressChangeRequested(val params: JsonObject) : CheckoutMessage()
        data class Completed(val params: JsonObject) : CheckoutMessage()
    }

    companion object {
        private const val LOG_TAG = "CheckoutMessageParser"
    }
}
