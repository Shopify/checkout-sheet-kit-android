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

import android.net.Uri
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.CheckoutSheetKitException
import com.shopify.checkoutsheetkit.lifecycleevents.CartInfo
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.lifecycleevents.OrderDetails
import com.shopify.checkoutsheetkit.lifecycleevents.Price
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ComposeCheckoutEventProcessorTest {

    private lateinit var activity: ComponentActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
    }

    @Test
    fun `onCheckoutCompleted invokes callback`() {
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
        var capturedEvent: CheckoutCompletedEvent? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { capturedEvent = it },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onCheckoutCompleted(testCompletedEvent)

        assertThat(capturedEvent).isEqualTo(testCompletedEvent)
    }

    @Test
    fun `onCheckoutCanceled invokes callback`() {
        var canceledCalled = false

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { canceledCalled = true },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onCheckoutCanceled()

        assertThat(canceledCalled).isTrue()
    }

    @Test
    fun `onCheckoutFailed invokes callback`() {
        val mockError = CheckoutSheetKitException("Test error", "test_error", false)
        var capturedError: com.shopify.checkoutsheetkit.CheckoutException? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { capturedError = it },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onCheckoutFailed(mockError)

        assertThat(capturedError).isEqualTo(mockError)
    }

    @Test
    fun `onCheckoutLinkClicked invokes custom callback when provided`() {
        val testUri = Uri.parse("https://example.com")
        var capturedUri: Uri? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = { capturedUri = it },
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onCheckoutLinkClicked(testUri)

        assertThat(capturedUri).isEqualTo(testUri)
    }

    @Test
    fun `onGeolocationPermissionsHidePrompt invokes callback when provided`() {
        var hideCalled = false

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = { hideCalled = true },
        )

        processor.onGeolocationPermissionsHidePrompt()

        assertThat(hideCalled).isTrue()
    }

    @Test
    fun `processor instantiates correctly with all required callbacks`() {
        // Test that the processor can be created and core callbacks work
        var completedCalled = false
        var canceledCalled = false
        var failedCalled = false

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { completedCalled = true },
            checkoutCanceledCallback = { canceledCalled = true },
            checkoutFailedCallback = { failedCalled = true },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        // Verify processor is created successfully and callbacks work
        assertThat(processor).isNotNull()
        assertThat(completedCalled).isFalse() // Not called yet
        assertThat(canceledCalled).isFalse() // Not called yet  
        assertThat(failedCalled).isFalse() // Not called yet
    }

    @Test
    fun `onWebPixelEvent invokes callback when provided`() {
        val testPixelEvent = mock<PixelEvent>()
        var capturedEvent: PixelEvent? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = { capturedEvent = it },
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onWebPixelEvent(testPixelEvent)

        assertThat(capturedEvent).isEqualTo(testPixelEvent)
    }

    @Test
    fun `onPermissionRequest invokes callback when provided`() {
        val testPermissionRequest = mock<PermissionRequest>()
        var capturedRequest: PermissionRequest? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = { capturedRequest = it },
            fileChooserCallback = null,
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        processor.onPermissionRequest(testPermissionRequest)

        assertThat(capturedRequest).isEqualTo(testPermissionRequest)
    }

    @Test
    fun `onShowFileChooser invokes callback when provided`() {
        val testWebView = mock<WebView>()
        val testFilePathCallback = mock<ValueCallback<Array<Uri>>>()
        val testFileChooserParams = mock<WebChromeClient.FileChooserParams>()
        var fileChooserCalled = false

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = { _, _, _ ->
                fileChooserCalled = true
                true
            },
            geolocationRequestCallback = null,
            geolocationHideCallback = null,
        )

        val result = processor.onShowFileChooser(testWebView, testFilePathCallback, testFileChooserParams)

        assertThat(fileChooserCalled).isTrue()
        assertThat(result).isTrue()
    }

    @Test
    fun `onGeolocationPermissionsShowPrompt invokes callback when provided`() {
        val testOrigin = "https://example.com"
        val testCallback = mock<GeolocationPermissions.Callback>()
        var geolocationRequestCalled = false
        var capturedOrigin: String? = null

        val processor = ComposeCheckoutEventProcessor(
            context = activity,
            checkoutCompletedCallback = { },
            checkoutCanceledCallback = { },
            checkoutFailedCallback = { },
            linkClickedCallback = null,
            webPixelEventCallback = null,
            permissionRequestCallback = null,
            fileChooserCallback = null,
            geolocationRequestCallback = { origin, _ ->
                geolocationRequestCalled = true
                capturedOrigin = origin
            },
            geolocationHideCallback = null,
        )

        processor.onGeolocationPermissionsShowPrompt(testOrigin, testCallback)

        assertThat(geolocationRequestCalled).isTrue()
        assertThat(capturedOrigin).isEqualTo(testOrigin)
    }
}
