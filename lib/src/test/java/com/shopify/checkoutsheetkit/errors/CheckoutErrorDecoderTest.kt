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
package com.shopify.checkoutsheetkit.errors

import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.WebToSdkEvent
import com.shopify.checkoutsheetkit.errorevents.CheckoutErrorDecoder
import com.shopify.checkoutsheetkit.errorevents.CheckoutErrorGroup
import com.shopify.checkoutsheetkit.errorevents.CheckoutErrorPayload
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito

class CheckoutErrorDecoderTest {

    private val logWrapper = Mockito.mock<LogWrapper>()
    private val decoder = CheckoutErrorDecoder(Json { ignoreUnknownKeys = true }, logWrapper)

    @Test
    fun `should decode a checkout error`() {
        val event = WebToSdkEvent(
            name = "error",
            body = """[
                |{
                |   "group": "unrecoverable",
                |   "flowType": "regular",
                |   "type": "sdk_not_enabled",
                |   "code": "sdk_not_enabled",
                |   "reason": ""
                |}
            ]""".trimMargin()
        )

        val decoded = decoder.decodeMessage(event)

        assertThat(decoded).isEqualTo(
            CheckoutErrorPayload(
                group = CheckoutErrorGroup.UNRECOVERABLE,
                flowType = "regular",
                type = "sdk_not_enabled",
                code = "sdk_not_enabled",
                reason = ""
            )
        )
    }

    @Test
    fun `should return group = unsupported for any groups that arent supported`() {
        val event = WebToSdkEvent(
            name = "error",
            body = """[
                |{
                |   "group": "other",
                |   "flowType": "regular",
                |   "type": "invalid_signature",
                |   "code": "invalid_signature",
                |   "reason": ""
                |}
            ]""".trimMargin()
        )

        val decoded = decoder.decodeMessage(event)

        assertThat(decoded).isEqualTo(
            CheckoutErrorPayload(
                group = CheckoutErrorGroup.UNSUPPORTED,
                flowType = "regular",
                type = "invalid_signature",
                code = "invalid_signature",
                reason = ""
            )
        )
    }

    @Test
    fun `should throw if decoding fails`() {
        val event = WebToSdkEvent(
            name = "error",
            body = """[
                |{
                |   "group": "authentication",
                |   "flowType": "regular",
                |   "type": "invalid_
                |}
            ]""".trimMargin()
        )

        assertThrows(RuntimeException::class.java) { decoder.decodeMessage(event) }
    }

    @Test
    fun `should return first message if multiple exist in payload`() {
        val event = WebToSdkEvent(
            name = "error",
            body = """[
                |{
                |   "group": "unrecoverable",
                |   "flowType": "regular",
                |   "type": "sdk_not_enabled",
                |   "code": "sdk_not_enabled",
                |   "reason": ""
                |},
                |{
                |   "group": "unrecoverable",
                |   "flowType": "regular",
                |   "type": "invalid_checkout_url",
                |   "code": "invalid_checkout_url",
                |   "reason": ""
                |}
            ]""".trimMargin()
        )

        val decoded = decoder.decodeMessage(event)

        assertThat(decoded.code).isEqualTo("sdk_not_enabled")
    }
}
