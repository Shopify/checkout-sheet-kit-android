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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ConfigurationTest {

    private lateinit var initialConfiguration: Configuration

    @Before
    fun setUp() {
        initialConfiguration = ShopifyCheckoutSheetKit.configuration
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = initialConfiguration.colorScheme
            it.preloading = initialConfiguration.preloading
            it.errorRecovery = initialConfiguration.errorRecovery
        }
    }

    @Test
    fun `can set colorScheme via configure function - light`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Light()
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().colorScheme).isEqualTo(ColorScheme.Light())
    }

    @Test
    fun `can set colorScheme via configure function - dark`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Dark()
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().colorScheme).isEqualTo(ColorScheme.Dark())
    }

    @Test
    fun `can set colorScheme via configure function - web`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Web()
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().colorScheme).isEqualTo(ColorScheme.Web())
    }

    @Test
    fun `can set colorScheme via configure function - automatic`() {
        ShopifyCheckoutSheetKit.configure {
            it.colorScheme = ColorScheme.Automatic()
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().colorScheme).isEqualTo(ColorScheme.Automatic())
    }

    @Test
    fun `can set preloading via configure function - enabled`() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = true)
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().preloading.enabled).isTrue
    }

    @Test
    fun `can set preloading via configure function - disabled`() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = false)
        }

        assertThat(ShopifyCheckoutSheetKit.getConfiguration().preloading.enabled).isFalse
    }

    @Test
    fun `by default attempt to recover from recoverable errors`() {
        val recoverableException = recoverableException()

        val shouldRecover = ShopifyCheckoutSheetKit.configuration.errorRecovery.shouldRecoverFromError(
            recoverableException
        )

        assertThat(shouldRecover).isEqualTo(true)
    }

    @Test
    fun `can disable error recovery`() {
        val recoverableException = recoverableException()
        ShopifyCheckoutSheetKit.configure {
            it.errorRecovery = object : ErrorRecovery {
                override fun shouldRecoverFromError(checkoutException: CheckoutException) = false
            }
        }

        val shouldRecover = ShopifyCheckoutSheetKit.configuration.errorRecovery.shouldRecoverFromError(
            recoverableException
        )

        assertThat(shouldRecover).isEqualTo(false)
    }

    @Test
    fun `can set pre-recovery actions`() {
        val mockFn = mock<Function0<Unit>>()

        val recoverableException = recoverableException()
        ShopifyCheckoutSheetKit.configure {
            it.errorRecovery = object : ErrorRecovery {
                override fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
                    mockFn.invoke()
                }
            }
        }

        ShopifyCheckoutSheetKit.configuration.errorRecovery.preRecoveryActions(
            recoverableException,
            "https://shopify.dev"
        )

        verify(mockFn).invoke()
    }

    private fun recoverableException(): CheckoutException {
        return CheckoutSheetKitException(
            errorDescription = "Unknown error",
            errorCode = CheckoutSheetKitException.UNKNOWN,
            isRecoverable = true
        )
    }
}
