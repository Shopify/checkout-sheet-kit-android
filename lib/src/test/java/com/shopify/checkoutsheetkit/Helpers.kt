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

import android.app.Activity
import android.net.Uri
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import org.assertj.core.api.AbstractAssert

fun withPreloadingEnabled(block: () -> Unit) {
    try {
        ShopifyCheckoutSheetKit.configure { it.preloading = Preloading(enabled = true) }
        block()
    } finally {
        ShopifyCheckoutSheetKit.configure { it.preloading = Preloading(enabled = false) }
    }
}

class CheckoutExceptionAssert(actual: CheckoutException) :
    AbstractAssert<CheckoutExceptionAssert, CheckoutException>(actual, CheckoutExceptionAssert::class.java) {

    fun isRecoverable(): CheckoutExceptionAssert {
        isNotNull()

        if (!actual.isRecoverable) {
            failWithMessage("Expected exception to be recoverable but was not")
        }

        return this
    }

    fun isNotRecoverable(): CheckoutExceptionAssert {
        isNotNull()

        if (actual.isRecoverable) {
            failWithMessage("Expected exception not to be recoverable but was")
        }

        return this
    }

    fun hasDescription(description: String): CheckoutExceptionAssert {
        isNotNull()

        if (actual.errorDescription != description) {
            failWithMessage("Expected exception to have description <%s>, but was, <%s>", description, actual.errorDescription)
        }

        return this
    }

    fun hasErrorCode(errorCode: String): CheckoutExceptionAssert {
        isNotNull()

        if (actual.errorCode != errorCode) {
            failWithMessage("Expected exception to have errorCode <%s>, but was, <%s>", errorCode, actual.errorCode)
        }

        return this
    }

    fun hasStatusCode(statusCode: Int): CheckoutExceptionAssert {
        isNotNull()

        if (actual !is HttpException) {
            failWithMessage("Cannot assert status code on an exception that is not a HttpException")
        }

        val actualCode = (actual as HttpException).statusCode
        if (actualCode != statusCode) {
            failWithMessage("Expected exception to have statusCode <%s>, but was, <%s>", statusCode, actualCode)
        }

        return this
    }
}

fun noopDefaultCheckoutEventProcessor(activity: Activity, log: LogWrapper = LogWrapper()): DefaultCheckoutEventProcessor {
    return object : DefaultCheckoutEventProcessor(activity, log) {
        override fun onComplete(checkoutCompleteEvent: CheckoutCompleteEvent) {
            // no-op
        }

        override fun onFail(error: CheckoutException) {
            // no-op
        }

        override fun onCancel() {
            // no-op
        }

        override fun onAddressChangeStart(event: CheckoutAddressChangeStart) {
            // no-op
        }
    }
}

class EmbedParamAssert(actual: Uri?) :
    AbstractAssert<EmbedParamAssert, Uri?>(actual, EmbedParamAssert::class.java) {

    fun hasBaseUrl(expected: String): EmbedParamAssert {
        val uri = actualUri()
        val scheme = uri.scheme?.let { "$it://" } ?: ""
        val authority = uri.encodedAuthority.orEmpty()
        val path = uri.encodedPath.takeUnless { it.isNullOrEmpty() } ?: ""
        val base = "$scheme$authority$path"
        if (base != expected) {
            failWithMessage("Expected base URL to be <%s> but was <%s>", expected, base)
        }
        return this
    }

    fun hasQueryParameter(key: String, expectedValue: String): EmbedParamAssert {
        val uri = actualUri()
        val actualValue = uri.getQueryParameter(key)
        if (actualValue != expectedValue) {
            failWithMessage(
                "Expected query parameter <%s> to have value <%s> but was <%s>",
                key,
                expectedValue,
                actualValue,
            )
        }
        return this
    }

    fun hasEmbedParamExactly(expected: Map<String, String>): EmbedParamAssert {
        val actualMap = embedMap()
        if (actualMap != expected) {
            failWithMessage("Expected embed param <%s> but was <%s>", expected, actualMap)
        }
        return this
    }

    fun withEmbedParameters(vararg expectedEntries: Pair<String, String>): EmbedParamAssert {
        val includeRecovery = expectedEntries.any { (key, value) ->
            key == EmbedFieldKey.RECOVERY && value.equals("true", ignoreCase = true)
        }

        val expected = defaultEmbed(includeRecovery).toMutableMap()
        expectedEntries.forEach { (key, value) ->
            expected[key] = value
        }

        return hasEmbedParamExactly(expected)
    }

    fun withoutEmbedParameters(vararg keys: String): EmbedParamAssert {
        val actualMap = embedMap()
        keys.forEach { key ->
            if (actualMap.containsKey(key)) {
                failWithMessage("Expected embed parameter <%s> to be absent", key)
            }
        }
        return this
    }

    private fun actualUri(): Uri {
        isNotNull
        return actual!!
    }

    private fun embedMap(): Map<String, String> {
        val uri = actualUri()
        val encoded = uri.getQueryParameter(QueryParamKey.EMBED)
        if (encoded == null) {
            failWithMessage("Expected query parameter '%s' to be present", QueryParamKey.EMBED)
            return emptyMap()
        }

        val decoded = Uri.decode(encoded).trim()
        return parseEmbed(decoded)
    }

    private fun defaultEmbed(includeRecovery: Boolean): Map<String, String> {
        val embedValue = EmbedParamBuilder.build(isRecovery = includeRecovery)
        return parseEmbed(embedValue)
    }

    private fun parseEmbed(value: String): Map<String, String> {
        if (value.isEmpty()) {
            return emptyMap()
        }

        return value.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .associate { entry ->
                val parts = entry.split("=", limit = 2)
                val key = parts[0]
                val entryValue = parts.getOrElse(1) { "" }
                key to entryValue
            }
    }
}
