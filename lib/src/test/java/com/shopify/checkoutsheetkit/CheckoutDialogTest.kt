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

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import com.shopify.checkoutsheetkit.lifecycleevents.emptyCompletedEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.awaitility.Awaitility.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowWebView
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CheckoutDialogTest {

    private lateinit var activity: ComponentActivity
    private lateinit var processor: DefaultCheckoutEventProcessor
    private lateinit var configuration: Configuration

    @Before
    fun setUp() {
        configuration = ShopifyCheckoutSheetKit.configuration
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = false)
        }
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
        processor = defaultCheckoutEventProcessor()
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = configuration.preloading
            it.colorScheme = configuration.colorScheme
            it.errorRecovery = configuration.errorRecovery
        }
    }

    @Test
    fun `shows dialog when present is called`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()

        assertThat(dialog.isShowing).isTrue
    }

    @Test
    fun `checkoutView is added to the container when dialog is presented`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()

        ShadowLooper.runUiThreadTasks()

        await().atMost(2, TimeUnit.SECONDS).until {
            dialog.containsChildOfType(CheckoutWebView::class.java)
        }
    }

    @Test
    fun `checkoutView child WebView onResume has been called`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val webView: WebView = ShadowDialog.getLatestDialog()
            .findViewById<CheckoutWebViewContainer>(R.id.checkoutSdkContainer)
            .children.firstOrNull { it is WebView } as WebView? ?: fail("No WebVew found in dialog")

        assertThat(shadowOf(webView).wasOnResumeCalled()).isTrue()
    }

    @Test
    fun `cancel() removes checkoutView from the container so that it can be destroyed`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        dialog.cancel()
        ShadowLooper.runUiThreadTasks()

        await().atMost(2, TimeUnit.SECONDS).until {
            !dialog.containsChildOfType(CheckoutWebView::class.java)
        }
    }

    @Test
    fun `present returns interface allowing dismissal of the dialog`() {
        val dialogHandle = ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(dialog.isShowing).isTrue()
        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        dialogHandle?.dismiss()
        ShadowLooper.runUiThreadTasks()

        assertThat(dialog.isShowing).isFalse()
        await().atMost(2, TimeUnit.SECONDS).until {
            !dialog.containsChildOfType(CheckoutWebView::class.java)
        }
    }

    @Test
    fun `calls onCheckoutCanceled if cancel is called`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        dialog.cancel()
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        verify(mockEventProcessor).onCheckoutCanceled()
        verify(mockEventProcessor, never()).onCheckoutFailed(any())
    }

    @Test
    fun `calls onCheckoutFailed if closeCheckoutDialogWithError for non-recoverable error`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        val checkoutDialog = dialog as CheckoutDialog
        val error = checkoutException(isRecoverable = false)

        checkoutDialog.closeCheckoutDialogWithError(error)
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        verify(mockEventProcessor, never()).onCheckoutCanceled()
        verify(mockEventProcessor).onCheckoutFailed(error)
    }

    @Test
    fun `calls attemptToRecoverFromError if closeCheckoutDialogWithError is called with recoverable error`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val checkoutDialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        assertThat(checkoutDialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        checkoutDialog.closeCheckoutDialogWithError(checkoutException(isRecoverable = true))
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // attemptToRecoverFromError creates a FallbackWebView and removes the CheckoutWebView
        assertThat(checkoutDialog.containsChildOfType(FallbackWebView::class.java)).isTrue()
        assertThat(checkoutDialog.containsChildOfType(CheckoutWebView::class.java)).isFalse()
        verify(mockEventProcessor, never()).onCheckoutCanceled()
        verify(mockEventProcessor).onCheckoutFailed(any())
    }

    @Test
    fun `can disable fallback behaviour via shouldRecoverFromError`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.configure {
            it.errorRecovery = object : ErrorRecovery {
                override fun shouldRecoverFromError(checkoutException: CheckoutException): Boolean {
                    return false
                }
            }
        }
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val checkoutDialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        assertThat(checkoutDialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        val error = checkoutException(isRecoverable = true)
        checkoutDialog.closeCheckoutDialogWithError(error)
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        // attemptToRecoverFromError creates a FallbackWebView and removes the CheckoutWebView
        assertThat(checkoutDialog.containsChildOfType(FallbackWebView::class.java)).isFalse()
        assertThat(checkoutDialog.containsChildOfType(CheckoutWebView::class.java)).isFalse()
        verify(mockEventProcessor, never()).onCheckoutCanceled()
        verify(mockEventProcessor).onCheckoutFailed(error)
    }

    @Test
    fun `calls onCheckoutCanceled if close menu item is clicked`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        // click cancel button
        val header = dialog.findViewById<Toolbar>(R.id.checkoutSdkHeader)
        header.menu.performIdentifierAction(R.id.checkoutSdkCloseBtn, 0)
        ShadowLooper.runUiThreadTasks()

        verify(mockEventProcessor, timeout(2000)).onCheckoutCanceled()
    }

    @Test
    fun `clicking close invokes cancel(), removing checkoutView from the container`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        // click cancel button
        val header = dialog.findViewById<Toolbar>(R.id.checkoutSdkHeader)
        header.menu.performIdentifierAction(R.id.checkoutSdkCloseBtn, 0)
        ShadowLooper.runUiThreadTasks()

        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isFalse()
    }

    @Test
    fun `sets header background color based on current configuration`() {
        val customColors = customColors()
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Web(customColors)

        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        val header = dialog.findViewById<Toolbar>(R.id.checkoutSdkHeader)
        val headerBackgroundColor = backgroundColor(header)
        val configuredColor = customColors.headerBackground.getValue(activity)

        assertThat(headerBackgroundColor).isEqualTo(configuredColor)
    }

    @Test
    fun `sets WebView container background color based on current configuration`() {
        val customColors = customColors()
        ShopifyCheckoutSheetKit.configuration.colorScheme = ColorScheme.Web(customColors)

        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        val webViewContainer = dialog.findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        val webViewContainerBackgroundColor = backgroundColor(webViewContainer)
        val configuredColor = customColors.webViewBackground.getValue(activity)

        assertThat(webViewContainerBackgroundColor).isEqualTo(configuredColor)
    }

    @Test
    fun `attemptToRecoverFromError replaces CheckoutWebView with FallbackWebView`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isTrue()

        dialog.attemptToRecoverFromError(checkoutException(isRecoverable = true))
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        assertThat(dialog.containsChildOfType(CheckoutWebView::class.java)).isFalse()
        assertThat(dialog.containsChildOfType(FallbackWebView::class.java)).isTrue()
    }

    @Test
    fun `attemptToRecoverFromError invokes pre recovery actions`() {
        var recoveryCalled = false

        ShopifyCheckoutSheetKit.configure {
            it.errorRecovery = object : ErrorRecovery {
                override fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
                    recoveryCalled = true
                }
            }
        }

        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        dialog.attemptToRecoverFromError(checkoutException(isRecoverable = true))
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        assertThat(recoveryCalled).isTrue()
    }

    @Test
    fun `attemptToRecoverFromError loads existing checkout URL`() {
        val checkoutUrl = "https://shopify.com"
        ShopifyCheckoutSheetKit.present(checkoutUrl, activity, processor)

        val dialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        dialog.attemptToRecoverFromError(checkoutException(isRecoverable = true))
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        val layout = dialog.findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        val fallbackView = layout.children.first { it is FallbackWebView } as FallbackWebView
        assertThat(shadowOf(fallbackView).lastLoadedUrl).isEqualTo(checkoutUrl)
    }

    @Test
    fun `attemptToRecoverFromError sets event processor`() {
        val checkoutUrl = "https://shopify.com"
        val mockProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present(checkoutUrl, activity, mockProcessor)

        val dialog = ShadowDialog.getLatestDialog() as CheckoutDialog
        dialog.attemptToRecoverFromError(checkoutException(isRecoverable = true))
        shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        val layout = dialog.findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        val fallbackView = layout.children.first { it is FallbackWebView } as FallbackWebView

        val completedEvent = emptyCompletedEvent()

        fallbackView.getEventProcessor().onCheckoutViewComplete(completedEvent)
        verify(mockProcessor).onCheckoutCompleted(completedEvent)
    }

    private fun backgroundColor(view: View): Int {
        return (view.background as ColorDrawable).color
    }

    private fun customColors(): Colors {
        return Colors(
            headerFont = Color.ResourceId(androidx.appcompat.R.color.material_grey_850),
            headerBackground = Color.ResourceId(androidx.appcompat.R.color.material_blue_grey_900),
            webViewBackground = Color.ResourceId(androidx.appcompat.R.color.material_deep_teal_200),
            progressIndicator = Color.ResourceId(androidx.appcompat.R.color.background_material_dark),
        )
    }

    private fun <T: WebView> Dialog.containsChildOfType(clazz: Class<T>): Boolean {
        val layout = this.findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        return layout.children.any { clazz.isInstance(it) }
    }

    private fun defaultCheckoutEventProcessor(): DefaultCheckoutEventProcessor {
        return object : DefaultCheckoutEventProcessor(activity) {
            override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
                // no-op
            }
            override fun onCheckoutFailed(error: CheckoutException) {
                // no-op
            }
            override fun onCheckoutCanceled() {
                // no-op
            }
        }
    }

    private fun checkoutException(isRecoverable: Boolean): CheckoutException {
        return CheckoutSheetKitException(
            errorCode = CheckoutSheetKitException.ERROR_SENDING_MESSAGE_TO_CHECKOUT,
            errorDescription = "Error sending message to checkout",
            isRecoverable = isRecoverable,
        )
    }
}
