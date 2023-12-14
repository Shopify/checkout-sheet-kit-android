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
package com.shopify.checkoutkit

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.ANALYTICS
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.COMPLETED
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.MODAL
import com.shopify.checkoutkit.messages.AnalyticsEventDecoder
import com.shopify.checkoutkit.messages.InstrumentationPayload
import com.shopify.checkoutkit.messages.SDKToWebEvent
import com.shopify.checkoutkit.messages.WebToSDKMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class CheckoutBridge(
    private var eventProcessor: CheckoutWebViewEventProcessor,
    private val decoder: Json = Json { ignoreUnknownKeys = true },
    private val analyticsEventDecoder: AnalyticsEventDecoder = AnalyticsEventDecoder(decoder),
) {

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        this.eventProcessor = eventProcessor
    }

    fun getEventProcessor(): CheckoutWebViewEventProcessor = this.eventProcessor

    enum class CheckoutWebOperation(val key: String) {
        COMPLETED("completed"),
        MODAL("checkoutBlockingEvent"),
        ANALYTICS("analytics");

        companion object {
            fun fromKey(key: String): CheckoutWebOperation? {
                return values().find { it.key == key }
            }
        }
    }

    sealed class SDKOperation(val key: String) {
        object Presented : SDKOperation("presented")
        class Instrumentation(val payload: InstrumentationPayload): SDKOperation("instrumentation")
    }

    // Allows Web to postMessages back to the SDK
    @JavascriptInterface
    fun postMessage(message: String) {
        val decodedMsg = decoder.decodeFromString<WebToSDKMessage>(message)

        when (CheckoutWebOperation.fromKey(decodedMsg.name)) {
            COMPLETED -> eventProcessor.onCheckoutViewComplete()
            MODAL -> {
                val modalVisible = decodedMsg.body.toBooleanStrictOrNull()
                modalVisible?.let {
                    eventProcessor.onCheckoutViewModalToggled(modalVisible)
                }
            }
            ANALYTICS -> {
                val analyticsEvent = analyticsEventDecoder.decode(decodedMsg)
                analyticsEvent?.let {
                    eventProcessor.onAnalyticsEvent(analyticsEvent)
                }
            }
            else -> {}
        }
    }

    // Send messages from SDK to Web
    @Suppress("SwallowedException")
    fun sendMessage(view: WebView, operation: SDKOperation) {
        val script = when (operation) {
            is SDKOperation.Presented -> dispatchMessageTemplate("'${operation.key}'")
            is SDKOperation.Instrumentation -> {
                val body = Json.encodeToString(SDKToWebEvent(operation.payload))
                dispatchMessageTemplate("'${operation.key}', $body")
            }
        }
        try {
            view.evaluateJavascript(script, null)
        } catch (e: Exception) {
            eventProcessor.onCheckoutViewFailedWithError(
                CheckoutSdkError("Failed to send '${operation.key}' message to checkout, some features may not work.")
            )
        }
    }

    companion object {
        private const val SDK_VERSION_NUMBER: String = BuildConfig.SDK_VERSION
        private const val SCHEMA_VERSION_NUMBER: String = "7.0"
        private fun dispatchMessageTemplate(body: String) = """|
        |if (window.MobileCheckoutSdk && window.MobileCheckoutSdk.dispatchMessage) {
        |    window.MobileCheckoutSdk.dispatchMessage($body);
        |} else {
        |    window.addEventListener('mobileCheckoutBridgeReady', function () {
        |        window.MobileCheckoutSdk.dispatchMessage($body);
        |    }, {passive: true, once: true});
        |}
        |""".trimMargin()

        fun userAgentSuffix(): String {
            val theme = ShopifyCheckoutKit.configuration.colorScheme.id
            return "ShopifyCheckoutSDK/$SDK_VERSION_NUMBER ($SCHEMA_VERSION_NUMBER;$theme;standard)"
        }
    }
}
