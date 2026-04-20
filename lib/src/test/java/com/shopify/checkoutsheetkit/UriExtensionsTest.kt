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

import android.net.Uri
import androidx.core.net.toUri
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UriExtensionsTest {

    @Test
    fun `isConfirmationPage returns true for thank-you path segment`() {
        assertThat("https://shop.com/cn-12345/thank-you".toUri().isConfirmationPage()).isTrue()
    }

    @Test
    fun `isConfirmationPage returns true for thank_you path segment`() {
        assertThat("https://shop.com/cn-12345/thank_you".toUri().isConfirmationPage()).isTrue()
    }

    @Test
    fun `isConfirmationPage is case insensitive`() {
        assertThat("https://shop.com/cn-12345/THANK-YOU".toUri().isConfirmationPage()).isTrue()
        assertThat("https://shop.com/cn-12345/Thank_You".toUri().isConfirmationPage()).isTrue()
    }

    @Test
    fun `isConfirmationPage matches when followed by a query string`() {
        assertThat("https://shop.com/cn-12345/thank-you?order_id=42".toUri().isConfirmationPage()).isTrue()
    }

    @Test
    fun `isConfirmationPage returns true for thank-you non-last segment`() {
        assertThat("https://shop.com/cn-12345/foo/thank-you/bar".toUri().isConfirmationPage()).isTrue()
    }

    @Test
    fun `isConfirmationPage returns false for non-confirmation paths`() {
        assertThat("https://shop.com/cn-12345/checkout".toUri().isConfirmationPage()).isFalse()
        assertThat("https://shop.com/products/widget".toUri().isConfirmationPage()).isFalse()
        assertThat("https://shop.com/".toUri().isConfirmationPage()).isFalse()
    }

    @Test
    fun `isConfirmationPage returns false when thank-you is only a substring`() {
        assertThat("https://shop.com/thank-you-page".toUri().isConfirmationPage()).isFalse()
        assertThat("https://shop.com/pre-thank-you".toUri().isConfirmationPage()).isFalse()
        assertThat("https://shop.com/prethankyou".toUri().isConfirmationPage()).isFalse()
    }

    @Test
    fun `isConfirmationPage returns false when thank-you appears only in the query string`() {
        assertThat("https://shop.com/checkout?next=thank-you".toUri().isConfirmationPage()).isFalse()
    }

    @Test
    fun `isConfirmationPage returns false for null uri`() {
        val uri: Uri? = null
        assertThat(uri.isConfirmationPage()).isFalse()
    }
}
