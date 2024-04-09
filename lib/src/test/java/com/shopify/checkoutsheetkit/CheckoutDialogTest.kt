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
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import org.assertj.core.api.Assertions.assertThat
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
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CheckoutDialogTest {

    private lateinit var activity: ComponentActivity
    private lateinit var processor: DefaultCheckoutEventProcessor

    @Before
    fun setUp() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = false)
        }
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
        processor = defaultCheckoutEventProcessor()
    }

    @After
    fun tearDown() {
        ShopifyCheckoutSheetKit.configure {
            it.preloading = Preloading(enabled = true)
        }
    }

    @Test
    fun `shows dialog when present is called`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()

        assertThat(dialog.isShowing).isTrue
    }

    @Test
    fun `when dialog is presented checkoutView is added to the container`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()

        ShadowLooper.runUiThreadTasks()

        await().atMost(2, TimeUnit.SECONDS).until {
            containerChildCount(dialog) == 2
        }
    }

    @Test
    fun `cancel() removes checkoutView from the container so that it can be destroyed`() {
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, processor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(containerChildCount(dialog)).isEqualTo(2)

        dialog.cancel()
        ShadowLooper.runUiThreadTasks()

        await().atMost(2, TimeUnit.SECONDS).until {
            containerChildCount(dialog) == 1
        }
    }

    @Test
    fun `cancel calls onCheckoutCanceled if cancel is called`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        dialog.cancel()
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        verify(mockEventProcessor).onCheckoutCanceled()
        verify(mockEventProcessor, never()).onCheckoutFailed(any())
    }

    @Test
    fun `cancel calls onCheckoutFailed if closeCheckoutDialogWithError is called`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        val checkoutDialog = dialog as CheckoutDialog
        val error = CheckoutSdkError("Error occurred")

        checkoutDialog.closeCheckoutDialogWithError(error)
        Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()

        verify(mockEventProcessor, never()).onCheckoutCanceled()
        verify(mockEventProcessor).onCheckoutFailed(error)
    }

    @Test
    fun `cancel menu option calls onCheckoutCanceled if close menu item is clicked`() {
        val mockEventProcessor = mock<DefaultCheckoutEventProcessor>()
        ShopifyCheckoutSheetKit.present("https://shopify.com", activity, mockEventProcessor)

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(containerChildCount(dialog)).isEqualTo(2)

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
        assertThat(containerChildCount(dialog)).isEqualTo(2)

        // click cancel button
        val header = dialog.findViewById<Toolbar>(R.id.checkoutSdkHeader)
        header.menu.performIdentifierAction(R.id.checkoutSdkCloseBtn, 0)
        ShadowLooper.runUiThreadTasks()

        await().atMost(2, TimeUnit.SECONDS).until {
            containerChildCount(dialog) == 1
        }
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

    private fun containerChildCount(dialog: Dialog): Int {
        val layout = dialog.findViewById<RelativeLayout>(R.id.checkoutSdkContainer)
        return layout.childCount
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
}
