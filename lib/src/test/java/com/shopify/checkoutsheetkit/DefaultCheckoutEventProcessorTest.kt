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

import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEvent
import com.shopify.checkoutsheetkit.rpc.events.CheckoutAddressChangeStart
import com.shopify.checkoutsheetkit.rpc.events.CheckoutPaymentMethodChangeStart
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
class DefaultCheckoutEventProcessorTest {

    private lateinit var activity: ComponentActivity
    private lateinit var shadowActivity: ShadowActivity

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
        shadowActivity = shadowOf(activity)
    }

    @Test
    fun `onLinkClick with http scheme launches action view intent with uri as data`() {
        val processor = noopDefaultCheckoutEventProcessor(activity)
        val uri = Uri.parse("https://shopify.com")

        processor.onLinkClick(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
    }

    @Test
    fun `onLinkClick with mailto scheme launches email intent with to address`() {
        val processor = noopDefaultCheckoutEventProcessor(activity)
        val uri = Uri.parse("mailto:test.user@shopify.com")

        processor.onLinkClick(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_EMAIL)).isEqualTo(arrayOf("test.user@shopify.com"))
        assertThat(intent.action).isEqualTo("android.intent.action.SEND")
    }

    @Test
    fun `onLinkClick with tel scheme launches action dial intent with phone number`() {
        val processor = noopDefaultCheckoutEventProcessor(activity)
        val uri = Uri.parse("tel:0123456789")

        processor.onLinkClick(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_DIAL)
    }

    @Test
    fun `onLinkClick with known deep link scheme`() {
        val uri = Uri.parse("geo:40.712776,-74.005974?q=Statue+of+Liberty")

        val pm: PackageManager = RuntimeEnvironment.getApplication().packageManager
        val shadowPackageManager = shadowOf(pm)

        val intentFilter = IntentFilter(Intent.ACTION_VIEW).apply {
            addDataScheme("geo")
        }
        shadowPackageManager.addIntentFilterForActivity(activity.componentName, intentFilter)

        val processor = noopDefaultCheckoutEventProcessor(activity)
        processor.onLinkClick(uri)

        val intent = shadowActivity.nextStartedActivity
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
    }

    @Test
    fun `onLinkClick with unhandled scheme logs warning`() {
        val log = mock<LogWrapper>()
        val processor = noopDefaultCheckoutEventProcessor(activity, log)

        val uri = Uri.parse("ftp:random")

        processor.onLinkClick(uri)

        assertThat(shadowActivity.peekNextStartedActivityForResult()).isNull()
        verify(log).w("DefaultCheckoutEventProcessor", "Unrecognized scheme for link clicked in checkout 'ftp:random'")
    }

    @Test
    fun `onFail returns an error description`() {
        val log = mock<LogWrapper>()
        var description = ""
        var recoverable: Boolean? = null
        val processor =
            object : DefaultCheckoutEventProcessor(activity, log) {
                override fun onComplete(checkoutCompleteEvent: CheckoutCompleteEvent) {
                    /* not implemented */
                }

                override fun onFail(error: CheckoutException) {
                    description = error.errorDescription
                    recoverable = error.isRecoverable
                }

                override fun onCancel() {
                    /* not implemented */
                }

                override fun onAddressChangeStart(event: CheckoutAddressChangeStart) {
                    /* not implemented */
                }

                override fun onCheckoutPaymentMethodChangeStart(event: CheckoutPaymentMethodChangeStart) {
                    /* not implemented */
                }
            }

        val error = object : CheckoutUnavailableException("error description", "unknown", true) {}

        processor.onFail(error)

        assertThat(description).isEqualTo("error description")
        assertThat(recoverable).isTrue()
    }
}
