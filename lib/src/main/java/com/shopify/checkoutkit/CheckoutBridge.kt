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

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.COMPLETED
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.MODAL
import com.shopify.checkoutkit.CheckoutBridge.CheckoutWebOperation.INIT
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class CheckoutBridge(
    private val webView: CheckoutWebView,
    private var eventProcessor: CheckoutWebViewEventProcessor,
    private val decoder: Json = Json { ignoreUnknownKeys = true }
) {

    private var hasInitialized = false
    private var messageBuffer = mutableListOf<String>()

    fun setEventProcessor(eventProcessor: CheckoutWebViewEventProcessor) {
        this.eventProcessor = eventProcessor
    }

    fun getEventProcessor(): CheckoutWebViewEventProcessor = this.eventProcessor

    enum class CheckoutWebOperation(val key: String) {
        COMPLETED("completed"),
        MODAL("checkoutBlockingEvent"),
        INIT("init");

        companion object {
            fun fromKey(key: String): CheckoutWebOperation? {
                return values().find { it.key == key }
            }
        }
    }

    enum class SDKOperation(val key: String) {
        PRESENTED("presented")
    }

    // Allows Web to postMessages back to the SDK
    @JavascriptInterface
    fun postMessage(message: String) {
        val decodedMsg = decoder.decodeFromString<JSMessage>(message)

        when (CheckoutWebOperation.fromKey(decodedMsg.name)) {
            COMPLETED -> eventProcessor.onCheckoutViewComplete()
            MODAL -> {
                val modalVisible = decodedMsg.body.toBooleanStrictOrNull()
                modalVisible?.let {
                    eventProcessor.onCheckoutViewModalToggled(modalVisible)
                }
            }
            INIT -> {
                hasInitialized = true
                Handler(Looper.getMainLooper()).post {
                    messageBuffer.forEach { webView.evaluateJavascript(it, null) }
                    messageBuffer.clear()
                }
            }

            else -> {}
        }
    }

    // Send messages from SDK to Web
    @Suppress("SwallowedException")
    fun sendMessage(type: SDKOperation) {
        val script = when (type) {
            SDKOperation.PRESENTED -> "window.MobileCheckoutSdk.dispatchMessage('presented');"
        }
        try {
            if (!hasInitialized) {
                // Buffer messages until the web SDK is ready to receive events
                messageBuffer.add(script)
            } else {
                webView.evaluateJavascript(script, null)
            }
        } catch (e: Exception) {
            eventProcessor.onCheckoutViewFailedWithError(
                CheckoutSdkError("Failed to send '${type.key}' message to checkout, some features may not work.")
            )
        }
    }

    @Serializable
    internal data class JSMessage(val name: String, val body: String = "")

    companion object {
        private const val SDK_VERSION_NUMBER: String = BuildConfig.SDK_VERSION
        private const val SCHEMA_VERSION_NUMBER: String = "7.0"

        fun userAgentSuffix(): String {
            val theme = ShopifyCheckoutKit.configuration.colorScheme.id
            return "ShopifyCheckoutSDK/$SDK_VERSION_NUMBER ($SCHEMA_VERSION_NUMBER;$theme;standard)"
        }

        fun instrument(webView: WebView, payload: InstrumentationPayload) {
            val event = SdkToWebEvent(payload)
            val json = Json.encodeToString(event)
            webView.evaluateJavascript(
                "setTimeout(()=> {window.MobileCheckoutSdk.dispatchMessage('instrumentation', $json); }, 1000)",
                null)
        }
    }
}

@Serializable
internal data class SdkToWebEvent<T>(
    val detail: T
)
@Serializable
internal data class InstrumentationPayload(
    val name: String,
    val value: Long,
    val type: InstrumentationType,
    val tags: Map<String, String>
)

@Suppress("EnumEntryName", "EnumNaming")
@Serializable
internal enum class InstrumentationType {
    histogram, incrementCounter
}
