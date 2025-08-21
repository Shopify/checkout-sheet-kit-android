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
package com.shopify.checkoutsheetkit.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.CheckoutSheetKitDialog
import com.shopify.checkoutsheetkit.CheckoutSheetKitException
import com.shopify.checkoutsheetkit.DefaultCheckoutEventProcessor
import com.shopify.checkoutsheetkit.lifecycleevents.CartInfo
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.lifecycleevents.OrderDetails
import com.shopify.checkoutsheetkit.lifecycleevents.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class)
class ShopifyCheckoutTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `ShopifyCheckout initializes without crashing when not visible`() {
        composeTestRule.setContent {
            ShopifyCheckout(
                url = "https://example.myshopify.com/checkouts/test",
                isVisible = false,
                onComplete = { },
                onCancel = { },
                onFail = { },
                onLinkClicked = { },
            )
        }

        // Should initialize without crashing when not visible
        composeTestRule.waitForIdle()
    }

    @Test
    fun `ShopifyCheckout simplified overload initializes without crashing when not visible`() {
        composeTestRule.setContent {
            ShopifyCheckout(
                url = "https://example.myshopify.com/checkouts/test",
                isVisible = false,
                onComplete = { },
                onCancel = { },
                onFail = { },
            )
        }

        // Should initialize without crashing when not visible
        composeTestRule.waitForIdle()
    }

    @Test
    fun `ShopifyCheckout calls onCancel when isVisible changes from true to false`() {
        var canceledCalled = false
        var isVisible by mutableStateOf(true)

        composeTestRule.setContent {
            ShopifyCheckout(
                url = "https://example.myshopify.com/checkouts/test",
                isVisible = isVisible,
                onComplete = { },
                onCancel = { canceledCalled = true },
                onFail = { }
            )
        }

        composeTestRule.waitForIdle()

        // Change visibility to false
        isVisible = false
        composeTestRule.waitForIdle()

        assertThat(canceledCalled).isTrue()
    }


    @Test
    fun `ShopifyCheckout handles empty checkout URL`() {
        composeTestRule.setContent {
            ShopifyCheckout(
                url = "",
                isVisible = true,
                onComplete = { },
                onCancel = { },
                onFail = { },
            )
        }

        // Should handle empty URL without crashing
        composeTestRule.waitForIdle()
    }

    @Test
    fun `ShopifyCheckout properly wires callbacks to ComposeCheckoutEventProcessor`() {
        // Capture the processor and mock the dialog
        var capturedProcessor: DefaultCheckoutEventProcessor? = null
        val mockDialog = mock<CheckoutSheetKitDialog>()

        val mockPresentCheckout: PresentCheckout =
            { _, _, processor ->
                capturedProcessor = processor
                mockDialog
            }

        // Track callback invocations
        var completedEvent: CheckoutCompletedEvent? = null
        var canceledCalled = false
        var failedError: CheckoutException? = null

        composeTestRule.setContent {
            ShopifyCheckout(
                url = "https://example.myshopify.com/checkouts/test",
                isVisible = true,
                onComplete = { completedEvent = it },
                onCancel = { canceledCalled = true },
                onFail = { failedError = it },
                presentCheckout = mockPresentCheckout
            )
        }

        composeTestRule.waitForIdle()

        assertThat(capturedProcessor).isNotNull()

        // Test that callbacks are properly wired by invoking processor methods
        val testCompletedEvent = CheckoutCompletedEvent(
            orderDetails = OrderDetails(
                id = "123",
                cart = CartInfo(
                    token = "456",
                    lines = listOf(),
                    price = Price(),
                )
            )
        )
        
        capturedProcessor!!.onCheckoutCompleted(testCompletedEvent)
        assertThat(completedEvent).isEqualTo(testCompletedEvent)

        capturedProcessor.onCheckoutCanceled()
        assertThat(canceledCalled).isTrue()

        val testError = CheckoutSheetKitException("Integration test error", "integration_error", false)
        capturedProcessor.onCheckoutFailed(testError)
        assertThat(failedError).isEqualTo(testError)
    }

    @Test
    fun `ShopifyCheckout presents checkout when isVisible changes from false to true`() {
        var presentCalled = false
        var capturedUrl: String? = null
        val mockDialog = mock<CheckoutSheetKitDialog>()
        var isVisible by mutableStateOf(false)

        val mockPresentCheckout: PresentCheckout =
            { url, _, _ ->
                presentCalled = true
                capturedUrl = url
                mockDialog
            }

        composeTestRule.setContent {
            ShopifyCheckout(
                url = "https://example.myshopify.com/checkouts/test",
                isVisible = isVisible,
                onComplete = { },
                onCancel = { },
                onFail = { },
                presentCheckout = mockPresentCheckout
            )
        }

        composeTestRule.waitForIdle()

        // Initially not visible, so present should not be called
        assertThat(presentCalled).isFalse()

        // Change to visible
        isVisible = true
        composeTestRule.waitForIdle()

        // Now present should be called with correct URL
        assertThat(presentCalled).isTrue()
        assertThat(capturedUrl).isEqualTo("https://example.myshopify.com/checkouts/test")
    }

    @Test
    fun `ShopifyCheckout handles URL changes while visible`() {
        var presentCallCount = 0
        var lastCapturedUrl: String? = null
        val mockDialog = mock<CheckoutSheetKitDialog>()
        var url by mutableStateOf("https://example.myshopify.com/checkouts/test1")

        val mockPresentCheckout: PresentCheckout =
            { url, _, _ ->
                presentCallCount++
                lastCapturedUrl = url
                mockDialog
            }

        composeTestRule.setContent {
            ShopifyCheckout(
                url = url,
                isVisible = true,
                onComplete = { },
                onCancel = { },
                onFail = { },
                presentCheckout = mockPresentCheckout
            )
        }

        composeTestRule.waitForIdle()

        // Initial presentation
        assertThat(presentCallCount).isEqualTo(1)
        assertThat(lastCapturedUrl).isEqualTo("https://example.myshopify.com/checkouts/test1")

        // Change URL while visible - should trigger new presentation
        url = "https://example.myshopify.com/checkouts/test2"
        composeTestRule.waitForIdle()

        // Should have presented again with new URL
        assertThat(presentCallCount).isEqualTo(2)
        assertThat(lastCapturedUrl).isEqualTo("https://example.myshopify.com/checkouts/test2")
    }

    @Test
    fun `ShopifyCheckout calls dismiss on dialog when component is disposed`() {
        val mockDialog = mock<CheckoutSheetKitDialog>()
        var showCheckout by mutableStateOf(true)

        val mockPresentCheckout: PresentCheckout =
            { _, _, _ -> mockDialog }

        composeTestRule.setContent {
            if (showCheckout) {
                ShopifyCheckout(
                    url = "https://example.myshopify.com/checkouts/test",
                    isVisible = true,
                    onComplete = { },
                    onCancel = { },
                    onFail = { },
                        presentCheckout = mockPresentCheckout
                )
            }
        }

        composeTestRule.waitForIdle()

        // Remove the composable to trigger onDispose
        showCheckout = false
        composeTestRule.waitForIdle()

        // Verify that dismiss was called on the dialog during disposal
        verify(mockDialog).dismiss()
    }

}
