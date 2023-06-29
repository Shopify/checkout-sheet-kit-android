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
package com.shopify.checkout

import com.shopify.checkout.CheckoutBridge.CheckoutWebOperation.COMPLETED
import com.shopify.checkout.CheckoutBridge.CheckoutWebOperation.MODAL
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(MockitoJUnitRunner::class)
class CheckoutBridgeTest {
    @Mock
    private lateinit var mockEventProcessor: CheckoutWebViewEventProcessor
    private lateinit var checkoutBridge: CheckoutBridge

    @Before
    fun init() {
        checkoutBridge = CheckoutBridge(mockEventProcessor)
    }

    @Test
    fun `parseMessage calls web event processor onCheckoutViewComplete when completed message received`() {
        checkoutBridge.postMessage(Json.encodeToString(CheckoutBridge.JSMessage(COMPLETED.key)))
        verify(mockEventProcessor).onCheckoutViewComplete()
    }

    @Test
    fun `parseMessage calls web event processor onCheckoutModalToggled when modal message received - false`() {
        checkoutBridge.postMessage(
            Json.encodeToString(
                CheckoutBridge.JSMessage(
                    MODAL.key,
                    "false"
                )
            )
        )
        verify(mockEventProcessor).onCheckoutViewModalToggled(false)
    }

    @Test
    fun `parseMessage calls web event processor onCheckoutModalToggled when modal message received - true`() {
        checkoutBridge.postMessage(
            Json.encodeToString(
                CheckoutBridge.JSMessage(
                    MODAL.key,
                    "true"
                )
            )
        )
        verify(mockEventProcessor).onCheckoutViewModalToggled(true)
    }

    @Test
    fun `parseMessage does not issue a msg to the event processor when unsupported message received`() {
        checkoutBridge.postMessage(Json.encodeToString(CheckoutBridge.JSMessage("boom")))
        verifyNoInteractions(mockEventProcessor)
    }
}
