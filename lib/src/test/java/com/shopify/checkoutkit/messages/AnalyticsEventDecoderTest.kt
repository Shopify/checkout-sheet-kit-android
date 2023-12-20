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
package com.shopify.checkoutkit.messages

import com.shopify.checkoutkit.WebToSdkEvent
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AnalyticsEventDecoderTest {

    private val decoder = AnalyticsEventDecoder(Json { ignoreUnknownKeys = true })

    @Test
    fun `should deserialize an event`() {
        val eventString = """|
            |{
            |    "name": "checkout_started",
            |    "event": {
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "checkout_started",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "data": {
            |            "checkout": {
            |                "order": {
            |                    "id": "123"
            |                }
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()

        val message = WebToSdkEvent(
            name = "analytics",
            body = eventString,
        )

        val result = decoder.decode(message)

        assertThat(result).isInstanceOf(CheckoutStarted::class.java)
        val resultEvent = result as CheckoutStarted
        assertThat(resultEvent.name).isEqualTo("checkout_started")
        assertThat(resultEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(resultEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(resultEvent.data?.checkout?.order?.id).isEqualTo("123")
    }
}
