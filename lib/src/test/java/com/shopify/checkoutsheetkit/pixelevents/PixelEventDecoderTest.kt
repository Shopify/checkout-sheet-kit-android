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
package com.shopify.checkoutsheetkit.pixelevents

import com.shopify.checkoutsheetkit.LogWrapper
import com.shopify.checkoutsheetkit.WebToSdkEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class PixelEventDecoderTest {

    private val logWrapper = mock<LogWrapper>()
    private val decoder = PixelEventDecoder(Json { ignoreUnknownKeys = true }, logWrapper)

    @Test
    fun `should deserialize a standard event - checkout started`() {
        val event = """|
            |{
            |    "name": "checkout_started",
            |    "event": {
            |        "type": "standard",
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

        assertThat(result).isInstanceOf(CheckoutStartedPixelEvent::class.java)

        val checkoutStartedEvent = result as CheckoutStartedPixelEvent
        assertThat(checkoutStartedEvent.name).isEqualTo("checkout_started")
        assertThat(checkoutStartedEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(checkoutStartedEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(checkoutStartedEvent.data?.checkout?.order?.id).isEqualTo("123")

        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should deserialize an alert_displayed event`() {
        val event = """|
            |{
            |    "name": "alert_displayed",
            |    "event": {
            |        "type": "standard",
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "alert_displayed",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "data": {
            |            "alert": {
            |                "target": "cart.deliveryGroups[0].deliveryAddress.address1",
            |                "value": "",
            |                "type": "INPUT_REQUIRED",
            |                "message": "Enter an address"
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(AlertDisplayedPixelEvent::class.java)

        val alertDisplayedEvent = result as AlertDisplayedPixelEvent
        assertThat(alertDisplayedEvent.name).isEqualTo("alert_displayed")
        assertThat(alertDisplayedEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(alertDisplayedEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(alertDisplayedEvent.data?.alert?.target).isEqualTo("cart.deliveryGroups[0].deliveryAddress.address1")
        assertThat(alertDisplayedEvent.data?.alert?.value).isEqualTo("")
        assertThat(alertDisplayedEvent.data?.alert?.type).isEqualTo("INPUT_REQUIRED")
        assertThat(alertDisplayedEvent.data?.alert?.message).isEqualTo("Enter an address")
        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should deserialize a ui_extension_errored event`() {
        val event = """|
            |{
            |    "name": "ui_extension_errored",
            |    "event": {
            |        "type": "standard",
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "ui_extension_errored",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "data": {
            |            "error": {
            |                "extensionTarget": "purchase.checkout.delivery.render-after",
            |                "placementReference": "",
            |                "trace": "",
            |                "type": "EXTENSION_USAGE_ERROR",
            |                "message": "Something went wrong",
            |                "appName": "My App",
            |                "appId": "gid://shopify/App/123",
            |                "appVersion": "1.0.0",
            |                "apiVersion": "2024-04",
            |                "extensionName": "My Extension"
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(UIExtensionErroredPixelEvent::class.java)

        val uiExtensionErrorEvent = result as UIExtensionErroredPixelEvent
        assertThat(uiExtensionErrorEvent.name).isEqualTo("ui_extension_errored")
        assertThat(uiExtensionErrorEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(uiExtensionErrorEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(uiExtensionErrorEvent.data?.error?.extensionTarget).isEqualTo("purchase.checkout.delivery.render-after")
        assertThat(uiExtensionErrorEvent.data?.error?.placementReference).isEqualTo("")
        assertThat(uiExtensionErrorEvent.data?.error?.trace).isEqualTo("")
        assertThat(uiExtensionErrorEvent.data?.error?.message).isEqualTo("Something went wrong")
        assertThat(uiExtensionErrorEvent.data?.error?.type).isEqualTo("EXTENSION_USAGE_ERROR")
        assertThat(uiExtensionErrorEvent.data?.error?.appName).isEqualTo("My App")
        assertThat(uiExtensionErrorEvent.data?.error?.appVersion).isEqualTo("1.0.0")
        assertThat(uiExtensionErrorEvent.data?.error?.apiVersion).isEqualTo("2024-04")
        assertThat(uiExtensionErrorEvent.data?.error?.extensionName).isEqualTo("My Extension")
        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should deserialize a standard event - CheckoutCompleted`() {
        val event = """|
            |{
            |    "name": "checkout_completed",
            |    "event": {
            |        "type": "standard",
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

        assertThat(result).isInstanceOf(CheckoutCompletedPixelEvent::class.java)

        val checkoutStarted = result as CheckoutCompletedPixelEvent
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
            |        "type": "custom",
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

        assertThat(result).isInstanceOf(CustomPixelEvent::class.java)

        val customPixelEvent = result as CustomPixelEvent
        assertThat(customPixelEvent.name).isEqualTo("my_custom_event")
        assertThat(customPixelEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(customPixelEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        val customData = Json.decodeFromString<ExampleClientDefinedType>(customPixelEvent.customData!!)
        assertThat(customData.a.b.c).isEqualTo("d")

        verifyNoInteractions(logWrapper)
    }

    @Test
    @Suppress("LongMethod")
    fun `should deserialize a page viewed event`() {
        val event = """|
            |{
            |    "name": "page_viewed",
            |    "event": {
            |        "type": "standard",
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |        "name": "page_viewed",
            |        "timestamp": "2023-12-20T16:39:23+0000",
            |        "context": {
            |            "document": {
            |                "location": {
            |                    "href": "https://test-store.myshopify.com/checkouts/cn/Z2NwLXVzLWNlbnRyYWwxOjAxSEs0U1BUSlozNDhFME5KTlM2MVhaOVE3?ew_m=f",
            |                    "hash": "",
            |                    "host": "test-store.myshopify.com",
            |                    "hostname": "test-store.myshopify.com",
            |                    "origin": "https://test-store.myshopify.com",
            |                    "pathname": "/checkouts/cn/Z2NwLXVzLWNlbnRyYWwxOjAxSEs0U1BUSlozNDhFME5KTlM2MVhaOVE3",
            |                    "port": "",
            |                    "protocol": "https:",
            |                    "search": "?ew_m=f"
            |                },
            |                "referrer": "https://test-store.myshopify.com/products/t-shirt",
            |                "characterSet": "UTF-8",
            |                "title": "Test Store"
            |            },
            |            "navigator": {
            |                "language": "en-GB",
            |                "cookieEnabled": true,
            |                "languages": [
            |                    "en-GB",
            |                    "en-US",
            |                    "en",
            |                    "es"
            |                ],
            |                "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            |            },
            |            "window": {
            |                "innerHeight": 934,
            |                "innerWidth": 1221,
            |                "outerHeight": 1055,
            |                "outerWidth": 1920,
            |                "pageXOffset": 0,
            |                "pageYOffset": 0,
            |                "location": {
            |                    "href": "https://test-store.myshopify.com/checkouts/cn/Z2NwLXVzLWNlbnRyYWwxOjAxSEs0U1BUSlozNDhFME5KTlM2MVhaOVE3?ew_m=f",
            |                    "hash": "",
            |                    "host": "test-store.myshopify.com",
            |                    "hostname": "test-store.myshopify.com",
            |                    "origin": "https://test-store.myshopify.com",
            |                    "pathname": "/checkouts/cn/Z2NwLXVzLWNlbnRyYWwxOjAxSEs0U1BUSlozNDhFME5KTlM2MVhaOVE3",
            |                    "port": "",
            |                    "protocol": "https:",
            |                    "search": "?ew_m=f"
            |                },
            |                "origin": "https://test-store.myshopify.com",
            |                "screen": {
            |                    "height": 1080,
            |                    "width": 1920
            |                },
            |                "screenX": 0,
            |                "screenY": 25,
            |                "scrollX": 0,
            |                "scrollY": 0
            |            }
            |        }
            |    }
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isInstanceOf(PageViewedPixelEvent::class.java)

        val pageViewedEvent = result as PageViewedPixelEvent
        assertThat(pageViewedEvent.name).isEqualTo("page_viewed")
        assertThat(pageViewedEvent.timestamp).isEqualTo("2023-12-20T16:39:23+0000")
        assertThat(pageViewedEvent.id).isEqualTo("sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B")
        assertThat(pageViewedEvent.data).isNull()
        assertThat(pageViewedEvent.context?.document?.location?.href)
            .isEqualTo("https://test-store.myshopify.com/checkouts/cn/Z2NwLXVzLWNlbnRyYWwxOjAxSEs0U1BUSlozNDhFME5KTlM2MVhaOVE3?ew_m=f")

        verifyNoInteractions(logWrapper)
    }

    @Test
    fun `should return null if event cannot be decoded`() {
        val event = """|
            |{
            |    "name": "cart_viewed",
            |    "event": {
            |        "type": "standard",
            |        "id": "sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B",
            |}
        |""".trimMargin()
            .toWebToSdkEvent()

        val result = decoder.decode(event)

        assertThat(result).isNull()
        verify(logWrapper).e(
            eq("CheckoutBridge"),
            eq("Failed to decode pixel event"),
            any()
        )
    }
}

private fun String.toWebToSdkEvent(): WebToSdkEvent {
    return WebToSdkEvent(
        name = "webPixels",
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
