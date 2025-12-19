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

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutErrorCode
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutErrorEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class CheckoutWebViewEventProcessorTest {

    private val mockEventProcessor = mock<CheckoutEventProcessor>()
    private var capturedError: CheckoutException? = null
    private lateinit var processor: CheckoutWebViewEventProcessor

    @Before
    fun setUp() {
        capturedError = null
        processor = CheckoutWebViewEventProcessor(
            eventProcessor = mockEventProcessor,
            checkoutErrorInterceptor = { error -> capturedError = error }
        )
    }

    @Test
    fun `onCheckoutViewError maps STOREFRONT_PASSWORD_REQUIRED to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.STOREFRONT_PASSWORD_REQUIRED,
            message = "Storefront password is required"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Storefront password is required")
        assertThat(capturedError?.errorCode).isEqualTo("STOREFRONT_PASSWORD_REQUIRED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps CUSTOMER_ACCOUNT_REQUIRED to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.CUSTOMER_ACCOUNT_REQUIRED,
            message = "Customer must be logged in"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Customer must be logged in")
        assertThat(capturedError?.errorCode).isEqualTo("CUSTOMER_ACCOUNT_REQUIRED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps INVALID_PAYLOAD to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.INVALID_PAYLOAD,
            message = "Invalid payload provided"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorCode).isEqualTo("INVALID_PAYLOAD")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps INVALID_SIGNATURE to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.INVALID_SIGNATURE,
            message = "Invalid signature"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorCode).isEqualTo("INVALID_SIGNATURE")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps NOT_AUTHORIZED to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.NOT_AUTHORIZED,
            message = "Not authorized"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorCode).isEqualTo("NOT_AUTHORIZED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps PAYLOAD_EXPIRED to ConfigurationException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.PAYLOAD_EXPIRED,
            message = "Payload has expired"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ConfigurationException::class.java)
        assertThat(capturedError?.errorCode).isEqualTo("PAYLOAD_EXPIRED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps CART_COMPLETED to CheckoutExpiredException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.CART_COMPLETED,
            message = "This checkout has already been completed"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("This checkout has already been completed")
        assertThat(capturedError?.errorCode).isEqualTo("CART_COMPLETED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps INVALID_CART to CheckoutExpiredException`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.INVALID_CART,
            message = "Cart is invalid"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Cart is invalid")
        assertThat(capturedError?.errorCode).isEqualTo("INVALID_CART")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps KILLSWITCH_ENABLED to ClientException with recoverable false`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.KILLSWITCH_ENABLED,
            message = "Checkout is temporarily disabled"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ClientException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Checkout is temporarily disabled")
        assertThat(capturedError?.errorCode).isEqualTo("KILLSWITCH_ENABLED")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps UNRECOVERABLE_FAILURE to ClientException with recoverable false`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.UNRECOVERABLE_FAILURE,
            message = "An unrecoverable error occurred"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ClientException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("An unrecoverable error occurred")
        assertThat(capturedError?.errorCode).isEqualTo("UNRECOVERABLE_FAILURE")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps POLICY_VIOLATION to ClientException with recoverable false`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.POLICY_VIOLATION,
            message = "Policy violation detected"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ClientException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Policy violation detected")
        assertThat(capturedError?.errorCode).isEqualTo("POLICY_VIOLATION")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `onCheckoutViewError maps VAULTED_PAYMENT_ERROR to ClientException with recoverable false`() {
        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.VAULTED_PAYMENT_ERROR,
            message = "Payment method could not be processed"
        )

        processor.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        assertThat(capturedError).isInstanceOf(ClientException::class.java)
        assertThat(capturedError?.errorDescription).isEqualTo("Payment method could not be processed")
        assertThat(capturedError?.errorCode).isEqualTo("VAULTED_PAYMENT_ERROR")
        assertThat(capturedError?.isRecoverable).isFalse()
    }

    @Test
    fun `default checkoutErrorInterceptor calls eventProcessor onFail`() {
        // Test the default behavior when checkoutErrorInterceptor is not provided
        // This simulates inline checkout mode where no explicit error handler is passed
        val testEventProcessor = mock<CheckoutEventProcessor>()

        // Create processor without passing checkoutErrorInterceptor - uses default
        val processorWithDefault = CheckoutWebViewEventProcessor(
            eventProcessor = testEventProcessor
        )

        val event = CheckoutErrorEvent(
            code = CheckoutErrorCode.CART_COMPLETED,
            message = "Cart has been completed"
        )

        processorWithDefault.onCheckoutViewError(event)
        ShadowLooper.runUiThreadTasks()

        // Verify that onFail was called via the default checkoutErrorInterceptor lambda
        val captor = argumentCaptor<CheckoutException>()
        verify(testEventProcessor).onFail(captor.capture())

        val capturedError = captor.firstValue
        assertThat(capturedError).isInstanceOf(CheckoutExpiredException::class.java)
        assertThat(capturedError.errorDescription).isEqualTo("Cart has been completed")
        assertThat(capturedError.errorCode).isEqualTo("CART_COMPLETED")
    }
}
