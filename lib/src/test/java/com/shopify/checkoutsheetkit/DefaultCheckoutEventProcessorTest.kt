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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.webkit.GeolocationPermissions
import androidx.activity.ComponentActivity
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
class DefaultCheckoutEventProcessorTest {

    private lateinit var context: Context
    private lateinit var activity: ComponentActivity
    private lateinit var shadowActivity: ShadowActivity
    private val mockCallback = mock<GeolocationPermissions.Callback>()
    private lateinit var packageManager: PackageManager

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).get()
        shadowActivity = shadowOf(activity)
        context = mock()
        packageManager = mock()
        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn("com.test.package")
    }

    @Test
    fun `onCheckoutLinkClicked with http scheme launches action view intent with uri as data`() {
        val processor = processor(activity)
        val uri = Uri.parse("https://shopify.com")

        processor.onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
    }

    @Test
    fun `onCheckoutLinkClicked with mailto scheme launches email intent with to address`() {
        val uri = Uri.parse("mailto:test.user@shopify.com")

        processor(activity).onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.getStringArrayExtra(Intent.EXTRA_EMAIL))
                .isEqualTo(arrayOf("test.user@shopify.com"))
        assertThat(intent.action).isEqualTo("android.intent.action.SEND")
    }

    @Test
    fun `onCheckoutLinkClicked with tel scheme launches action dial intent with phone number`() {
        val uri = Uri.parse("tel:0123456789")

        processor(activity).onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_DIAL)
    }

    @Test
    fun `onCheckoutLinkedClick with known deep link scheme logs warning`() {
        val log = mock<LogWrapper>()
        val uri = Uri.parse("geo:40.712776,-74.005974?q=Statue+of+Liberty")

        val expectedIntent = Intent(Intent.ACTION_VIEW)
        expectedIntent.data = uri

        val pm: PackageManager = RuntimeEnvironment.getApplication().packageManager
        val shadowPackageManager = shadowOf(pm)
        shadowPackageManager.addResolveInfoForIntent(expectedIntent, ResolveInfo())

        processor(activity, log).onCheckoutLinkClicked(uri)

        val intent = shadowActivity.peekNextStartedActivityForResult().intent
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.action).isEqualTo(Intent.ACTION_VIEW)
    }

    @Test
    fun `onCheckoutLinkedClick with unhandled scheme logs warning`() {
        val log = mock<LogWrapper>()
        val uri = Uri.parse("ftp:random")

        processor(activity, log).onCheckoutLinkClicked(uri)

        assertThat(shadowActivity.peekNextStartedActivityForResult()).isNull()
        verify(log)
                .w(
                        "DefaultCheckoutEventProcessor",
                        "Unrecognized scheme for link clicked in checkout 'ftp:random'"
                )
    }

    @Test
    fun `onCheckoutFailed returns an error description`() {
        val log = mock<LogWrapper>()
        var description = ""
        var recoverable: Boolean? = null
        val processor =
                object : TestCheckoutEventProcessor(activity, log) {
                    override fun onCheckoutFailed(error: CheckoutException) {
                        description = error.errorDescription
                        recoverable = error.isRecoverable
                    }
                }

        val error = object : CheckoutUnavailableException("error description", "unknown", true) {}

        processor.onCheckoutFailed(error)

        assertThat(description).isEqualTo("error description")
        assertThat(recoverable).isTrue()
    }

    @Test
    fun `invokes the callback with false for grant and retain arguments`() {
        // Simulate no permissions declared in the manifest
        whenever(packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS))
                .thenThrow(PackageManager.NameNotFoundException())
        processor(activity).onGeolocationPermissionsShowPrompt("http://shopify.com", mockCallback)
        verify(mockCallback).invoke("http://shopify.com", false, false)
    }

    @Test
    fun `invokes the callback with true for grant and retain arguments when included in the manifest`() {
        // Simulate permissions declared in the manifest
        val permissions =
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
        whenever(packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS))
                .thenReturn(mockPackageInfo(permissions))

        // Simulate runtime permission granted
        whenever(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                .thenReturn(PackageManager.PERMISSION_GRANTED)
        whenever(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
                .thenReturn(PackageManager.PERMISSION_GRANTED)

        TestCheckoutEventProcessor(context)
                .onGeolocationPermissionsShowPrompt("http://shopify.com", mockCallback)

        verify(mockCallback).invoke("http://shopify.com", true, true)
    }

    @Test
    fun `invokes the callback with false for grant and retain arguments when not included in the manifest`() {
        // Simulate permissions declared in the manifest
        val permissions =
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
        whenever(packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS))
                .thenReturn(mockPackageInfo(permissions))

        // Simulate runtime permission denied
        whenever(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                .thenReturn(PackageManager.PERMISSION_DENIED)
        whenever(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
                .thenReturn(PackageManager.PERMISSION_DENIED)

        TestCheckoutEventProcessor(context)
                .onGeolocationPermissionsShowPrompt("http://shopify.com", mockCallback)

        verify(mockCallback).invoke("http://shopify.com", false, false)
    }

    // Private

    private fun processor(
            activity: ComponentActivity,
            log: LogWrapper = LogWrapper()
    ): DefaultCheckoutEventProcessor {
        return object : TestCheckoutEventProcessor(activity, log) {}
    }

    private fun mockPackageInfo(permissions: Array<String>): PackageInfo {
        return PackageInfo().apply { requestedPermissions = permissions }
    }

    private open class TestCheckoutEventProcessor(
            context: Context,
            log: LogWrapper = LogWrapper()
    ) : DefaultCheckoutEventProcessor(context, log) {
        override fun onCheckoutCompleted(checkoutCompletedEvent: CheckoutCompletedEvent) {
            /*noop*/
        }
        override fun onCheckoutFailed(error: CheckoutException) {
            /*noop*/
        }
        override fun onCheckoutCanceled() {
            /*noop*/
        }
    }
}
