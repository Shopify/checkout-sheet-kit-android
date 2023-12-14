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

import com.shopify.checkoutkit.LogWrapper
import com.shopify.checkoutkit.WebToSdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class AnalyticsEventDecoderTest {

    private val logWrapper = mock<LogWrapper>()
    private val decoder = AnalyticsEventDecoder(Json { ignoreUnknownKeys = true }, logWrapper)

    @Test
    fun `should deserialize a standard event - checkout started`() {
        val event = """|
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
            .toWebToSdkEvent()


        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(CheckoutStarted::class.java)

        val checkoutStarted = result as CheckoutStarted
        assertThat(checkoutStarted.name).isEqualTo("checkout_started")
        assertThat(checkoutStarted.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(checkoutStarted.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(checkoutStarted.data?.checkout?.order?.id).isEqualTo("123")

        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should deserialize a standard event - CheckoutCompleted`() {
        val event = """|
            |{
            |    "name": "checkout_completed",
            |    "event": {
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "checkout_completed",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "data": {
            |            "checkout": {
            |                "totalPrice": {
            |                   "amount": 123.3,
            |                   "currencyCode": "USD"
            |                },
            |                "order": {
            |                    "id": "123"
            |                }
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()


        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(CheckoutCompleted::class.java)

        val checkoutStarted = result as CheckoutCompleted
        assertThat(checkoutStarted.name).isEqualTo("checkout_completed")
        assertThat(checkoutStarted.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(checkoutStarted.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(checkoutStarted.data?.checkout?.order?.id).isEqualTo("123")
        assertThat(checkoutStarted.data?.checkout?.totalPrice?.amount).isEqualTo(123.3)
        assertThat(checkoutStarted.data?.checkout?.totalPrice?.currencyCode).isEqualTo("USD")

        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should deserialize a custom event`() {
        val event = """|
            |{
            |    "name": "my_custom_event",
            |    "event": {
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "my_custom_event",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "customData": {
            |            "a": {
            |                "b": {
            |                    "c": "d"
            |                }
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(CustomEvent::class.java)

        val customEvent = result as CustomEvent
        assertThat(customEvent.name).isEqualTo("my_custom_event")
        assertThat(customEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(customEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        val customData = Json.decodeFromJsonElement<ExampleClientDefinedType>(customEvent.customData as JsonObject)
        assertThat(customData.a.b.c).isEqualTo("d")

        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should return null for a standard event we don't know about`() {
        val event = """|
            |{
            |    "name": "new_standard_event",
            |    "event": {
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "new_standard_event",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "data": {
            |            "a": {
            |                "b": {
            |                    "c": "d"
            |                }
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isNull()
        verify(logWrapper).w(
            "CheckoutBridge",
            "Unrecognized standard analytics event received 'new_standard_event'"
        )
    }

    @Test
    fun `should return null if event cannot be decoded`() {
        val event = """|
            |{
            |    "name": "cart_viewed",
            |    "event": {
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isNull()
        verify(logWrapper).e(
            eq("CheckoutBridge"),
            eq("Failed to decode analytics event"),
            any()
        )
    }
}

private fun String.toWebToSdkEvent(): WebToSdkEvent {
    return WebToSdkEvent(
        name = "analytics",
        body = this,
    )
}

@Serializable
data class ExampleClientDefinedType(
    val a: B
)

@Serializable
data class B(
    val b: C
)

@Serializable
data class C(
    val c: String
)
