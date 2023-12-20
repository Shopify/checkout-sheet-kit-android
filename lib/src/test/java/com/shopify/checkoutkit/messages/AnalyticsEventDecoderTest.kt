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
