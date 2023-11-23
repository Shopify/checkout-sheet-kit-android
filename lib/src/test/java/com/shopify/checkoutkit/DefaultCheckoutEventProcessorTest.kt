package com.shopify.checkoutkit

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
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
    fun `onCheckoutLinkClicked with http scheme launches action view intent with uri as data`() {
        val processor = processor(activity)
        val uri = Uri.parse("https://shopify.com")

        processor.onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        intent.type = Intent.ACTION_VIEW
    }

    @Test
    fun `onCheckoutLinkClicked with mailto scheme launches email intent with to address`() {
        val processor = processor(activity)
        val uri = Uri.parse("mailto:test.user@shopify.com")

        processor.onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_EMAIL)).isEqualTo(arrayOf("test.user@shopify.com"))
        intent.type = "vnd.android.cursor.item/email"
    }

    @Test
    fun `onCheckoutLinkClicked with tel scheme launches action dial intent with phone number`() {
        val processor = processor(activity)
        val uri = Uri.parse("tel:0123456789")

        processor.onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        intent.type =  Intent.ACTION_DIAL
    }

    @Test
    fun `onCheckoutLinkedClick with unhandled scheme logs warning`() {
        val log = mock<LogWrapper>()
        val processor = object: DefaultCheckoutEventProcessor(activity, log) {
            override fun onCheckoutCompleted() {/* not implemented */}
            override fun onCheckoutFailed(error: CheckoutException) {/* not implemented */}
            override fun onCheckoutCanceled() {/* not implemented */}
        }

        val uri = Uri.parse("ftp:lsklsm")

        processor.onCheckoutLinkClicked(uri)

        assertThat(shadowActivity.peekNextStartedActivityForResult()).isNull()
        verify(log).w("DefaultCheckoutEventProcessor", "Unrecognized scheme for link clicked in checkout 'ftp:lsklsm'")
    }

    private fun processor(activity: ComponentActivity): DefaultCheckoutEventProcessor {
        return object: DefaultCheckoutEventProcessor(activity) {
            override fun onCheckoutCompleted() {/* not implemented */}
            override fun onCheckoutFailed(error: CheckoutException) {/* not implemented */}
            override fun onCheckoutCanceled() {/* not implemented */}
        }
    }
}
