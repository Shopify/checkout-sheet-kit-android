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

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigurationTest {

    @Test
    fun `can set colorScheme via configure function - light`() {
        ShopifyCheckoutSheet.configure {
            it.colorScheme = ColorScheme.Light()
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().colorScheme).isEqualTo(ColorScheme.Light())
    }

    @Test
    fun `can set colorScheme via configure function - dark`() {
        ShopifyCheckoutSheet.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().colorScheme).isEqualTo(ColorScheme.Dark())
    }

    @Test
    fun `can set colorScheme via configure function - web`() {
        ShopifyCheckoutSheet.configure {
            it.colorScheme = ColorScheme.Web()
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().colorScheme).isEqualTo(ColorScheme.Web())
    }

    @Test
    fun `can set colorScheme via configure function - automatic`() {
        ShopifyCheckoutSheet.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().colorScheme).isEqualTo(ColorScheme.Automatic())
    }

    @Test
    fun `can set preloading via configure function - enabled`() {
        ShopifyCheckoutSheet.configure {
            it.preloading = Preloading(enabled = true)
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().preloading.enabled).isTrue
    }

    @Test
    fun `can set preloading via configure function - disabled`() {
        ShopifyCheckoutSheet.configure {
            it.preloading = Preloading(enabled = false)
        }

        assertThat(ShopifyCheckoutSheet.getConfiguration().preloading.enabled).isFalse
    }
}
