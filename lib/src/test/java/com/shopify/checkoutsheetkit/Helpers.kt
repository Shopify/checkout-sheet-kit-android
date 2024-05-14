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
    companion object {
        fun assertThat(actual: CheckoutException): CheckoutExceptionAssert {
            return CheckoutExceptionAssert(actual)
        }
    }

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
